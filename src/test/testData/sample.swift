// Full Swift syntax sample.
// Covers structs, classes, enums with associated values, protocols, extensions,
// generics, closures, optionals, properties, subscripts, async/await and errors.

import Foundation

let maxItems: Int = 1024
let greeting: String = "hello"
let enabled: Bool = true
let ratio: Double = 1.5e3

enum Direction: String, CaseIterable {
    case north = "N"
    case south = "S"
    case east = "E"
    case west = "W"

    var opposite: Direction {
        switch self {
        case .north: return .south
        case .south: return .north
        case .east: return .west
        case .west: return .east
        }
    }
}

enum Result<Value> {
    case success(Value)
    case failure(Error)

    var value: Value? {
        if case let .success(value) = self { return value }
        return nil
    }
}

protocol Describable {
    var description: String { get }
}

protocol Repository {
    associatedtype Item: Describable

    func all() -> [Item]
    func find(_ index: Int) -> Item?
}

struct Point: Describable, Equatable {
    var x: Double
    var y: Double

    var description: String { "(\(x), \(y))" }

    mutating func translate(by delta: Point) {
        x += delta.x
        y += delta.y
    }
}

final class Shape: Describable {
    let name: String
    private(set) var area: Double

    init(name: String, area: Double) {
        self.name = name
        self.area = area
    }

    var description: String { "\(name): \(String(format: "%.2f", area))" }
}

final class Circle: Shape {
    let radius: Double

    init(radius: Double) {
        self.radius = radius
        super.init(name: "circle", area: Double.pi * radius * radius)
    }
}

struct Stack<Element>: Repository where Element: Describable {
    private var items: [Element] = []

    mutating func push(_ value: Element) {
        items.append(value)
    }

    subscript(index: Int) -> Element? {
        items.indices.contains(index) ? items[index] : nil
    }

    func all() -> [Element] { items }
    func find(_ index: Int) -> Element? { self[index] }
}

extension Stack {
    var isEmpty: Bool { items.isEmpty }
}

extension Array where Element: Comparable {
    func largest() -> Element? {
        guard let first = self.first else { return nil }
        return dropFirst().reduce(first) { $0 > $1 ? $0 : $1 }
    }
}

func identity<T>(_ value: T) -> T { value }

func sum(_ numbers: Int...) -> Int {
    numbers.reduce(0, +)
}

func classify(_ value: Any) -> String {
    switch value {
    case 0:
        return "zero"
    case let number as Int where number > 0:
        return "positive \(number)"
    case is String:
        return "text"
    case let shape as Shape:
        return shape.description
    default:
        return "unknown"
    }
}

enum ValidationError: Error {
    case empty
    case tooLong(limit: Int)
}

func validate(_ text: String) throws -> String {
    guard !text.isEmpty else { throw ValidationError.empty }
    guard text.count <= maxItems else { throw ValidationError.tooLong(limit: maxItems) }
    return text
}

@MainActor
func fetch(_ url: String) async throws -> String {
    try await Task.sleep(nanoseconds: 0)
    return "content of \(url)"
}

func run() async {
    var stack = Stack<Point>()
    stack.push(Point(x: 1, y: 2))
    stack.push(Point(x: 3, y: 4))

    var mutablePoint = Point(x: 0, y: 0)
    mutablePoint.translate(by: Point(x: 1, y: 1))

    let shapes: [Shape] = [Circle(radius: 2), Shape(name: "square", area: 16)]
    for shape in shapes where shape.area > 1 {
        print(shape.description)
    }

    let numbers = [3, 1, 4, 1, 5]
    let sorted = numbers.sorted().map { $0 * 2 }
    let total = numbers.reduce(0, +)

    if let index = numbers.firstIndex(of: 4), index > 0 {
        print("index \(index)")
    }

    guard let largest = numbers.largest() else { return }
    print(largest, total, sorted, identity("value"), sum(1, 2, 3))

    do {
        print(try validate("sample"))
        print(try await fetch("https://example.com"))
    } catch ValidationError.empty {
        print("empty")
    } catch let ValidationError.tooLong(limit) {
        print("too long: \(limit)")
    } catch {
        print("failed: \(error)")
    }

    let optional: String? = Direction.north.opposite.rawValue
    print(optional ?? "none", Direction.allCases.count, enabled, ratio, greeting, maxItems)
    print(classify(42), classify("text"), classify(Point(x: 1, y: 1)), stack.isEmpty)
}
