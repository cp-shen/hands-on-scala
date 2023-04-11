//> using scala "2.13"
//> using jvm "system"
//> using deps "org.scalameta::munit::1.0.0-M7"
//> using deps "at.favre.lib:bcrypt:0.10.2"
//> using deps "com.lihaoyi::os-lib::0.9.1"
//> using repository "central"
//> using repository "https://maven.aliyun.com/repository/public/"

import at.favre.lib.crypto.bcrypt.BCrypt
import at.favre.lib.crypto.bcrypt.LongPasswordStrategies
import scala.concurrent._, duration.Duration.Inf,
ExecutionContext.Implicits.global
import java.lang.System.nanoTime
import scala.util.Failure
import scala.util.Success

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

  val resPath = os.pwd / os.up / os.up / "resources" / "13"

  val (result, time) = profile {
    val futures = for (file <- os.list(resPath)) yield Future {
      println("Hashing " + file)
      hash(file)
    }
    val joined = futures.reduce((a, b) =>
      a.zip(b).map { case (hash1, hash2) =>
        s"$hash1\n$hash2"
      }
    )
    val joined2 = Future
      .sequence(futures)
      .map(_.mkString("\n"))
    joined.onComplete {
      case Failure(exception) => println(exception)
      case Success(value)     => println(value)
    }
    joined2.onComplete {
      case Failure(exception) => println(exception)
      case Success(value)     => println(value)
    }
    Await.ready(joined, atMost = Inf)
    Await.ready(joined2, atMost = Inf)
  }

  println(s"use $time ms")
}
