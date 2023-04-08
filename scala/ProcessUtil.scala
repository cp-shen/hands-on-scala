object ProcessUtil {
  object GitUtil {
    def gitStatus() = {
      os.proc("git", "status").call()
    }

    def listBranches() = {
      os.proc("git", "branch").call().out.lines().map(_.drop(2))
    }

    def deleteBranch(br: String) = {
      val branches = listBranches()
      branches.find(_.equals(br)) match {
        case None     => { println(s"branch $br not found") }
        case Some(br) => { os.proc("git", "branch", "-D", br).call() }
      }
      ()
    }

    def getContributors(p: os.Path = os.pwd) = {
      val gitLog = os.proc("git", "log").spawn(cwd = p)
      val grepAuthor =
        os.proc("grep", "Author: ").spawn(stdin = gitLog.stdout, cwd = p)
      val output = grepAuthor.stdout.lines().distinct
      output
    }
  }

  object CurlUtil {
    def downloadReleaseJson(author: String, repo: String): Unit = {
      //   val url = s"https://api.github.com/repos/$author/$repo/releases"
      var url = "https://api.github.com/repositories/107214500/releases"
      var jsonFilePath = os.pwd / "github.json"
      var jsonGzipPath = os.pwd / "github.json.gz"
      os.proc("curl", url).call(stdout = jsonFilePath)
      os.proc("gzip").call(stdin = jsonFilePath, stdout = jsonGzipPath)
    }
  }
}
