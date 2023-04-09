//> using scala "2.13"
//> using jvm "system"
//> using deps "org.jsoup:jsoup:1.15.4"
//> using repository "https://maven.aliyun.com/repository/public/"

import org.jsoup._
import scala.jdk.CollectionConverters._

object Main {
  def main(args: Array[String]): Unit = {
    if (args.find(_ == "wikipedia").isDefined) {
      scrapWikipedia()
    }
    if (args.find(_ == "mdn").isDefined) {
      scrapMDN()
    }

    def scrapWikipedia() = {
      val doc =
        Jsoup
          .connect("https://en.wikipedia.org/")
          .proxy("127.0.0.1", 7890)
          .get();

      val headlines = doc.select("#mp-itn b a").asScala
      for (headline <- headlines) {
        println(headline.attr("title"))
        println(headline.attr("href"))
        println()
      }
    }

    def scrapMDN() = {
      val baseUrl = "https://developer.mozilla.org"
      val proxyAddr = "127.0.0.1"
      val proxyPort = 7890
      val doc =
        Jsoup
          .connect(baseUrl + "/en-US/docs/Web/API")
          .proxy(proxyAddr, proxyPort)
          .get();

      val links =
        doc.select("h2#interfaces").nextAll().select("div.index a").asScala

      // (interfaceName, interfaceLink)
      val interfaces =
        links.map(e => (e.selectFirst("code").text(), e.attr("href")))

      // (desc, interfaceName)
      val interfaceDesc =
        interfaces
          .take(1)
          .map(i =>
            (
              Jsoup
                .connect(baseUrl + i._2)
                .proxy(proxyAddr, proxyPort)
                .get()
                .selectFirst(
                  "article.main-page-content > div.section-content"
                ) match {
                case null => "null"
                case n    => n.text()
              },
              i._1
            )
          )
      interfaceDesc.foreach(println)
    }
  }
}
