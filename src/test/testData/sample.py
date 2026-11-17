"""Full Python syntax sample.

Covers imports, decorators, classes, dataclasses, typing, generators,
comprehensions, context managers, exceptions, pattern matching, async,
PEP 695 generics, exception groups and template strings.
"""

from __future__ import annotations

import asyncio
import json
from dataclasses import dataclass, field
from typing import Any, Callable, Iterable, Iterator, Protocol

MAX_SIZE = 1024
PI = 3.14159
HEX_VALUE = 0xDEADBEEF
FLAG = True


class Comparable(Protocol):
    def key(self) -> str: ...


@dataclass(order=True)
class Person:
    name: str
    age: int = 0
    tags: list[str] = field(default_factory=list)

    def __post_init__(self) -> None:
        self.name = self.name.strip()

    @property
    def is_adult(self) -> bool:
        return self.age >= 18

    @staticmethod
    def from_dict(data: dict[str, Any]) -> "Person":
        return Person(**{k: data[k] for k in ("name", "age") if k in data})

    @classmethod
    def anonymous(cls) -> "Person":
        return cls("anonymous")

    def __repr__(self) -> str:
        return f"Person(name={self.name!r}, age={self.age})"


class Employee(Person):
    def __init__(self, name: str, age: int, salary: float) -> None:
        super().__init__(name, age)
        self.salary = salary

    def raise_salary(self, percent: float = 5.0) -> None:
        self.salary *= 1 + percent / 100


def decorate(func: Callable[[int], int]) -> Callable[[int], int]:
    def wrapper(value: int) -> int:
        print(f"calling {func.__name__}")
        return func(value)

    return wrapper


@decorate
def double(value: int) -> int:
    return value * 2


def counter(start: int = 0) -> Iterator[int]:
    current = start
    while True:
        yield current
        current += 1


def powers(limit: int, **kwargs) -> list[int]:
    return [number**2 for number in range(limit) if number % 2 == 0]


def unpack(a, b, *args, **kwargs) -> tuple:
    total = sum(args) + len(kwargs)
    return a, b, total


def divide(a: float, b: float) -> float:
    if b == 0:
        raise ValueError("cannot divide by zero")
    return a / b


class Context:
    def __enter__(self) -> "Context":
        print("enter")
        return self

    def __exit__(self, exc_type, exc_value, traceback) -> bool:
        print("exit")
        return False


def classify(value: int | str) -> str:
    match value:
        case 0:
            return "zero"
        case int() as number if number < 0:
            return "negative"
        case str() as text:
            return f"text {text}"
        case {"kind": kind}:
            return kind
        case _:
            return "unknown"


async def fetch(url: str) -> dict[str, Any]:
    await asyncio.sleep(0)
    return {"url": url}


async def main() -> None:
    data = await fetch("https://example.com")
    print(json.dumps(data, indent=2))


def main_sync() -> None:
    people = [Person("alice", 30), Person("bob", 17)]
    adults = [person.name for person in people if person.is_adult]

    squares = {n: n * n for n in range(5)}
    unique = {tag for person in people for tag in person.tags}

    with Context():
        try:
            print(divide(1, 0))
        except ValueError as error:
            print(f"error: {error}")
        finally:
            print("done")

    iterator = counter(10)
    print(next(iterator), next(iterator))

    if (size := len(adults)) > 0:
        print(size, squares, unique, powers(10), double(21))

    print(unpack(1, 2, 3, 4, key="value"), classify(-5))
    print([*adults, *range(3)], {**squares})


type Vector = list[float]


class Box[T]:
    def __init__(self, value: T) -> None:
        self.value = value

    def get(self) -> T:
        return self.value


def first[T](items: list[T]) -> T:
    return items[0]


def group_errors() -> None:
    try:
        raise ExceptionGroup("errors", [ValueError("bad"), TypeError("wrong")])
    except* ValueError as errors:
        print("value errors:", len(errors.exceptions))
    except* TypeError as errors:
        print("type errors:", len(errors.exceptions))


def template_demo(name: str) -> None:
    template = t"Hello {name}"
    print(template.strings, template.interpolations)


if __name__ == "__main__":
    main_sync()
    asyncio.run(main())
