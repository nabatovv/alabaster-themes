// Full Kotlin syntax sample.
// Covers imports/aliases, annotations, classes (open/abstract/final/sealed/data/value/inner/enum),
// objects & companions, interfaces, constructors/init, properties, extensions, lambdas, higher-order
// functions, function types with receivers, inline/reified/noinline/crossinline, contracts, generics
// & variance, delegation, type aliases, null-safety & casts, destructuring, ranges, when/if/loops,
// exceptions, operator overloading, infix/tailrec/suspend/context parameters, strings & interpolation,
// numbers, arrays/collections/sequences and Java interop. Standard library only (Kotlin 2.x).
package sample
import java.math.BigDecimal
import java.time.LocalDate
import java.util.ArrayList as JList
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.properties.Delegates

// ----- Annotations -----
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Marker(val name: String = "untagged")
enum class Level { LOW, HIGH }
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Tag(val level: Level)
@RequiresOptIn(message = "This API is a preview.")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class PreviewApi
@PreviewApi fun experimentalFeature(): Int = 42
@OptIn(PreviewApi::class) fun usesPreview(): Int = experimentalFeature()
fun tagged(@Tag(Level.HIGH) value: Int): Int = value

// ----- Classes, constructors, properties -----
@Marker("base")
open class Base(val id: Int) {
    open val label: String = "base"
    open fun describe(): String = "$label($id)"
}
abstract class Shape { abstract fun area(): Double; open fun name(): String = "shape" }
final class Square(private val side: Double) : Shape() { override fun area(): Double = side * side; override fun name(): String = "square" }
sealed class Result<out T> {
    data class Ok<T>(val value: T) : Result<T>()
    data class Err(val message: String) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}
data class Point(val x: Int, val y: Int)
@JvmInline
value class Meters(val value: Double)
class Outer(private val outerValue: Int) {
    class Nested { fun text(): String = "nested" }
    inner class Inner { fun combined(): Int = outerValue + 1 }
}
class Widget(val name: String) {
    var size: Int
    init { size = name.length }
    constructor(name: String, size: Int) : this(name) { this.size = size }
}
class Thermometer(temp: Double) {
    var celsius: Double = temp
        get() = field
        set(value) { field = value.coerceIn(-273.15, 1000.0) }
    val fahrenheit: Double
        get() = celsius * 9.0 / 5.0 + 32.0
}
enum class Planet(val mass: Double, val radius: Double) {
    EARTH(5.97e24, 6.37e6),
    MARS(6.42e23, 3.39e6);

    val surfaceGravity: Double
        get() = 6.674e-11 * mass / (radius * radius)
    fun label(): String = name.lowercase()
}
object Config {
    const val VERSION: String = "2.0"
    @JvmStatic fun info(): String = "version=$VERSION"
}
class Counter private constructor() {
    companion object Factory {
        @JvmField val START: Int = 0
        fun create(): Counter = Counter()
    }
}

// ----- Interfaces, lateinit / lazy / const -----
interface Greeter {
    val greeting: String            // abstract property
    fun greet(): String             // abstract member
    fun farewell(): String = "Bye"  // default member
}
interface Producer<out T> { fun produce(): T }
interface Consumer<in T> { fun consume(item: T) }
const val MAX_SIZE: Int = 100
class Service {
    lateinit var endpoint: String
    val client: List<String> by lazy { listOf("alpha", "beta") }
    fun setUp() { endpoint = "https://example.com" }
}

// ----- Extensions, higher-order functions, lambdas, receivers -----
fun String.shout(): String = uppercase() + "!"
val String.firstChar: Char?
    get() = firstOrNull()
