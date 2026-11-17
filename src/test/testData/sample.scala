// Full Scala syntax sample.
// Coverage note: a Scala 3.7 syntax sweep for the theme's highlighting tests:
// package/import/export clauses; vals/vars/lazy vals; every literal kind
// (decimal, hex, binary, octal, underscored, boolean, char, s/f/raw
// interpolation, triple-quoted); type aliases; opaque types; case/abstract/
// final/sealed classes; objects and companions; simple, parameterised and
// generic enums; traits with self-types and mixins; generics (variance,
// bounds, context bounds with `as`, type lambdas); given/using; extensions;
// given Conversion; match types; union/intersection types; dependent and
// polymorphic function types; inline/compiletime/erased; derives; every
// pattern form; for-comprehensions; control flow; collections; tuples; Scala
// 3.7 named tuples; Option/Either/Try; operator overloading; infix, prefix and
// postfix syntax; annotations; indentation and braces syntax; Java interop.
package alabaster.showcase.scala3
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicLong
import java.util.{ArrayList as JArrayList, List as JList}
import scala.annotation.{nowarn, switch, tailrec}
import scala.compiletime.{constValue, erasedValue}
import scala.compiletime.ops.int.*
import scala.jdk.CollectionConverters.*
import scala.language.{implicitConversions, postfixOps}
import scala.util.{Failure, Success, Try, Using}
type StringOrInt = String | Int
type StringEither = [X] =>> Either[String, X]
type Element[X] = X match
  case String => Char
  case Array[t] => t
  case Iterable[t] => t
type IsZero[N <: Int] = N match
  case 0 => true
  case _ => false
type InnerSelector = (o: Outer) => o.Inner
type PolyIdentity = [A] => A => A
trait Outer:
  type Inner
  def inner: Inner

object Literals {
  val decimal: Int = 1_000_000
  val hexadecimal: Int = 0xCAFE_BABE
  val binary: Long = 0b1010_1010L
  val octal: Int = 0o755
  val simpleDouble: Double = 3.14159
  val singleFloat: Float = 2.5f
  val truthy: Boolean = true && !false
  val aChar: Char = 'A'
  val escapedChar: Char = '\n'
  val plainString: String = "tab\there, newline\n"
  val rawString: String = raw"C:\temp\nested"
  val name: String = "Scala"
  val major: Int = 3
  val interpolated: String = s"$name $major (${name.length} letters)"
  val formatted: String = f"$name%s $major%04d, pi = ${math.Pi}%.3f"
  val block: String =
    """|first line
       |second line
       |third line""".stripMargin
  val quoting: String = """He said "quotes" stay put."""
  val nullable: String | Null = null
  lazy val lazyName: String = name.toUpperCase
}

object Identifiers:
  opaque type UserId = Long
  object UserId:
    def apply(value: Long): UserId = value
  extension (id: UserId)
    def raw: Long = id
    def isNobody: Boolean = id == 0L
  def newUser(): UserId = UserId(42L)

final case class Point(x: Double, y: Double) derives CanEqual:
  def +(that: Point): Point = Point(x + that.x, y + that.y)
  def *(factor: Double): Point = Point(x * factor, y * factor)
  def unary_- : Point = Point(-x, -y)
  infix def distanceTo(that: Point): Double =
    math.sqrt(math.pow(x - that.x, 2) + math.pow(y - that.y, 2))
object Point:
  def parse(text: String): Option[Point] = text.split(',').toList match
    case List(xs, ys) => for x <- xs.toDoubleOption; y <- ys.toDoubleOption yield Point(x, y)
    case _ => None

abstract class Shape(val name: String):
  def area: Double
  def describe: String = s"$name with area ${f"$area%.2f"}"
final case class Rectangle(width: Double, height: Double) extends Shape("rectangle") {
  def area: Double = width * height
}
final case class Circle(radius: Double) extends Shape("circle") {
  def area: Double = math.Pi * radius * radius
}

sealed trait Expr
object Expr:
  final case class Lit(value: Int) extends Expr
  final case class Sum(left: Expr, right: Expr) extends Expr
  case object Zero extends Expr
  def evaluate(expr: Expr): Int = expr match
    case Lit(value) => value
    case Sum(left, right) => evaluate(left) + evaluate(right)
    case Zero => 0

enum Direction:
  case North, South, East, West
  def isVertical: Boolean = this match
    case North | South => true
    case East | West => false
enum Color(val rgb: Int):
  case Red extends Color(0xFF0000)
  case Green extends Color(0x00FF00)
  case Blue extends Color(0x0000FF)
  def hex: String = f"#${rgb}%06x"
enum Tree[A]:
  case Leaf(value: A)
  case Branch(left: Tree[A], right: Tree[A])
  def depth: Int = this match
    case Leaf(_) => 1
    case Branch(left, right) => 1 + math.max(left.depth, right.depth)
