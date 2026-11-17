// Full JavaScript syntax sample.
// Coverage note: exercises modern ES2025/ES2026 syntax on Node 26 (ESM) so the
// theme can highlight every token kind. This is a highlighting fixture, not
// runnable logic — top-level await and side effects are expected.
// --- 1. Module imports (ESM) ---
import { readFile, writeFile } from 'node:fs/promises';
import * as path from 'node:path';
import { EventEmitter as Emitter } from 'node:events';

// --- 2. Declarations & primitive literals ---
const decimal = 42;
const binary = 0b1010_1100;
const octal = 0o755;
const hex = 0xDEAD_BEEF;
const bigInt = 9_007_199_254_740_993n;
const float = 3.141_592_653;
const expo = 6.022e23;
const exponent = 2 ** 10;
const single = 'single quoted';
const double = "double quoted";
const escaped = 'line\nbreak\ttab\u00e9\u{1F600}';
const template = `template ${decimal + 1} literal`;
const multiline = `multi
line`;
const tagged = String.raw`raw\n${hex}`;
const boolTrue = true;
const boolFalse = false;
const nothing = null;
const missing = undefined;
const unique = Symbol('unique');
const sharedSym = Symbol.for('app.shared');
const regex = /(?<area>\d{3})-(?<num>\d{4})/gu;
const unicodeSet = /[\p{Letter}--\p{ASCII}]/v;
let counter = 0;
counter += 1;
counter++;
counter--;

// --- 3. Destructuring, spread, rest ---
const [first, second = 2, ...others] = [1, undefined, 3, 4];
const { a, b: renamed, c = 3, ...restProps } = { a: 1, b: 2, d: 4 };
const { nested: { deep, deeper = 'x' } = {} } = { nested: { deep: true } };
const merged = [...others, ...Object.values(restProps)];
const cloned = { ...restProps, extra: true };
const [head, ...tail] = 'abc';

// --- 4. Operators ---
const sum = a + first - second * 2 / 2 % 3;
const comparison = a < b && b >= first || a !== second;
const bitwise = (a & b) | (a ^ b) | (~a) << 1 >> 1 >>> 1;
const optional = restProps?.d?.toString?.();
const nullish = nothing ?? 'fallback';
let mutable = 0;
mutable ??= 10;
mutable ||= 20;
mutable &&= 30;
const ternary = a > b ? 'greater' : 'lesser';
void (a, b, second);
typeof optional === 'string';
delete restProps.d;

// --- 5. Control flow ---
if (a > b) {
  counter = a;
} else if (a === b) {
  counter = 0;
} else {
  counter = b;
}
switch (renamed) {
  case 1:
  case 2:
    counter += 1;
    break;
  case 3: {
    const scoped = true;
    counter += scoped ? 1 : 0;
    break;
  }
  default:
    counter = -1;
}
for (let i = 0; i < 3; i++) counter += i;
for (const key in restProps) counter += key.length;
for (const value of others) counter += value;
let whileCount = 0;
while (whileCount < 3) whileCount++;
do {
  whileCount--;
} while (whileCount > 0);
outer: for (const row of [[1, 2], [3, 4]]) {
  for (const cell of row) {
    if (cell === 4) break outer;
    if (cell === 2) continue outer;
    counter += cell;
  }
}

// --- 6. Functions ---
function declared(x, y = 10, ...rest) {
  return x + y + rest.length;
}
const expressed = function namedExpr(z) {
  return z * 2;
};
const arrow = (x, y) => x + y;
const arrowBlock = (x) => {
  const inner = x + 1;
  return inner;
};
function makeCounter(start = 0) {
  let count = start;
  return () => ++count; // closure
}
const adders = [1, 2, 3].map((n) => (x) => x + n); // higher-order
(function iife() {
  counter += 1;
})();
const factorial = (n) => (n <= 1 ? 1 : n * factorial(n - 1)); // recursion
function* generator(range) {
  for (let i = 0; i < range; i++) yield i;
  yield* range; // delegate
}
async function fetchThing(id) {
  await Promise.resolve(id);
  return { id };
}
async function* asyncGenerator(items) {
  for (const item of items) yield await Promise.resolve(item);
}

