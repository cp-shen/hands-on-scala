object ComprehensionExample {
  val a = Array(1, 2, 3, 4)
  val a2 = for (i <- a) yield i * i
  val b = Array("hello", "world")
  val hw = for (i <- a; s <- b) yield s + i

  a2.foreach(println)
  hw.foreach(println)
}

object MethodsExample {
  def hello(times: Int) = {
    println("hello " + times)
    "hello"
  }
}

object FunctionExample {
  val add_one: Int => Int = i => i + 1
}

object CollectionExample {
  val v = Vector(1, 2, 3)
  val v1 = 1 +: v
  val v2 = 1 +: v :+ 2
  println(v2)

  val s1 = Set(1, 2, 3)
  val s2 = Set(2, 3, 4)
  println(s1 ++ s2 - 2)

  val aqb = collection.mutable.ArrayDeque.newBuilder[Int]
  aqb.sizeHint(10)
  aqb += (1, 2, 3, 4, 5)

  val aq = aqb.result()
  aq.removeHead()
  aq.append(6)
  aq.removeHead()
  println(aq)
}

object CollectionInterfaceExample {
  def iterateOverSth[T](items: Seq[T]) = {
    for (i <- items) println(i)
  }

  def getTwoAndFour[T](items: IndexedSeq[T]) = {
    (items(2), items(4))
  }
}

object PatternMatchExample {
  case class Person(name: String, title: String)
  def greet(p: Person) = p match {
    case Person(s"$firstName $lastName", title) =>
      println(s"Hello $title $lastName")
    case Person(name, title) => println(s"Hello $title $name")
  }
  greet(Person("Haoyi Li", "Mr"))
  greet(Person("Who?", "Dr"))

  def greet2(husband: Person, wife: Person) = (husband, wife) match {
    case (Person(s"$first1 $last1", _), Person(s"$first2 $last2", _))
        if last1 == last2 =>
      println(s"Hello Mr and Ms $last1")
    case _ =>
      println("Hello")
  }

  greet2(Person("James Bond", "Mr"), Person("Jane Bond", "Ms"))
  greet2(Person("James Bond", "Mr"), Person("Jane", "Ms"))
}

object ByNameParamExample {
  def measureTime(f: => Unit) = {
    val start = System.currentTimeMillis()
    f
    val end = System.currentTimeMillis()
    println("Eval took " + (end - start) + " millisenconds")
  }

  measureTime(new Array[String](10 * 1000 * 1000).hashCode())
  measureTime(new Array[String](100 * 1000 * 1000).hashCode())
  measureTime(new Array[String](1000 * 1000 * 1000).hashCode())
}

object ImplicitParamExample {
  case class Foo(val value: Int)
  def bar(implicit foo: Foo) = foo.value + 10

  implicit val fooo = Foo(1)
  println(bar)
}