trait Logging { def log(message: String): Unit }
trait Auditable { def audit(event: String): Unit }
trait Service:
  self: Logging & Auditable =>
  def run(task: String): Unit =
    log(s"starting $task")
    record(task)
  def record(event: String): Unit = audit(s"[${LocalDate.now()}] $event")
final class ConsoleService extends Service with Logging with Auditable {
  def log(message: String): Unit = println(s"LOG $message")
  def audit(event: String): Unit = println(s"AUDIT $event")
}
trait Source[+A] { def get: A }
trait Sink[-A] { def put(value: A): Unit }
final class Box[+A](val value: A) { def map[B](f: A => B): Box[B] = Box(f(value)) }
def widest[A <: Comparable[A]](values: List[A]): A = values.reduce((l, r) => if l.compareTo(r) >= 0 then l else r)
def prepend[A, B >: A](value: B, values: List[B]): List[B] = value :: values
def sumOf[A](values: List[A])(using numeric: Numeric[A]): A = values.foldLeft(numeric.zero)(numeric.plus)
def biggest[A: Ordering as ord](values: List[A]): A = values.max(ord)

trait Functor[F[_]] { def map[A, B](fa: F[A])(f: A => B): F[B] }
trait Show[A] { def show(value: A): String }
object Show:
  def apply[A](using instance: Show[A]): Show[A] = instance
  given Show[Int] with
    def show(value: Int): String = s"Int($value)"
  given Show[String] = (value: String) => s"String($value)"
  given [A](using showA: Show[A]): Show[List[A]] =
    (value: List[A]) => value.map(showA.show).mkString("List(", ", ", ")")
  given Show[Point] with
    def show(value: Point): String = s"Point[${value.x}, ${value.y}]"
given Functor[List] = new Functor[List] { def map[A, B](fa: List[A])(f: A => B) = fa.map(f) }
given Functor[StringEither] = new Functor[StringEither] { def map[A, B](fa: Either[String, A])(f: A => B) = fa.map(f) }
def renderAll[A](values: List[A])(using Show[A]): String = values.map(Show[A].show).mkString(" | ")

final case class Celsius(degrees: Double)
final case class Fahrenheit(degrees: Double)
given Conversion[Celsius, Fahrenheit] with
  def apply(celsius: Celsius): Fahrenheit = Fahrenheit(celsius.degrees * 9.0 / 5.0 + 32.0)
object Legacy { implicit def stringToChars(text: String): List[Char] = text.toList }
def toFahrenheit(celsius: Celsius): Fahrenheit = celsius

object Extensions:
  extension (text: String)
    def shout: String = text.toUpperCase + "!"
    def bracketed: String = "[" + text + "]"
  extension [A](values: List[A])
    def secondOr(fallback: => A): A = values.drop(1).headOption.getOrElse(fallback)
  extension [A: Numeric](values: List[A])
    def total: A = values.sum
object CompileTime:
  inline def power(x: Double, inline n: Int): Double =
    inline if n == 0 then 1.0
    else x * power(x, n - 1)
  inline def literal[N <: Int & Singleton]: N = constValue[N]
  transparent inline def widen[T](value: T): T = value
  inline def defaultValue[T]: T = inline erasedValue[T] match
    case _: Int => 0.asInstanceOf[T]
    case _: Boolean => false.asInstanceOf[T]
    case _ => null.asInstanceOf[T]
  type Plus[A <: Int, B <: Int] = A + B
  erased def trace(message: String): Unit = ()
object Annotations:
  @tailrec def factorial(n: BigInt, accumulator: BigInt = 1): BigInt =
    if n <= 1 then accumulator else factorial(n - 1, n * accumulator)
  @switch def digit(c: Char): Int = c match { case '0' => 0; case _ => -1 }
  @nowarn("msg=never used") private val unused: Int = 0
  @deprecated("use Point(0, 0) instead", "3.7") def legacyOrigin: Point = Point(0.0, 0.0)
  @volatile private var state: Int = 0

object Matching:
  def classify(n: Int): String = n match
    case 0 => "zero"
    case value if value < 0 => "negative"
    case value if value % 2 == 0 => "positive even"
    case _ => "positive odd"
  def describeAny(value: Any): String = value match
    case text: String => s"string of length ${text.length}"
    case number: Int => s"int $number"
    case _ => "other"
  def headTail(values: Seq[Int]): String = values match
    case Nil => "empty"
    case head +: tail => s"head=$head tail=${tail.size}"
  def entries(map: Map[String, Int]): String = map match
    case Map() => "no entries"
    case Map("a" -> a) => s"a=$a"
    case Map("a" -> a, "b" -> b) => s"a=$a, b=$b"
  def expr(expr: Expr): String = expr match
    case whole @ Expr.Sum(_, Expr.Zero) => s"branch: $whole"
    case Expr.Lit(value) => s"literal $value"
    case Expr.Zero => "zero"
  def optional(value: Option[Int]): String = value match
    case Some(0) => "some zero"
    case Some(n) => s"some $n"
    case None => "none"
  def attempt(value: Try[Int]): String = value match
    case Success(n) => s"success $n"
    case Failure(exception) => s"failure ${exception.getMessage}"
  def union(value: StringOrInt): String = value match
    case text: String => s"text: $text"
    case number: Int => s"number: $number"
