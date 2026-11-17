// Full Groovy syntax sample.
// Covers packages + static imports, classes (abstract/final/inner/anonymous),
// interfaces with default methods, traits, enums with fields/methods, custom
// @interface annotations and @Immutable/@CompileStatic/@TypeChecked, properties
// with explicit getters/setters, closures (defaults, currying, composition,
// @DelegatesTo), GStrings, operators (safe-nav, Elvis, spread, ranges, spaceship,
// regex, identity, membership), switch (in/not-in, regex, class, closure, list and
// range cases), loops, exceptions + multi-catch, generics, varargs, collections,
// numbers, regex, java.time, metaprogramming (metaClass, methodMissing/
// propertyMissing, invokeMethod, Expando) and file/IO.
// Note: Groovy has no native `record` declaration (use @Immutable/@Canonical), so
// record syntax is intentionally omitted here.
package demo.groovy

import groovy.transform.CompileStatic
import groovy.transform.Immutable
import groovy.transform.TypeChecked
import groovy.lang.DelegatesTo
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import static java.lang.Math.PI
import static java.lang.Math.max

// ---- Custom annotations (@interface) with default values ----
@interface Marker { String value() default 'unmarked'; int priority() default 0 }
@interface Scheduled { String cron() }

// ---- Enum with fields, a constructor and methods ----
enum Priority {
    LOW(1, 'low'), MEDIUM(5, 'medium'), HIGH(10, 'high');
    final int weight; final String label
    Priority(int weight, String label) { this.weight = weight; this.label = label }
    boolean isUrgent() { weight >= 10 }
    static Priority byLabel(String lbl) { values().find { it.label == lbl } }
}
// ---- Trait with an abstract method, a property and concrete methods ----
trait Auditable {
    abstract String auditLabel()
    List<String> auditLog = []
    void record(String e) { auditLog << "${new Date().time}: $e" }
    String describe() { "Auditable(${auditLabel()}, entries=${auditLog.size()})" }
}
// ---- Interface with a default method ----
interface Greeter {
    String name()
    default String greet() { "Hello, ${name()}!" }
}
// ---- Abstract class implementing the interface ----
abstract class Shape implements Greeter {
    abstract double area()
    @Override String name() { getClass().simpleName }
    @Override String toString() { "${name()} area ${String.format('%.2f', area())}" }
}
// ---- Final class: auto property (radius) + explicit getter/setter (color) ----
final class Circle extends Shape implements Auditable {
    final double radius
    private String color = 'black'
    Circle(double radius) { assert radius >= 0: 'non-negative'; this.radius = radius }
    String getColor() { color }
    void setColor(String color) { this.color = color }
    @Override double area() { PI * radius ** 2 }
    @Override String auditLabel() { "circle:${radius}" }
}
// ---- Generics + varargs ----
class Box<T> {
    private final List<T> items = []
    Box<T> add(T... values) { items.addAll(values as List<T>); this }
    T first() { items ? items[0] : null }
    <U> List<U> map(Closure<U> fn) { items.collect { fn(it) } }
}
// ---- Dynamic behaviour: propertyMissing / methodMissing ----
class DynamicBag {
    private final Map<String, Object> store = [:]
    def propertyMissing(String n) { store[n] }
    def propertyMissing(String n, v) { store[n] = v }
    def methodMissing(String n, args) {
        if (n == 'shout') return "SHOUT: ${args.join(',')}"
        throw new MissingMethodException(n, getClass(), args)
    }
}
// ---- invokeMethod interception + delegation to the real method ----
class Interceptor {
    @Override
    def invokeMethod(String name, Object args) {
        if (name == 'ping') return 'pong'
        metaClass.getMetaMethod(name, args)?.invoke(this, args)
    }
}
// ---- Nested (static), inner and (below) anonymous classes ----
class Outer {
    static class Nested { String hi() { 'nested' } }
    class Inner { String hi() { "inner of ${Outer.simpleName}" } }
    String innerHi() { new Inner().hi() }
}
// ---- Custom exception ----
class ValidationException extends RuntimeException {
    final String field
    ValidationException(String field, String msg) { super(msg); this.field = field }
}
// ---- Static typing: @CompileStatic + @TypeChecked ----
@CompileStatic
class Calculator {
    int add(int a, int b) { a + b }
    @TypeChecked
    double average(List<Integer> values) {
        double total = 0
        for (int v : values) total += v
        total / values.size()
    }
}
// ---- @Immutable (auto constructors, equals, hashCode) ----
@Immutable
class Point { double x; double y }
// ---- Annotations in use on class, field and method ----
@Marker('demo')
@Scheduled(cron = '0 0 * * *')
class Job {
    @Marker(value = 'field', priority = 1)
    String title = 'nightly'
    @Marker('run')
    void run() { }
}
// ---- Delegate target for @DelegatesTo ----
class Config {
    String host = 'localhost'; int port = 8080
    void host(String h) { this.host = h }
    void port(int p) { this.port = p }
}

