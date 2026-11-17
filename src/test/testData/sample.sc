// Full Scala syntax sample (Scala 3 script).
// Covers literals, vals/vars/lazy vals, enums, case classes, traits, objects,
// extension methods, given/using, opaque types, union/intersection/match types,
// pattern matching, for-comprehensions, control flow, collections, functions,
// inline/compiletime, annotations, string interpolation and Java interop.

import scala.annotation.tailrec
import scala.compiletime.constValue
import scala.jdk.CollectionConverters.*
import scala.util.{Try, Success, Failure, Using}

val decimal: Int = 1_000_000
var counter: Int = 0
lazy val expensive: Int = { counter += 1; counter }
val hexadecimal: Int = 0xDEAD_BEEF
val binary: Int = 0b1010_1100
val octal: Int = 0o755
val float: Double = 3.14159
val exponent: Double = 6.022e23
val character: Char = 'λ'
val boolean: Boolean = true
val greeting: String = "hello"
val interpolated: String = s"$greeting, ${decimal + 1}"
val formatted: String = f"$float%.2f"
val rawText: String = raw"no \n escapes"
val multiline: String = """line one
                         |line two""".stripMargin

enum Color(val hexCode: String):
  case Red extends Color("#f00")
  case Green extends Color("#0f0")
  case Blue extends Color("#00f")

  def describe: String = s"$productPrefix ($hexCode)"

case class Point(x: Double, y: Double):
  def +(other: Point): Point = Point(x + other.x, y + other.y)
  def distance: Double = math.hypot(x, y)

trait Describable:
  def describe: String
  def label: String = describe.toUpperCase

object Point:
  val origin: Point = Point(0, 0)
  def apply(value: Int): Point = Point(value.toDouble, value.toDouble)

extension (point: Point)
  def scaled(factor: Double): Point = Point(point.x * factor, point.y * factor)
  def manhattan: Double = math.abs(point.x) + math.abs(point.y)

given Ordering[Point] with
  def compare(left: Point, right: Point): Int = left.distance.compare(right.distance)

opaque type UserId = Long
object UserId:
  def apply(value: Long): UserId = value
  extension (id: UserId) def value: Long = id

type StringOrInt = String | Int
type Named = String & AnyRef
type Element[X] = X match
  case String => Char
  case Array[t] => t

inline def twice[A](value: A)(operation: A => A): A = operation(operation(value))
inline def compileTimeThree: Int = constValue[3]

def divide(a: Int, b: Int): Either[String, Int] =
  if b == 0 then Left("division by zero") else Right(a / b)

def classify(value: Any): String = value match
  case 0 => "zero"
  case n: Int if n < 0 => "negative"
  case s: String => s"string: $s"
  case point: Point => s"point: ${point.x}, ${point.y}"
  case Seq(first, _*) => s"sequence starting with $first"
  case _ => "other"

@tailrec
def factorial(n: BigInt, accumulator: BigInt = 1): BigInt =
  if n <= 1 then accumulator else factorial(n - 1, accumulator * n)

def largest[A: Ordering](values: List[A]): Option[A] =
  if values.isEmpty then None else Some(values.max)

def sum(values: Int*): Int = values.sum

val numbers: List[Int] = List(1, 2, 3, 4, 5)
val doubled = numbers.map(_ * 2)
val evens = numbers.filter(_ % 2 == 0)
val grouped = numbers.groupBy(_ % 2)
val total = numbers.foldLeft(0)(_ + _)
val zipped = numbers.zip(List("a", "b", "c", "d", "e"))
val flattened = List(List(1, 2), List(3)).flatten
val mapped: Map[String, Int] = numbers.map(n => s"n$n" -> n).toMap
val set: Set[String] = Set("a", "b", "a")

val pairs = for
  number <- numbers
  letter <- List("x", "y")
  if number % 2 == 0
yield s"$number$letter"

val option: Option[Int] = numbers.headOption
val either: Either[String, Int] = divide(10, 2)
val attempt: Try[Int] = Try("42".toInt)
val fallback = option.getOrElse(0)
val status = attempt match
  case Success(value) => s"ok: $value"
  case Failure(error) => s"error: ${error.getMessage}"

val point = Point(3, 4)
val moved = point + Point(1, 1)
val scaledPoint = point.scaled(2)
val user = UserId(42L)
val description = point match
  case Point(0, 0) => "origin"
  case Point(x, y) if x == y => s"diagonal at $x"
  case other => s"point at ${other.distance}"

val javaList = new java.util.ArrayList[String]()
javaList.add("alpha")
javaList.add("beta")
val scalaList = javaList.asScala.toList
val read: Try[Int] = Using(new java.io.StringReader("abc"))(_.read())
val now = java.time.LocalDate.now()

println(greeting)
println(interpolated)
println(formatted)
println(Color.Red.describe)
println(description)
println(pairs.mkString(", "))
println(total)
println(largest(numbers))
println(twice(2)(_ + 1))
println(compileTimeThree)
println(divide(10, 2))
println(factorial(10))
println(scalaList)
println(read)
println(now)