// --- 7. Classes ---
class Shape {
  static #instances = 0; // static private field
  #id; // private field
  static {
    this.registry = new Map(); // static initialization block
  }
  constructor(name) {
    this.name = name;
    this.#id = ++Shape.#instances;
  }
  get id() { return this.#id; }
  set id(value) { this.#id = value; }
  static create(name) { return new this(name); }
  static hasBrand(obj) { return #id in obj; } // private brand check
  #describe() { return `${this.name}#${this.#id}`; }
  describe() { return this.#describe(); }
  toString() { return `Shape(${this.name})`; }
}
class Circle extends Shape {
  radius = 1; // instance field
  constructor(radius = 1) {
    super('circle');
    this.radius = radius;
  }
  area() { return Math.PI * this.radius ** 2; }
}
const circle = new Circle(2);
const shapeExpr = class Named extends Shape {};
void shapeExpr;

// --- 8. Objects ---
const shorthandValue = 1;
const obj = {
  shorthandValue,
  ['computed' + 'Key']: true,
  method() { return this.shorthandValue; },
  get doubled() { return this.shorthandValue * 2; },
  set doubled(v) { this.shorthandValue = v / 2; },
  *gen() { yield 1; },
  async asyncMethod() { return await Promise.resolve(1); },
};
const proto = Object.create(obj);
Object.assign(cloned, { assigned: 1 });
const frozen = Object.freeze({ immutable: true });
const grouped = Object.groupBy([1, 2, 3, 4], (n) => (n % 2 ? 'odd' : 'even'));

// --- 9. Arrays & modern array methods ---
const numbers = [3, 1, 4, 1, 5, 9, 2, 6];
const atLast = numbers.at(-1);
const sorted = numbers.toSorted((x, y) => x - y);
const reversed = numbers.toReversed();
const spliced = numbers.toSpliced(0, 1, 99);
const replaced = numbers.with(0, 100);
const lastFound = numbers.findLast((n) => n < 6);
const flattened = [[1, [2]], [3]].flat(Infinity);
const mappedFlat = [1, 2, 3].flatMap((n) => [n, n * 2]);

// --- 10. Collections ---
const map = new Map([['a', 1], ['b', 2]]);
map.set('c', 3).set('d', 4);
const set = new Set([1, 2, 3, 2, 1]);
const weakMap = new WeakMap([[obj, 'meta']]);
const weakSet = new WeakSet([obj]);

// --- 11. Iterators & for-await-of ---
const iterable = {
  *[Symbol.iterator]() {
    yield* numbers;
  },
};
for (const n of iterable) counter += n;
async function consume() {
  for await (const value of asyncGenerator([1, 2, 3])) counter += value;
}
void consume;

// --- 12. Promises & async/await ---
const all = Promise.all([fetchThing(1), fetchThing(2)]);
const allSettled = Promise.allSettled([fetchThing(1), Promise.reject(new Error('x'))]);
const any = Promise.any([Promise.reject(0), Promise.resolve(1)]);
const { promise: manual, resolve: manualResolve } = Promise.withResolvers();
void manual;
manualResolve(1);
const resolved = await all; // top-level await

// --- 13. Symbol, Proxy, Reflect, WeakRef, structuredClone ---
class Branded {
  static [Symbol.hasInstance](value) { return Boolean(value?.brand); }
  [Symbol.toPrimitive](hint) { return hint === 'number' ? 1 : 'branded'; }
}
const wellKnown = [Symbol.iterator, Symbol.asyncIterator, Symbol.dispose];
const proxy = new Proxy(obj, {
  get(target, prop, receiver) { return Reflect.get(target, prop, receiver); },
  set(target, prop, value) { return Reflect.set(target, prop, value); },
});
void proxy;
const weakRef = new WeakRef(obj);
const registry = new FinalizationRegistry((held) => void held);
registry.register(obj, 'resource');
const clone = structuredClone({ data: numbers, nested: { map } });

// --- 14. Intl & JSON ---
const money = new Intl.NumberFormat('en-US', {
  style: 'currency',
  currency: 'USD',
}).format(1234.5);
const dateFmt = new Intl.DateTimeFormat('de-DE', { dateStyle: 'full' });
const now = dateFmt.format(new Date());
const serialized = JSON.stringify({ money, now, numbers });
const parsed = JSON.parse(serialized, (key, value) =>
  key === 'numbers' ? value.map(Number) : value,
);

// --- 15. Errors ---
class CustomError extends Error {
  constructor(message, options) {
    super(message, options); // options carries { cause }
    this.name = 'CustomError';
  }
}
function validate(input) {
  if (!input) throw new CustomError('missing input', { cause: 'empty' });
  return input;
}
try {
  validate('');
} catch (error) {
  counter = error?.cause ?? 0;
} finally {
  counter += 1;
}
const aggregate = new AggregateError([new Error('e1'), new Error('e2')], 'multi');

// --- 16. Explicit resource management (using / await using) ---
{
  using handle = { [Symbol.dispose]() { counter = 0; } };
  await using asyncHandle = { async [Symbol.asyncDispose]() { await Promise.resolve(); } };
  void handle;
  void asyncHandle;
}

// --- 17. Dynamic import & module exports ---
const utilModule = await import('node:util');
const formatted = utilModule.format('%s=%d', 'value', counter);
export const exportedConst = decimal;
export let exportedLet = counter;
export function exportedFn(input = {}) { return validate(input); }
export class ExportedClass extends Shape {}
export default {
  add: arrow,
  describe: (c) => c.describe(),
  fetched: resolved,
  results: allSettled,
  firstSettled: any,
  money,
  formatted,
  literals: {
    binary, octal, hex, bigInt, float, expo, exponent,
    template, multiline, tagged, unique, sharedSym, regex, unicodeSet,
    escaped, single, double, boolTrue, boolFalse, nothing, missing,
  },
  collections: { map, set, weakMap, weakSet, clone, weakRef, registry, frozen, grouped, proto, merged, wellKnown },
  arrays: { atLast, sorted, reversed, spliced, replaced, lastFound, flattened, mappedFlat, adders, head, tail, first, second, others },
  parsed, aggregate, registry, path: path.sep, Emitter, readFile, writeFile,
};