// =====================================================================
// Script body — executable tour (asserts document behaviour)
// =====================================================================
// GStrings: simple interpolation, ${expression}, multi-line triple-quoted
def name = 'Groovy', version = 5
assert "Hello $name" == 'Hello Groovy' && "v${version * 2}" == 'v10'
def multi = """Name : $name
Major: ${version}
Half : ${version / 2}"""
assert multi.contains('Half : 2.5')

// Numbers: BigDecimal by default, hex/binary, underscores, suffixes
def dec = 1.5, hex = 0xFF, bin = 0b1010_1010, grouped = 1_000_000, big = 42G, dbl = 2.0d
assert dec instanceof BigDecimal && hex == 255 && bin == 170 && grouped == 1_000_000
assert big instanceof BigInteger && dbl instanceof Double

// Operators: arithmetic, comparison, spaceship, regex, identity, membership
assert 2 + 3 * 4 - 10.intdiv(3) + (2 ** 3) == 19
assert (3 <=> 5) == -1 && (5 <=> 5) == 0 && (('abc123' =~ /\d+/) && ('abc123' ==~ /\w+\d+/))
def refA = name
assert refA.is(name) && 5 in 1..10 && !(5 in 20..30)

// Ranges: inclusive, exclusive, backwards
assert (1..5).toList() == [1, 2, 3, 4, 5] && (1..<5).toList() == [1, 2, 3, 4]
assert (5..1).toList() == [5, 4, 3, 2, 1] && ('a'..'e').size() == 5

// Safe navigation, Elvis, spread (*.) and spread-map (*:)
def maybe = null
assert maybe?.size() == null && (maybe ?: 'default') == 'default'
def people = [[name: 'Ada'], [name: 'Linus'], [name: null]]
assert people*.name == ['Ada', 'Linus', null] && ([*: [a: 1, b: 2], c: 3] == [a: 1, b: 2, c: 3])

// Closures: default param, implicit it, explicit params, currying, composition
def greet = { String who = 'world' -> "Hello, $who" }
assert greet() == 'Hello, world' && greet('dev') == 'Hello, dev'
assert [1, 2, 3].collect { it * 2 } == [2, 4, 6]
def add = { int a, int b -> a + b }
def twice = { n -> n * 2 }, plusOne = { n -> n + 1 }
assert add.curry(1)(41) == 42 && (twice >> plusOne)(5) == 11

// Delegation with @DelegatesTo (delegate-based builder)
def configure(@DelegatesTo(Config) Closure block) {
    def cfg = new Config()
    block.delegate = cfg
    block.resolveStrategy = Closure.DELEGATE_FIRST
    block.call()
    cfg
}
assert configure { host 'example.com'; port 9090 }.port == 9090

// switch: regex, closure, list membership, range and class cases
def classify = { Object v ->
    switch (v) {
        case ~/\d{3}-\d{4}/:  return 'phone'
        case { it instanceof String && it.startsWith('a') }: return 'a-string'
        case ['x', 'y']:      return 'list'
        case 10..20:          return 'range'
        case Integer:         return 'int'
        default:              return 'unknown'
    }
}
assert classify('555-1234') == 'phone' && classify(42) == 'int' && classify(15) == 'range'
assert classify('apple') == 'a-string' && classify('y') == 'list'
def bucket = { int n ->
    switch (n) {
        case { it in 1..5 && it !in 3..4 }: return 'tiny'
        case { it in 6..10 }:               return 'medium'
        default:                            return 'large'
    }
}
assert bucket(2) == 'tiny' && bucket(7) == 'medium' && bucket(99) == 'large'

