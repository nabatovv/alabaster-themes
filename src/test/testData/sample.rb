# frozen_string_literal: true

# Full Ruby syntax sample.
# Covers modules, classes, methods, blocks, procs, lambdas, symbols, hashes,
# exceptions, metaprogramming, string interpolation and pattern matching.

MAX_ITEMS = 1024
GREETING = 'hello'
ENABLED = true
RATIO = 1.5e3

module Describable
  def describe
    "#{self.class.name.downcase}: #{to_s}"
  end
end

module Comparable
  def largest
    max
  end
end

class Shape
  include Describable

  attr_reader :name
  attr_accessor :color

  def initialize(name, color: :black)
    @name = name
    @color = color
  end

  def area
    raise NotImplementedError, "#{self.class} must implement #area"
  end

  def to_s
    "#{name} #{area.round(2)}"
  end
end

class Circle < Shape
  def initialize(radius)
    super('circle')
    @radius = radius
  end

  def area
    Math::PI * @radius**2
  end
end

class Rectangle < Shape
  def initialize(width, height)
    super('rectangle', color: :blue)
    @width = width
    @height = height
  end

  def area
    @width * @height
  end
end

class Stack
  include Enumerable

  def initialize(*items)
    @items = items
  end

  def push(item)
    @items.push(item)
    self
  end

  def pop
    @items.pop
  end

  def each(&block)
    @items.each(&block)
  end

  def [](index)
    @items[index]
  end
end

def greet(name = 'world', punctuation: '!')
  "#{GREETING}, #{name}#{punctuation}"
end

def classify(value)
  case value
  when 0 then 'zero'
  when Integer then value.negative? ? 'negative' : 'positive'
  when String then "text #{value}"
  when Hash then value[:kind] || 'hash'
  else 'unknown'
  end
end

def pattern_matching(value)
  case value
  in { name: String => name, age: Integer => age } if age >= 18
    "#{name} is an adult"
  in [Integer => first, *rest]
    "#{first} then #{rest.inspect}"
  else
    'no match'
  end
end

def blocks_and_lambdas(numbers)
  doubled = numbers.map { |number| number * 2 }
  evens = numbers.select(&:even?)
  total = numbers.reduce(0) { |sum, number| sum + number }
  add = ->(a, b) { a + b }
  square = proc { |value| value**2 }
  [doubled, evens, total, add.call(1, 2), square.call(3)]
end

def exceptions
  raise ArgumentError, 'boom'
rescue ArgumentError => e
  e.message
ensure
  nil
end

def metaprogramming
  klass = Class.new do
    define_method(:dynamic) { |value| value * 2 }
  end

  klass.new.dynamic(21)
end

def strings
  text = "  Ruby strings  "
  {
    trimmed: text.strip,
    upper: text.upcase,
    length: text.length,
    formatted: format('%s has %d items', GREETING, 3),
    interpolation: "#{GREETING} #{MAX_ITEMS} #{RATIO} #{ENABLED}"
  }
end

def run
  shapes = [Circle.new(2), Rectangle.new(3, 4)]
  total = shapes.sum(&:area)
  stack = Stack.new(1, 2).push(3)

  puts greet('ruby')
  puts classify(-5)
  puts pattern_matching(name: 'alice', age: 30)
  p blocks_and_lambdas([1, 2, 3, 4])
  puts exceptions
  puts metaprogramming
  p strings
  puts total.round(2)
  puts stack.map { |value| value * 10 }.inspect
  puts stack[0]
  puts shapes.first.describe
  puts ENABLED, RATIO, MAX_ITEMS
end

run if $PROGRAM_NAME == __FILE__
