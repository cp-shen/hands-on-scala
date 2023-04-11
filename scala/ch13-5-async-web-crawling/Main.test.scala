import concurrent.duration.Duration
class MyTests extends munit.FunSuite {

  override val munitTimeout = Duration(200, "s")
  test("crawling test") {
    val respTitle = Vector("Singapore", "China")
    val respDepth = Vector(2, 3)

    for (t <- respTitle; d <- respDepth) {
      println(s"crawling $t with depth: $d")

      // val (result1, time1) = Main.profile(Main.fetchAllLinks(t, d))
      val (result2, time2) = Main.profile(Main.fetchAllLinksPar(t, d))
      val (result3, time3) = Main.profile(
        concurrent.Await.result(Main.fetchAllLinksAsync(t, d), Duration.Inf)
      )

      // println(s"sequential crawling using $time1 ms")
      println(s"par crawling using $time2 ms")
      println(s"async crawling using $time3 ms")

      // assertEquals(result1, result2)
      assertEquals(result2, result3)
    }

  }
}
