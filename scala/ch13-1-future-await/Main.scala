//> using scala "2.13"
//> using jvm "system"
//> using deps "at.favre.lib:bcrypt:0.10.2"
//> using deps "com.lihaoyi::os-lib::0.9.1"
//> using repository "https://maven.aliyun.com/repository/public/"

import at.favre.lib.crypto.bcrypt.BCrypt
import at.favre.lib.crypto.bcrypt.LongPasswordStrategies
import scala.concurrent._, duration.Duration.Inf, ExecutionContext.Implicits.global
import java.lang.System.nanoTime

object Main extends App {
  def profile[R](code: => R, t: Long = nanoTime) =
    (code, (nanoTime - t) / 1000 / 1000) // convert nanotime to milisec

  def hash(file: os.Path) = {
    val salt = file.last.take(16).padTo(16, ' ').getBytes
    val bytes =
      hasher.hash( /*difficulty*/ 17, salt, os.read.bytes(file))
    new String(bytes)
  }

  val hasher =
    BCrypt.`with`(LongPasswordStrategies.hashSha512(BCrypt.Version.VERSION_2A))

  val v = Vector.newBuilder[String]

  println(
    profile {
      val f1 = Future {
        hash(os.pwd / os.up / os.up / "resources" / "13" / "Chinatown.jpg")
      }
      val f2 = Future {
        hash(os.pwd / os.up / os.up / "resources" / "13" / "ZCenter.jpg")
      }
      v += Await.result(f1, Inf)
      v += Await.result(f2, Inf)
    }._2
  )

  v.result().foreach(println)
}
