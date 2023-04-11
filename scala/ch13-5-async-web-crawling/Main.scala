//> using scala "2.13"
//> using jvm "system"
//> using dep "com.lihaoyi::os-lib::0.9.1"
//> using dep "com.lihaoyi::requests::0.8.0"
//> using dep "com.lihaoyi::ujson::2.0.0"
//> using dep "org.scalameta::munit::1.0.0-M7"
//> using dep "org.asynchttpclient:async-http-client:2.12.3"
//> using repository "https://maven.aliyun.com/repository/public/"
//> using repository "central"

import java.lang.System.nanoTime
import org.asynchttpclient.Dsl._
import concurrent._
import concurrent.ExecutionContext.Implicits.global
import concurrent.duration.Duration._

object Main {
  def main(args: Array[String]): Unit = {}

  val asynchttpclient = asyncHttpClient(
    config().setProxyServer(proxyServer(proxyAddr, proxyPort))
  )

  def proxyAddr = "127.0.0.1"
  def proxyPort = 7890

  def profile[R](code: => R, t: Long = nanoTime) =
    (code, (nanoTime - t) / 1000 / 1000) // convert nanotime to milisec

  def fetchLinksAsync(title: String): Future[Seq[String]] = {
    val p = Promise[String]
    val linstenableFut = asynchttpclient
      .prepareGet(
        "https://en.wikipedia.org/w/api.php"
      )
      .addQueryParam("action", "query")
      .addQueryParam("titles", title)
      .addQueryParam("prop", "links")
      .addQueryParam("format", "json")
      .execute()
    linstenableFut.addListener(
      () => p.success(linstenableFut.get().getResponseBody),
      null
    )
    val scalaFut: Future[String] = p.future
    scalaFut.map { responseBody =>
      for {
        page <- ujson.read(responseBody)("query")("pages").obj.values.toSeq
        links <- page.obj.get("links").toSeq
        link <- links.arr
      } yield link("title").str
    }

  }

  def fetchLinks(title: String): Seq[String] = {
    val resp = requests.get(
      "https://en.wikipedia.org/w/api.php",
      proxy = (proxyAddr, proxyPort),
      params = Seq(
        "action" -> "query",
        "titles" -> title,
        "prop" -> "links",
        "format" -> "json"
      )
    )
    for {
      page <- ujson.read(resp.text())("query")("pages").obj.values.toSeq
      links <- page.obj.get("links").toSeq
      link <- links.arr
    } yield link("title").str
  }

  def fetchAllLinks(startTitle: String, depth: Int): Set[String] = {
    var seen = Set(startTitle)
    var current = Set(startTitle)
    for (i <- Range(0, depth)) {
      val nextTitleLists =
        for (title <- current) yield {
          fetchLinks(title)
        }
      current = nextTitleLists.flatten.filter(!seen.contains(_))
      seen = seen ++ current
    }
    seen
  }

  def fetchAllLinksPar(startTitle: String, depth: Int): Set[String] = {
    var seen = Set(startTitle)
    var current = Set(startTitle)
    for (i <- Range(0, depth)) {
      val futures = for (title <- current) yield Future {
        fetchLinks(title)
      }
      val nextTitleLists = futures.map(Await.result(_, duration.Duration.Inf))
      current = nextTitleLists.flatten.filter(!seen.contains(_))
      seen = seen ++ current
    }
    seen
  }

  def fetchAllLinksRec(startTitle: String, depth: Int): Set[String] = {
    def rec(
        current: Set[String],
        seen: Set[String],
        recDepth: Int
    ): Set[String] = {
      if (recDepth >= depth) seen
      else {
        val futures = for (title <- current) yield Future { fetchLinks(title) }
        val nextTitleLists = futures.map(Await.result(_, Inf))
        val nextTitles = nextTitleLists.flatten
        rec(
          nextTitles.filter(!seen.contains(_)),
          seen ++ nextTitles,
          recDepth + 1
        )
      }
    }
    rec(Set(startTitle), Set(startTitle), 0)
  }

  def fetchAllLinksAsync(
      startTitle: String,
      depth: Int
  ): Future[Set[String]] = {

    def rec(
        current: Set[String],
        seen: Set[String],
        recDepth: Int
    ): Future[Set[String]] = {
      if (recDepth >= depth) Future.successful(seen)
      else {
        val futures = for (title <- current) yield fetchLinksAsync(title)
        Future
          .sequence(futures)
          .map { nextTitleLists =>
            val nextTitles = nextTitleLists.flatten
            rec(
              nextTitles.filter(!seen.contains(_)),
              seen ++ nextTitles,
              recDepth + 1
            )
          }
          .flatten
      }
    }

    rec(Set(startTitle), Set(startTitle), 0)
  }

}
