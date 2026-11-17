// Full C++ syntax sample.
// Covers namespaces, templates, classes, inheritance, virtual dispatch,
// operators, lambdas, the STL, smart pointers, exceptions and modern C++23.

#include <algorithm>
#include <concepts>
#include <expected>
#include <iostream>
#include <map>
#include <memory>
#include <optional>
#include <ranges>
#include <span>
#include <stdexcept>
#include <string>
#include <string_view>
#include <vector>

namespace geometry {

constexpr double PI = 3.141592653589793;
inline constexpr char NAME[] = "geometry";

template <typename T>
concept Number = std::integral<T> || std::floating_point<T>;

template <Number T>
constexpr T square(T value) {
    return value * value;
}

template <typename... Args>
auto sum(Args... args) {
    return (args + ...);
}

class Shape {
public:
    Shape() = default;
    explicit Shape(std::string name) : name_(std::move(name)) {}
    virtual ~Shape() = default;

    [[nodiscard]] virtual double area() const = 0;
    virtual std::string describe() const {
        return name_ + ": " + std::to_string(area());
    }

    bool operator==(const Shape &other) const {
        return name_ == other.name_;
    }

protected:
    std::string name_;
};

class Circle final : public Shape {
public:
    explicit Circle(double radius) : Shape("circle"), radius_(radius) {}

    double area() const override { return PI * radius_ * radius_; }

private:
    double radius_;
};

class Rectangle : public Shape {
public:
    Rectangle(double width, double height)
        : Shape("rectangle"), width_(width), height_(height) {}

    double area() const override { return width_ * height_; }
    double width() const { return width_; }
    double height() const { return height_; }

private:
    double width_;
    double height_;
};

struct Point {
    int x;
    int y;

    Point operator+(const Point &other) const { return {x + other.x, y + other.y}; }
};

template <typename T>
class Stack {
public:
    void push(T value) { items_.push_back(std::move(value)); }

    T pop() {
        if (items_.empty()) {
            throw std::out_of_range("stack is empty");
        }
        T value = std::move(items_.back());
        items_.pop_back();
        return value;
    }

    [[nodiscard]] bool empty() const { return items_.empty(); }

private:
    std::vector<T> items_;
};

}  // namespace geometry

std::expected<int, std::string> parse_int(std::string_view text) {
    try {
        return std::stoi(std::string(text));
    } catch (const std::exception &) {
        return std::unexpected("not an integer");
    }
}

constexpr int consteval_branch() {
    if consteval {
        return 1;
    } else {
        return 2;
    }
}

int main() {
    using namespace geometry;

    std::vector<std::unique_ptr<Shape>> shapes;
    shapes.push_back(std::make_unique<Circle>(2.0));
    shapes.push_back(std::make_unique<Rectangle>(3.0, 4.0));

    double total = 0.0;
    for (const auto &shape : shapes) {
        std::cout << shape->describe() << ' ' << shape->area() << '\n';
        total += shape->area();
    }

    std::map<std::string, int> counts{{"one", 1}, {"two", 2}};
    counts["three"] = 3;

    auto doubled = counts
                   | std::views::filter([](const auto &entry) { return entry.second > 1; })
                   | std::views::transform([](const auto &entry) { return entry.first; });

    for (const auto &key : doubled) {
        std::cout << key << ' ';
    }
    std::cout << '\n';

    Stack<int> stack;
    stack.push(42);
    stack.push(7);

    Point a{1, 2};
    Point b{3, 4};
    auto [px, py] = a + b;

    std::optional<Stack<int>> maybe;
    maybe.emplace();

    try {
        stack.pop();
    } catch (const std::exception &error) {
        std::cerr << "error: " << error.what() << '\n';
    }

    const std::vector<int> span_values{1, 2, 3};
    const std::span<const int> view{span_values};
    const auto parsed = parse_int("42");
    std::cout << view.size() << ' ' << (parsed ? *parsed : -1) << ' '
              << consteval_branch() << '\n';

    constexpr auto squared = square(5);
    std::cout << squared << ' ' << sum(1, 2, 3) << ' ' << px << py << '\n';
    return 0;
}
