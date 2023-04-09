object FileUtil {
  def findLargestFiveFiles = {
    os.walk(os.pwd)
      .filter(os.isFile)
      .map(p => (os.size(p), p))
      .sortBy(-_._1)
      .take(5)
  }

  def sync(src: os.Path, dest: os.Path) = {
    for (srcSubPath <- os.walk(src)) {
      val subPath = srcSubPath.subRelativeTo(src)
      val destSubPath = dest / subPath
      (os.isDir(srcSubPath), os.isDir(destSubPath)) match {
        case (false, true) | (true, false) =>
          os.copy.over(srcSubPath, destSubPath, createFolders = true)
        case (false, false)
            if !os.exists(destSubPath) || !os.read
              .bytes(srcSubPath)
              .sameElements(os.read.bytes(destSubPath)) =>
          os.copy.over(srcSubPath, destSubPath, createFolders = true)
        case _ =>
      }
    }
  }
}
