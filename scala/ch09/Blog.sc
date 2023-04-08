#!/usr/bin/env -S scala-cli shebang

import scalatags.Text.all._

println("rendering POSTS...")

val cwd = os.pwd / "scala" / "ch09"

val postInfo = os
  .list(cwd / "post")
  .map { p =>
    val s"$prefix - $suffix.md" = p.last
    (prefix, suffix, p)
  }
  .sortBy(_._1.toInt)

def mdNameToHtml(name: String) = name.replace(" ", "-").toLowerCase() + ".html"

val bootstrapCss = link(
  rel := "stylesheet",
  href := "https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.css"
)

os.remove.all(cwd / "out")
os.makeDir.all(cwd / "out" / "post")

os.write(
  cwd / "out" / "index.html",
  doctype("html")(
    html(
      head(bootstrapCss),
      body(
        h1("Blog"),
        for ((_, suffix, _) <- postInfo)
          yield h2(a(href := ("post/" + mdNameToHtml(suffix)), suffix))
      )
    )
  )
)

for ((_, suffix, path) <- postInfo) {
  val parser = org.commonmark.parser.Parser.builder().build()
  val document = parser.parse(os.read(path))
  val renderer = org.commonmark.renderer.html.HtmlRenderer.builder().build()
  val output = renderer.render(document)
  os.write(
    cwd / "out" / "post" / mdNameToHtml(suffix),
    doctype("html")(
      html(
        head(bootstrapCss),
        body(h1(a(href := "../index.html")("Blog"), " / ", suffix)),
        raw(output)
      )
    )
  )
}

def pushPosts() {
  val targetGitRepo = if (args.length >= 1) args(0) else ""
  val targetGitBranch = if (args.length >= 2) args(1) else "master"

  if (targetGitRepo != "") {
    os.proc("git", "init").call(cwd = cwd / "out")
    os.proc("git", "add", "-A").call(cwd = cwd / "out")
    os.proc("git", "commit", "-am", ".").call(cwd = cwd / "out")
    os.proc("git", "push", targetGitRepo, targetGitBranch, "-f")
      .call(cwd = cwd / "out")
  }
}

pushPosts()