fun Int?.orZero(): Int = this ?: 0
fun applyTwice(value: Int, block: (Int) -> Int): Int = block(block(value))
fun buildText(builder: StringBuilder.() -> Unit): String {
    val sb = StringBuilder()
    sb.builder()
    return sb.toString()
}
inline fun <reified T> typeName(): String = T::class.simpleName ?: "unknown"
inline fun <T> measure(noinline before: () -> Unit, crossinline action: () -> T): T {
    before()
    return action()
}
@OptIn(ExperimentalContracts::class)
fun requireNonNull(value: String?): String {
    contract { returns() implies (value != null) }
    return value ?: error("value must not be null")
}

// ----- Null-safety, casts, smart casts -----
fun lengthOrZero(text: String?): Int {
    val safe = text?.length ?: 0
    if (text != null) return text.length   // smart cast
    return safe
}
fun forcedLength(text: String?): Int = text!!.length
fun asList(value: Any): List<*>? = value as? List<*>
fun classify(value: Any): String =
    if (value is String) "text" else if (value !is Int) "not-int" else "int"

// ----- Destructuring, ranges, when, loops -----
fun destructure() {
    val (x, y) = Point(1, 2)
    val (word, count) = "Earth" to 3
    println("$x,$y,$word,$count")
}
val oneToTen = 1..10; val stepped = 0..20 step 2
val countingDown = 10 downTo 1; val exclusive = 1 until 5
fun containsN(n: Int): Boolean = n in oneToTen
fun whenSubject(day: Int): String = when (day) {
    0, 6 -> "weekend"
    in 1..5 -> "weekday"
    else -> "unknown"
}
fun whenGuards(value: Any): String = when (value) {
    is String if value.isNotEmpty() -> "non-empty string"
    is String -> "empty string"
    is Int if value > 0 -> "positive int"
    else -> "other"
}
fun subjectless(n: Int): String = when {
    n < 0 -> "negative"
    n == 0 -> "zero"
    else -> "positive"
}
fun maximum(a: Int, b: Int): Int = if (a >= b) a else b
fun loops() {
    outer@ for (i in 1..3) {
        for (j in 1..3) {
            if (j == 2) continue
            if (i == 3) break@outer
            println("$i,$j")
        }
    }
    var k = 0
    while (k < 3) k++
    do { k-- } while (k > 0)
}

// ----- Exceptions -----
class ValidationException(message: String) : Exception(message)
fun validate(age: Int): Int = try {
    if (age < 0) throw ValidationException("negative age")
    age
} catch (e: ValidationException) {
    println(e.message); 0
} finally {
    println("validation done")
}

// ----- Operator overloading, infix, tailrec -----
class Vector2(val x: Int, val y: Int) {
    operator fun plus(other: Vector2) = Vector2(x + other.x, y + other.y)
    operator fun get(index: Int): Int = if (index == 0) x else y
    operator fun invoke(scale: Int) = Vector2(x * scale, y * scale)
    operator fun compareTo(other: Vector2): Int = (x * x + y * y) - (other.x * other.x + other.y * other.y)
    operator fun rangeTo(other: Vector2) = IntRange(x, other.x)
    operator fun contains(value: Int): Boolean = value in x..y
}
class Countdown(private val start: Int) {
    operator fun iterator(): Iterator<Int> = object : Iterator<Int> {
        private var current = start
        override fun hasNext(): Boolean = current >= 0
        override fun next(): Int = current--
    }
}
infix fun Int.repeated(word: String): String = word.repeat(this)
tailrec fun factorial(n: Int, acc: Long = 1L): Long =
    if (n <= 1) acc else factorial(n - 1, acc * n)

// ----- Suspend & context parameters -----
suspend fun fetchValue(): Int = 42
suspend fun runSuspending(): Int = fetchValue() + fetchValue()
class Logger { fun log(message: String) = println("[log] $message") }
context(logger: Logger)
fun logTwice(message: String) {
    logger.log(message)
    logger.log(message)
}

