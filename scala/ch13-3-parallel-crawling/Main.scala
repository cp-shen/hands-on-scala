//> using scala "2.13"
//> using jvm "system"
//> using deps "com.lihaoyi::os-lib::0.9.1"
//> using deps "com.lihaoyi::requests::0.8.0"
//> using deps "com.lihaoyi::ujson::2.0.0"
//> using dep "org.scalameta::munit::1.0.0-M7"
//> using repository "https://maven.aliyun.com/repository/public/"
//> using repository "central"

import java.lang.System.nanoTime
import concurrent._
import concurrent.ExecutionContext.Implicits.global

object Main extends App {
  def proxyAddr = "127.0.0.1"
  def proxyPort = 7890

  def profile[R](code: => R, t: Long = nanoTime) =
    (code, (nanoTime - t) / 1000 / 1000) // convert nanotime to milisec

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
}
