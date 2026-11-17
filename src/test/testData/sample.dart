// Full Dart syntax sample.
// Covers classes, constructors, mixins, extensions, generics, null safety,
// async/await, streams, records, patterns, enums and collections.

import 'dart:async';
import 'dart:math' as math;

const int maxItems = 1024;
const String greeting = 'hello';
const bool enabled = true;
const double ratio = 1.5e3;

enum Color {
  red('R'),
  green('G'),
  blue('B');

  const Color(this.code);

  final String code;
}

mixin Describable {
  String describe() => 'instance of $runtimeType';
}

abstract class Shape with Describable {
  const Shape(this.name);

  final String name;

  double area();

  @override
  String toString() => '$name: ${area().toStringAsFixed(2)}';
}

class Circle extends Shape {
  const Circle(this.radius) : super('circle');

  final double radius;

  @override
  double area() => math.pi * radius * radius;
}

class Rectangle extends Shape {
  Rectangle(this.width, this.height) : super('rectangle');

  final double width;
  final double height;

  @override
  double area() => width * height;
}

class Stack<T> {
  final List<T> _items = [];

  void push(T value) => _items.add(value);

  T? pop() => _items.isEmpty ? null : _items.removeLast();

  int get size => _items.length;

  bool get isEmpty => _items.isEmpty;
}

extension PointMath on Point {
  Point operator +(Point other) => Point(x + x, y + other.y);
}

typedef Mapper<T, R> = R Function(T value);

class Point {
  const Point(this.x, this.y);

  final double x;
  final double y;

  @override
  String toString() => '($x, $y)';
}

T identity<T>(T value) => value;

Future<int> fetch(int id) async {
  await Future<void>.delayed(Duration.zero);
  return id * 2;
}

Stream<int> counter(int limit) async* {
  for (var i = 0; i < limit; i++) {
    yield i;
  }
}

String classify(Object? value) => switch (value) {
      0 => 'zero',
      int n when n < 0 => 'negative',
      String text => 'text $text',
      Point(x: final x, y: final y) => 'point $x/$y',
      _ => 'unknown',
    };

Future<void> main() async {
  final shapes = <Shape>[Circle(2), Rectangle(3, 4)];
  var total = 0.0;
  for (final shape in shapes) {
    total += shape.area();
  }

  final stack = Stack<String>()
    ..push('one')
    ..push('two');

  final point = const Point(1, 2) + const Point(3, 4);
  final mapper = <int>[1, 2, 3].map((value) => value * 2).toList();
  final record = (name: 'record', value: 42);
  final (name: recordName, value: recordValue) = record;

  final doubled = await fetch(21);
  final values = await counter(3).toList();

  final nullable = stack.pop();
  final length = nullable?.length ?? 0;

  try {
    if (length == 0) {
      throw StateError('empty stack');
    }
  } on StateError catch (error) {
    print('error: ${error.message}');
  } finally {
    print('finally');
  }

  final unique = {for (final shape in shapes) shape.name};
  final map = <String, int>{'a': 1, 'b': 2};

  print('$greeting $enabled $maxItems $ratio');
  print('${Color.red.code} ${total.toStringAsFixed(2)} ${stack.size} $length');
  print('$point $mapper $recordName/$recordValue $doubled $values');
  print('${classify(0)} ${classify(-1)} ${classify(point)} ${identity<int>(7)} $unique $map');
  print('${shapes.first.describe()} ${(() => mapper)() is Mapper<int, int>}');
}