// ----- Delegation & type aliases -----
interface Printer { fun print(message: String) }
class ConsolePrinter : Printer { override fun print(message: String) = println(message) }
class LoggingPrinter(private val delegate: Printer) : Printer by delegate
class Settings {
    var enabled: Boolean by Delegates.observable(false) { _, old, new ->
        println("changed $old -> $new")
    }
    val lazyValue: String by lazy { "computed" }
}
typealias StringMap<V> = Map<String, V>
typealias Handler = (String) -> Unit

// ----- Sealed hierarchy, exhaustive when -----
fun render(result: Result<Int>): String = when (result) {
    is Result.Ok<*> -> "ok: ${result.value}"
    is Result.Err -> "err: ${result.message}"
    Result.Loading -> "loading"
}

// ----- Generics, variance, star projections -----
fun <T> largest(a: T, b: T): T where T : Comparable<T> = if (a > b) a else b
class Repository<T> where T : Comparable<T> {
    private val items = mutableListOf<T>()
    fun add(item: T) { items.add(item) }
}
fun printAll(items: List<*>) { items.forEach { println(it) } }
fun copyInto(source: Collection<Number>, dest: MutableList<in Number>) {
    source.forEach { dest.add(it) }
}
@Deprecated("Use describe instead", ReplaceWith("describe()"))
fun oldDescribe(): String = "old"
@Suppress("UNUSED_PARAMETER", "unused")
fun suppressed(unused: Int) {}

// ----- Strings, chars, numbers -----
fun strings(name: String, items: List<String>): String {
    val greeting = "Hi, $name! You have ${items.size} items."
    val escaped = "tab:\t newline:\n quote:\" backslash:\\"
    val raw = """
        |Line one for $name
        |Line two
    """.trimMargin()
    val ch: Char = '\n'
    val unicode: Char = '\u00A9'
    val multiDollar = $$"Price: $5.00, count via interpolation: $${items.size}"
    val schema = $$"""
        { "$schema": "https://example.com", "count": $${items.size} }
    """.trimIndent()
    return greeting + escaped + raw + ch + unicode + multiDollar + schema
}
val hex = 0xFF; val binary = 0b1010; val underscored = 1_000_000
val uByte: UByte = 200u.toUByte(); val uInt: UInt = 3_000_000u; val uLong: ULong = 9_000_000_000uL
val price: BigDecimal = BigDecimal("19.99"); val total: BigDecimal = price.multiply(BigDecimal("3"))

// ----- Arrays, collections, sequences -----
val intArray: IntArray = intArrayOf(1, 2, 3)
val namesList: List<String> = listOf("alpha", "beta", "gamma")
val mutableNumbers: MutableList<Int> = mutableListOf(1, 2, 3)
val lookup: StringMap<Int> = mapOf("one" to 1, "two" to 2)
fun collectionOps(): Map<Int, List<Int>> {
    val processed = namesList.filter { it.isNotEmpty() }.map { it.uppercase() }.associateWith { it.length }
    val totalLen = namesList.fold(0) { acc, s -> acc + s.length }
    val grouped = listOf(1, 2, 3, 4, 5).groupBy { it % 2 }
    val zipped = namesList.zip(listOf(1, 2, 3))
    val sequence = generateSequence(1) { it * 2 }.take(5).toList()
    println(processed); println(totalLen); println(zipped); println(sequence)
    return grouped
}

// ----- Java interop -----
fun javaInterop() {
    val today: LocalDate = LocalDate.now()
    val year = today.year
    val list = JList<Int>()
    list.add(1); list.add(2)
    println("year=$year list=$list")
}
fun main() {
    println(render(Result.Ok(7)))
    println(Planet.EARTH.surfaceGravity)
    println(strings("World", namesList))
    println(collectionOps())
    for (c in Countdown(3)) print(c)
    println()
    javaInterop()
    println((Vector2(1, 2) + Vector2(3, 4)) is Vector2)
    println(Meters(9.5).value + factorial(5).toInt() + usesPreview())
}