object Control:
  @tailrec def gcd(a: Int, b: Int): Int = if b == 0 then a else gcd(b, a % b)
  def countdown(n: Int): Unit = {
    var i = n
    while i > 0 do { println(if i % 2 == 0 then s"$i is even" else s"$i is odd"); i -= 1 }
  }
  def sumUntil(n: Int): Int = {
    var i = 0; var total = 0
    do { total += i; i += 1 } while (i < n)
    total
  }
  def parseInt(text: String): Either[String, Int] =
    try Right(text.toInt)
    catch case error: NumberFormatException => Left(s"not a number: ${error.getMessage}")
    finally println("parse attempt finished")
  def positive(n: Int): Int = if n < 0 then throw new IllegalArgumentException(s"negative: $n") else n

object Collections:
  val numbers: List[Int] = List(1, 2, 3, 4, 5, 6)
  val words: Vector[String] = Vector("alpha", "beta", "gamma")
  val ages: Map[String, Int] = Map("Ada" -> 36, "Grace" -> 85)
  val primes: Set[Int] = Set(2, 3, 5, 7, 11)
  def pipeline(values: List[Int]): Map[Boolean, List[Int]] =
    values.map(_ * 2).filter(_ > 4).flatMap(n => List(n, -n))
      .collect { case n if n % 3 == 0 => n / 3 }.groupBy(_ >= 0)
  def total(values: List[Int]): Int = values.fold(0)(_ + _)
  def pairs(xs: List[Int], ys: List[Int]): List[(Int, Int)] =
    for
      x <- xs if x % 2 == 0
      y <- ys if y > x
    yield (x, y)
  def sideEffects(values: List[Int]): Unit = for value <- values do println(value)
object Functions:
  val pair: (String, Int) = ("answer", 42)
  val (key, value) = pair
  val named: (name: String, age: Int) = (name = "Ada", age = 36) // Scala 3.7 named tuple
  val namedAge: Int = named.age
  val add: (Int, Int) => Int = (a, b) => a + b
  def curry(a: Int)(b: Int): Int = a + b
  val addFive: Int => Int = curry(5)
  def applyTwice[A](f: A => A, a: A): A = f(f(a))
  def repeat(n: Int)(body: => Unit): Unit = (1 to n).foreach(_ => body)
  def sumAll(values: Int*): Int = values.sum
  def greet(name: String = "world", punctuation: String = "!"): String = s"hello $name$punctuation"
  val greeting: String = greet(punctuation = "?", name = "Scala")
object Operators:
  val moved: Point = Point(1, 1) + Point(2, 3) * 2.0
  val flipped: Point = -Point(3, 4)
  val distance: Double = Point(0, 0) distanceTo Point(3, 4)
  def postfixSize(values: List[Int]): Int = values size
object Strings {
  val upper: String = "  Alabaster  ".trim.toUpperCase
  val joined: String = List("x", "y", "z").mkString("-")
}

object JavaInterop:
  private val counter: AtomicLong = new AtomicLong(0L)
  def next(): Long = counter.incrementAndGet()
  def javaList(): JList[String] =
    val list = new JArrayList[String]()
    list.add("alpha")
    list.add("beta")
    list
  def asScala(list: JList[String]): List[String] = list.asScala.toList
  def readLength(): Try[Int] = Using(new java.io.StringReader("abc"))(_.read())
object NullSafety:
  def lengthOf(value: String | Null): Int = Option(value).fold(0)(_.length)
  def nonEmptyOr(value: Option[String], fallback: => String): String =
    value.map(_.trim).filter(_.nonEmpty).getOrElse(fallback)
  def safeDivide(a: Int, b: Int): Option[Int] = Option.when(b != 0)(a / b)
  def parseInt(text: String): Either[String, Int] = text.toIntOption.toRight("not an int")
object MathHelpers:
  def square(x: Double): Double = x * x
  def cube(x: Double): Double = x * x * x
export MathHelpers.{square, cube as cubed}
object ReExports { export Show.given }
@main def showcase(args: String*): Unit =
  println(Functions.greet(name = args.headOption.getOrElse("world")))
  println(Matching.classify(42))
  Collections.sideEffects(Collections.numbers.take(3))
