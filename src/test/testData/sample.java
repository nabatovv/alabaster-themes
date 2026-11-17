// Full Java syntax sample.
// Coverage: primitives, literals, operators, control flow, switch patterns, records, enums,
// annotations, sealed types, generics, exceptions, lambdas, streams, collections, concurrency.
import java.lang.annotation.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;
import java.util.regex.*;
import java.util.stream.*;
/** Syntax showcase meant to be compiled as a single file (no package declaration). */
final class Sample {
    // ---- annotations: custom @interface with @Retention, @Target, @Repeatable ----
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
    @Repeatable(Tags.class)
    @interface Tag { String value(); int priority() default 0; }
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
    @interface Tags { Tag[] value(); }
    @Tag("class-level") @Tag(value = "repeated", priority = 2)
    static final class Annotated {
        @Tag("field") private int value = 1;
        @Tag(value = "method", priority = 1) int value() { return value; }
    }
    // ---- enum with constructor, methods and constant-specific bodies ----
    enum Planet {
        MERCURY(3.303e+23, 2.4397e6),
        EARTH(5.976e+24, 6.37814e6) {
            @Override double surfaceGravity() { return super.surfaceGravity() + 0.1; }
        },
        MARS(6.421e+23, 3.3972e6);
        private static final double G = 6.67300E-11;
        private final double mass, radius;
        Planet(double mass, double radius) { this.mass = mass; this.radius = radius; }
        double mass() { return mass; }
        double surfaceGravity() { return G * mass / (radius * radius); }
    }
    // ---- sealed interface: abstract, default, static and private methods ----
    sealed interface Shape permits Circle, Rectangle, Square {
        double area();
        default boolean isLargerThan(Shape other) { return area() > other.area(); }
        static Shape unitSquare() { return new Square(1.0); }
        private String label() { return getClass().getSimpleName() + "=" + area(); }
    }
    record Circle(double radius) implements Shape {
        Circle { if (radius < 0) throw new IllegalArgumentException("radius < 0"); }   // compact
        @Override public double area() { return Math.PI * radius * radius; }
        static Circle unit() { return new Circle(1.0); }                // static factory
    }
    record Rectangle(double width, double height) implements Shape {
        Rectangle { if (width < 0 || height < 0) throw new IllegalArgumentException("size"); }
        @Override public double area() { return width() * height(); }
        @Override public double width() { return Math.abs(width); }     // custom accessor
    }
    static final class Square implements Shape {
        private final double side;
        Square(double side) { this.side = side; }
        @Override public double area() { return side * side; }
    }
    record Pair<A, B>(A first, B second) {                              // generic record
        static <T> Pair<T, T> of(T left, T right) { return new Pair<>(left, right); }
    }
    // ---- abstract and final classes, nested, inner, local, anonymous ----
    abstract static class Animal {
        abstract String sound();
        String speak() { return getClass().getSimpleName() + " says " + sound(); }
    }
    static final class Dog extends Animal { @Override String sound() { return "woof"; } }
    static class Nested {
        static int created;
        private final String label;
        Nested(String label) { this.label = label; created++; }
    }
    class Inner { String greet() { return "inner sees seed=" + seed; } }
    private int seed = 40;
    private static volatile int hits;
    private static final AtomicInteger LIVE = new AtomicInteger();
    static { hits = 5; }                    // static initializer
    { seed += 2; }                          // instance initializer
    // ---- custom checked and unchecked exceptions, AutoCloseable resource ----
    static class CheckedFailure extends Exception {
        CheckedFailure(String message, Throwable cause) { super(message, cause); }
    }
    static class UncheckedFailure extends RuntimeException {
        UncheckedFailure(String message) { super(message); }
    }
    static final class Resource implements AutoCloseable {
        private final String name;
        Resource(String name) { this.name = name; LIVE.incrementAndGet(); }
        String read() { return "data:" + name; }
        @Override public void close() { LIVE.decrementAndGet(); }
    }
    @FunctionalInterface
    interface Transformer<R> { R apply(String input); }
    // ---- all primitive types and literal forms ----
    static String primitives() {
        byte b = 0b0111_1111;                       // binary
        short s = -32_768;
        int dec = 1_000_000, hex = 0xFF_EC_D5, bin = 0b1010_0001, oct = 0755;
        long big = 9_223_372_036_854_775_807L;
        float f = 3.14_15F; double d = 6.022_140_76e23;
        char c = 'A', tab = '\t'; boolean flag = true;
        String escaped = "quote:\" backslash:\\ tab:" + tab + " unicode:\u00e9";
        String block = """
                {
                  "name": "java",
                  "stable": %s
                }""".formatted(flag);                // text block
        var sum = dec + hex + bin + oct;            // var
        return "%d %d %c %s %s %d %s %.2f %.1f %d".formatted(b, s, c, escaped, block, sum, big, d, f, c + 1);
    }
    // ---- arithmetic, bitwise, shift, comparison, logical, ternary operators ----
    static int operators(int x, int y) {
        int arithmetic = x + y - x * y / (y == 0 ? 1 : y) % 7;
        int bitwise = (x & y) | (x ^ y) & ~x;
        int shift = (x << 2) >> 1 >>> 1;
        boolean compare = x > y && y >= 0 || x == y != false;
        int ternary = x > y ? x : y;
        long promoted = (long) x + y;
        return arithmetic + bitwise + shift + (compare ? 1 : 0) + ternary + (int) promoted;
    }
    static String describe(Object o) {
        if (o instanceof String s && !s.isBlank()) return "string:" + s.strip();   // type pattern
        else if (o instanceof Integer i) return "int:" + i;
        else if (o instanceof Pair<?, ?>(var a, var b)) return "pair:" + a + "&" + b;
        return "other";
    }
    // ---- switch: record deconstruction, guards, arrow labels, yield ----
    static String classify(Shape shape) {
        return switch (shape) {
            case Circle(double r) when r > 10 -> "big circle";
            case Circle(double r) -> "circle:" + r;
            case Rectangle(double w, double h) when w == h -> "square-ish " + w;
            case Rectangle(var w, var h) -> "rect:" + w + "x" + h;
            case Square sq -> "square:" + sq.area();
        };
    }
    static int switchStatement(int day) {
        int result;
        switch (day) {
            case 1, 2, 3, 4, 5 -> result = 10;
            case 6, 7 -> { result = 20; }
            default -> result = -1;
        }
        return result;
    }
    static int switchExpression(int day) {
        return switch (day) {
            case 1, 2, 3, 4, 5 -> 10;
            case 6, 7 -> {
                int bonus = day * 2;
                yield 20 + bonus;                   // yield from a switch expression
            }
            default -> -1;
        };
    }
    // ---- for, enhanced for, while, do-while, labelled break and continue ----
    static int loops() {
        int total = 0;
        outer: for (int i = 0; i < 10; i++) {
            if (i == 3) continue outer;
            for (int j = 0; j < 10; j++) {
                if (j > i) continue outer;
                if (i * j > 20) break outer;
                total += i * j;
            }
        }
        for (var v : List.of(1, 2, 3)) total += v;      // enhanced for
        while (total % 7 != 0) total++;                  // while
        do { total--; } while (total % 5 != 0);          // do-while
        return total + IntStream.rangeClosed(1, 4).sum();
    }
    // ---- one-dimensional and multidimensional arrays ----
    static int arrays() {
        int[] empty = new int[3]; int[] init = {1, 2, 3, 4};               // array initializer
        int[][] grid = {{1, 2}, {3, 4}};
        int[][] jagged = new int[2][];
        jagged[0] = new int[] {9, 8, 7};
        String[] names = new String[] {"a", "b"};
        int sum = 0;
        for (int[] row : grid) for (int v : row) sum += v;
        return sum + empty.length + init.length + jagged[0].length + names.length;
    }
    // ---- generics: bounded type parameters, wildcards, generic methods, varargs ----
    static <T extends Comparable<T>> T largest(List<T> values, T fallback) {
        List<? extends Number> numbers = List.of(1, 2.5f);                 // wildcard
        return numbers.isEmpty() ? fallback : values.stream().max(Comparator.naturalOrder()).orElse(fallback);
    }
    static int varargs(String prefix, int... values) {                     // varargs
        return prefix.length() + Arrays.stream(values).sum();
    }
    // ---- throw, multi-catch, try-with-resources, finally, nested try ----
    static String exceptions(boolean fail) throws CheckedFailure {
        String result = null;
        try (Resource a = new Resource("a"); Resource b = new Resource("b")) {
            if (fail) throw new CheckedFailure("boom", new IllegalStateException("inner"));
            result = a.read() + "+" + b.read();
        } catch (CheckedFailure | UncheckedFailure e) {                // multi-catch
            result = "caught:" + e.getMessage();
        } catch (RuntimeException e) {
            throw new UncheckedFailure("wrapped: " + e.getMessage());
        } finally { hits++; }
        try {                                                          // nested try
            try {
                if (fail) throw new UncheckedFailure("nested");
                return result;
            } finally { hits += 10; }
        } catch (UncheckedFailure e) {
            return "bad:" + e.getMessage();
        }
    }
    // ---- lambdas, method references, functional interfaces ----
    static String functional() {
        Transformer<Integer> length = String::length;                  // unbound method reference
        Function<String, String> upper = String::toUpperCase;
        Supplier<List<String>> factory = ArrayList::new;               // constructor reference
        Comparator<String> byLength = Comparator.comparingInt(String::length)
                .thenComparing(Comparator.reverseOrder());
        List<String> data = factory.get();
        data.addAll(List.of("bb", "a", "ccc"));
        data.sort(byLength);
        Supplier<String> greeting = () -> "hi";                        // lambda
        return upper.apply(data.toString()) + length.apply("four") + greeting.get();
    }
    // ---- streams, Optional, immutable collection factories ----
    static String streamsAndCollections() {
        List<String> names = new ArrayList<>(List.of("ada", "grace", "alan"));
        Set<Integer> evens = Set.of(2, 4, 6, 8);
        Map<String, Integer> mutable = new HashMap<>(Map.of("ada", 36, "alan", 41)); mutable.put("grace", 45);
        String joined = names.stream().filter(n -> n.length() > 3).map(String::toUpperCase)
                .sorted(Comparator.reverseOrder()).collect(Collectors.joining(","));
        Optional<String> firstLong = names.stream().filter(n -> n.length() >= 4).findFirst();
        int total = evens.stream().mapToInt(Integer::intValue).sum()
                + mutable.values().stream().reduce(0, Integer::sum)
                + firstLong.map(String::length).orElse(0);
        return joined + "|" + total + "|" + mutable.getOrDefault("bob", 0) + "|" + names.contains("ada");
    }
    // ---- virtual threads, executor, CompletableFuture, atomic, synchronized ----
    static int concurrency() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger();
        Thread worker = Thread.ofVirtual().name("vt-1").start(() -> hits++); worker.join();
        Runnable task = counter::incrementAndGet;
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            for (int i = 0; i < 4; i++) pool.submit(task);
        }
        synchronized (Sample.class) { hits += counter.get(); }
        CompletableFuture<Integer> future = CompletableFuture
                .supplyAsync(() -> 21).thenApply(v -> v * 2);
        return future.join() + hits + LIVE.get();
    }
    // ---- java.time, regex, formatting and modern String methods ----
    static String textUtils(String text) {
        LocalDate date = LocalDate.of(2026, 1, 31).plusDays(1);
        Instant now = Instant.parse("2026-02-01T00:00:00Z"); Duration span = Duration.between(now, now.plusSeconds(90));
        Matcher matcher = Pattern.compile("(?<word>[A-Za-z]+)-(\\d+)").matcher(text);
        StringBuilder found = new StringBuilder();
        while (matcher.find()) found.append(matcher.group("word")).append(':').append(matcher.group(2)).append(' ');
        String raw = "  Hello, World  ";
        String modern = raw.strip() + "/" + raw.strip().toUpperCase().repeat(2) + "/"
                + String.join("-", "a", "b", "c") + "/" + "42".transform(s -> s + "!") + "/"
                + "    indented\n".indent(2).strip() + "/" + raw.lines().count() + "/"
                + "%.2f".formatted(3.14159) + "/" + "abc".compareTo("abd") + "/"
                + "x".isEmpty() + "/" + "Café".equalsIgnoreCase("CAFÉ");
        return date.format(DateTimeFormatter.ISO_DATE) + "+" + span.toMinutes() + "m|"
                + text.replaceAll("\\d+", "#") + "|" + found.toString().trim() + "|" + modern;
    }
    // ---- local class, anonymous class, assert ----
    static String localAndAnonymous(int value) {
        class Local {
            private final int factor;
            Local(int factor) { this.factor = factor; }
            int apply(int input) { return input * factor; }
        }
        Transformer<String> decorate = new Transformer<>() {            // anonymous class
            @Override public String apply(String input) { return "<" + input + ">"; }
        };
        int doubled = new Local(2).apply(value);
        assert doubled >= 0 : "result must not be negative";
        return decorate.apply(Integer.toString(doubled));
    }
    public static void main(String[] args) throws Exception {
        Sample sample = new Sample();
        System.out.println(primitives());
        System.out.println("ops=" + operators(6, 3) + " describe=" + describe(Pair.of("k", 1)));
        System.out.println(classify(Circle.unit()) + " " + classify(new Rectangle(3, 3)) + " "
                + classify(Shape.unitSquare()) + " " + switchStatement(6) + switchExpression(7));
        System.out.println("loops=" + loops() + " arrays=" + arrays() + " varargs=" + varargs("p", 1, 2, 3)
                + " generics=" + largest(List.of(3, 9, 4), 0));
        System.out.println(exceptions(false) + " " + exceptions(true) + " " + functional());
        System.out.println(streamsAndCollections() + " " + textUtils("item-42 other-7"));
        System.out.println("concurrency=" + concurrency() + " " + sample.new Inner().greet());
        System.out.println(localAndAnonymous(21) + " " + new Dog().speak() + " enum="
                + Planet.EARTH.surfaceGravity() + " annotations=" + Annotated.class.getAnnotations().length
                + " nested=" + new Nested("n").label + Nested.created);
    }
}