// for-in, classic for, while, do-while
def squares = []
for (n in 1..3) squares << n * n
for (int i = 0; i < 3; i++) assert i in 0..2
def countdown = 3, ticks = []
while (countdown > 0) ticks << countdown--
def once = 0
do { once++ } while (once < 3)
assert squares == [1, 4, 9] && ticks == [3, 2, 1] && once == 3

// try/catch/finally, multi-catch and a custom exception
def log = []
try { throw new ValidationException('email', 'bad') }
catch (ValidationException e) { log << e.field }
finally { log << 'cleanup' }
try { Integer.parseInt('nope') }
catch (NumberFormatException | IllegalStateException e) { log << e.class.simpleName }
assert log.size() == 3

// Generics + varargs in action
def box = new Box<Integer>().add(1, 2, 3)
assert box.first() == 1 && box.map { it * 2 } == [2, 4, 6]

// Collections: List/Map/Set literals + each/collect/findAll/inject/groupBy
def list = [1, 2, 3, 4, 5]
def map = [name: 'Ada', age: 36]
def set = ['a', 'b', 'a'] as Set
assert set == ['a', 'b'] as Set
list.each { assert it > 0 }
assert list.findAll { it % 2 == 0 } == [2, 4]
assert list.collect { it * 3 } == [3, 6, 9, 12, 15]
assert list.inject(0) { acc, v -> acc + v } == 15
assert list.groupBy { it % 2 == 0 ? 'even' : 'odd' } == [odd: [1, 3, 5], even: [2, 4]]

// Map property access + JSON-like nested maps
assert map.name == 'Ada' && map['age'] == 36
map.role = 'admin'
def json = [user: [name: 'Ada', tags: ['math', 'code']], active: true, score: 9.5]
assert map.containsKey('role') && json.user.tags[0] == 'math'

// Sorting (natural and comparator, non-mutating), string methods, regex
def unsorted = [5, 3, 1, 4, 2]
assert unsorted.sort(false) == [1, 2, 3, 4, 5] && unsorted.sort(false) { a, b -> b <=> a } == [5, 4, 3, 2, 1]
def phrase = '  Hello Groovy World  '
assert phrase.trim().toLowerCase().contains('groovy') && 'a,b,c'.split(',').length == 3
assert 'racecar'.reverse() == 'racecar' && phrase.strip().tokenize().size() == 3
def m = 'order-42' =~ /order-(\d+)/
assert m[0][1] == '42' && ('v1.2.3' ==~ /v\d+\.\d+\.\d+/)

// java.time, enum behaviour, interface/trait/abstract dispatch
assert LocalDate.now().format(DateTimeFormatter.ISO_DATE) ==~ /\d{4}-\d{2}-\d{2}/
assert Priority.HIGH.isUrgent() && Priority.byLabel('medium') == Priority.MEDIUM
def circle = new Circle(2.0d)
assert circle.greet() == 'Hello, Circle!' && circle.describe().startsWith('Auditable(circle:2.0')

// Nested, inner and anonymous classes
def outer = new Outer()
assert new Outer.Nested().hi() == 'nested' && outer.innerHi() == 'inner of Outer'
def cmp = new Comparator<Integer>() { int compare(Integer a, Integer b) { a <=> b } }
assert cmp.compare(1, 2) < 0

// Static typing (@CompileStatic/@TypeChecked) and @Immutable
assert new Calculator().add(2, 3) == 5 && new Calculator().average([2, 4, 6]) == 4.0d
assert new Point(1.0d, 2.0d).x == 1.0d

// Metaprogramming: propertyMissing, methodMissing, invokeMethod, Expando, metaClass
def bag = new DynamicBag()
bag.color = 'blue'
assert bag.color == 'blue' && bag.shout('hi', 'there') == 'SHOUT: hi,there'
assert new Interceptor().ping() == 'pong' && max(3, 7) == 7
def counter = new Expando(count: 0)
counter.increment = { -> ++delegate.count }
counter.increment()
def dyn = new Job()
dyn.metaClass.describe = { -> "job:${title}" }
assert counter.count == 1 && dyn.describe() == 'job:nightly'

// ---- File / IO (side-effect free; uncomment to run) ----
// new File('data.txt').eachLine { line -> println line }
// def text = new URL('https://example.com').text
