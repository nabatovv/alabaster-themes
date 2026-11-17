// Full TypeScript syntax sample.
// Covers interfaces, type aliases, generics, classes, enums, decorators,
// unions, narrowing, utility types, async/await, template literal types,
// `satisfies`, const type parameters and explicit resource management.

export const MAX_ITEMS: number = 1024;
export const GREETING: string = "hello";
export const ENABLED: boolean = true;
export const FACTOR: number = 1.5e3;

interface Entity {
  readonly id: number;
  name: string;
  tags?: string[];
}

type Status = "active" | "inactive" | "pending";
type Identifier = string | number;
type Mapper<T, U> = (value: T, index: number) => U;
type ReadonlyPoint = Readonly<{ x: number; y: number }>;
type PartialEntity = Partial<Entity>;
type PickedEntity = Pick<Entity, "id" | "name">;
type EventName = `on${Capitalize<string>}`;
type Entry = readonly [string, number];

enum Color {
  Red = "RED",
  Green = "GREEN",
  Blue = "BLUE",
}

enum Level {
  Low = 1,
  Medium,
  High,
}

abstract class Shape {
  protected constructor(public readonly name: string) {}

  abstract area(): number;

  describe(): string {
    return `${this.name}: ${this.area().toFixed(2)}`;
  }
}

class Circle extends Shape implements Entity {
  id = 1;
  name: string;
  tags: string[] = [];

  constructor(public radius: number) {
    super("circle");
    this.name = "circle";
  }

  area(): number {
    return Math.PI * this.radius ** 2;
  }
}

class Stack<T extends { toString(): string }> {
  private items: T[] = [];

  push(...values: T[]): this {
    this.items.push(...values);
    return this;
  }

  pop(): T | undefined {
    return this.items.pop();
  }

  get size(): number {
    return this.items.length;
  }
}

function identity<T>(value: T): T {
  return value;
}

function first<T>(items: readonly T[]): T | undefined {
  return items[0];
}

function isEntity(value: unknown): value is Entity {
  return typeof value === "object" && value !== null && "id" in value;
}

function describe(status: Status): string {
  switch (status) {
    case "active":
      return "running";
    case "inactive":
      return "stopped";
    case "pending":
      return "waiting";
    default: {
      const exhaustive: never = status;
      return exhaustive;
    }
  }
}

const map = <T, U>(items: T[], mapper: Mapper<T, U>): U[] => items.map(mapper);

function sum(...numbers: number[]): number {
  return numbers.reduce((total, current) => total + current, 0);
}

async function fetchEntity(id: Identifier): Promise<Entity | undefined> {
  await new Promise((resolve) => setTimeout(resolve, 0));
  return typeof id === "number" ? { id, name: `entity-${id}` } : undefined;
}

function decorator<TCtor extends { new (...args: any[]): {} }>(constructor: TCtor) {
  return class extends constructor {
    created = new Date();
  };
}

@decorator
class Service {
  private cache = new Map<string, Entity>();

  constructor(private readonly baseUrl: string) {}

  async load(id: number): Promise<Entity> {
    const key = String(id);
    if (!this.cache.has(key)) {
      const entity = await fetchEntity(id);
      if (entity) this.cache.set(key, entity);
    }
    return this.cache.get(key)!;
  }
}

const point: ReadonlyPoint = { x: 1, y: 2 };
const entries: Entry[] = [["a", 1], ...( [["b", 2]] as Entry[] )];
const unknownValue: unknown = { id: 1, name: "thing" };

if (isEntity(unknownValue)) {
  const { id, name } = unknownValue;
  console.log(identity<number>(id), name, describe("active"), describe("inactive"));
}

const stack = new Stack<string>().push("one", "two");
console.log(stack.pop(), stack.size, sum(1, 2, 3), map([1, 2], (n) => n * 2));
console.log(Color.Red, Level.Medium, point, entries, GREETING, ENABLED, MAX_ITEMS, FACTOR);

// `satisfies` keeps literal types while checking the shape.
const palette = {
  background: "#f7f7f7",
  foreground: "#000000",
} satisfies Record<string, `#${string}`>;

// Const type parameters preserve tuple literals.
function tuple<const T extends readonly unknown[]>(...values: T): T {
  return values;
}

const tupleValue = tuple("a", 1, true);

// Explicit resource management.
class DisposableResource {
  [Symbol.dispose](): void {}
}

function useResource(): void {
  using resource = new DisposableResource();
  void resource;
}

console.log(palette, tupleValue, useResource);
