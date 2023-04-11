import concurrent.duration.Duration
class MyTests extends munit.FunSuite {

  // override val munitTimeout = Duration(200, "s")
  // test("crawling test") {
  //   val respTitle = Vector("Singapore", "China")
  //   val respDepth = Vector(2, 3)

  //   for (t <- respTitle; d <- respDepth) {
  //     println(s"crawling $t with depth: $d")
  //     val (result1, time1) = Main.profile(Main.fetchAllLinks(t, d))
  //     val (result2, time2) = Main.profile(Main.fetchAllLinksPar(t, d))
  //     println(s"sequential crawling using $time1 ms")
  //     println(s"par crawlling using $time2 ms")
  //     assertEquals(result1, result2)
  //   }

  // }
}
