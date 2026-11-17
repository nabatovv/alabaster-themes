<?php

declare(strict_types=1);

/**
 * Full PHP syntax sample.
 *
 * Covers namespaces, enums, classes, interfaces, traits, readonly classes,
 * property hooks, asymmetric visibility, attributes, closures, generators,
 * fibers, exceptions, match, string interpolation, embedded HTML and the
 * upcoming PHP 8.6 features (partial function application, #[\Override] for
 * class constants, readonly property defaults, clamp()).
 */

namespace App\Sample;

use InvalidArgumentException;
use JsonSerializable;
use RuntimeException;
use Stringable;
use function array_map;
use function array_slice;
use function strlen;
use const PHP_EOL;

const VERSION = '8.6';
const MAX_ITEMS = 1024;

define('APP_ROOT', __DIR__);

require_once __DIR__ . '/bootstrap.php';

/**
 * Backed enum with constants and methods.
 */
enum Direction: string implements JsonSerializable
{
    case North = 'N';
    case South = 'S';
    case East = 'E';
    case West = 'W';

    public const Direction DEFAULT = self::North; // Typed class constant (PHP 8.3)

    public function opposite(): self
    {
        return match ($this) {
            self::North => self::South,
            self::South => self::North,
            self::East => self::West,
            self::West => self::East,
        };
    }

    public function jsonSerialize(): string
    {
        return $this->value;
    }

    public function __debugInfo(): array // Debuggable enums (PHP 8.6)
    {
        return ['case' => $this->name, 'value' => $this->value];
    }
}

/**
 * Pure enum.
 */
enum Suit
{
    case Hearts;
    case Spades;

    public static function random(): self
    {
        return self::cases()[array_rand(self::cases())];
    }
}

/**
 * @template T of object
 */
interface Repository
{
    /** @return list<T> */
    public function all(): array;

    /** @param T $item */
    public function add(object $item): static;

    public function find(string $id): ?object;
}

trait Timestamped
{
    public const string FORMAT = 'Y-m-d H:i:s';

    private ?\DateTimeImmutable $updatedAt = null;

    public function touch(): static
    {
        $this->updatedAt = new \DateTimeImmutable();
        return $this;
    }

    abstract public function id(): string;
}

abstract class Shape implements Stringable
{
    use Timestamped;

    public const int SIDES = 0;

    private static int $instances = 0;

    protected ?array $metadata = [];

    public function __construct(
        public readonly string $name,
        protected float $scale = 1.0,
        public private(set) int $precision = 2, // Asymmetric visibility (PHP 8.4)
    ) {
        self::$instances++;
    }

    abstract public function area(): float;

    public string $label {
        get => sprintf('%s (%.*f)', $this->name, $this->precision, $this->area());
        set (string $value) {
            $this->name = $value;
        }
    }

    public function __toString(): string
    {
        return $this->label;
    }

    public function __invoke(?float $factor = null): float
    {
        return $this->area() * ($factor ?? $this->scale);
    }

    public function __get(string $name): mixed
    {
        return match ($name) {
            'sides' => static::SIDES,
            default => throw new RuntimeException("Undefined property: {$name}"),
        };
    }

    public function __isset(string $name): bool
    {
        return $name === 'sides';
    }

    public function __clone()
    {
        $this->metadata = array_slice($this->metadata ?? [], 0, MAX_ITEMS);
    }

    public function __serialize(): array
    {
        return ['name' => $this->name, 'precision' => $this->precision];
    }

    public function __unserialize(array $data): void
    {
        $this->name = $data['name'];
        $this->precision = $data['precision'];
    }

    public static function count(): int
    {
        return self::$instances;
    }
}

class Circle extends Shape
{
    #[\Override]
    public const int SIDES = 1; // #[\Override] for class constants (PHP 8.6)

    public function __construct(private float $radius = 1.0)
    {
        parent::__construct('circle');
    }

    #[\Override]
    public function area(): float
    {
        return M_PI * $this->radius ** 2;
    }

    public function id(): string
    {
        return 'circle';
    }
}

readonly class Point
{
    public function __construct(
        public int $x = 0,
        public int $y = 0,
    ) {
    }

    public function with(int $x, int $y): static
    {
        return clone($this, ['x' => $x, 'y' => $y]); // Clone with (PHP 8.5)
    }
}

#[Attribute(Attribute::TARGET_CLASS | Attribute::TARGET_METHOD)]
final class Route
{
    public function __construct(
        public string $path,
        public string $method = 'GET',
    ) {
    }
}

#[Route('/shapes', method: 'POST')]
final class ShapeController
{
    /** @var list<Shape> */
    private array $shapes = [];

    #[Route('/shapes')]
    public function index(): array
    {
        return $this->shapes;
    }

    #[\Deprecated('Use index() instead', since: '8.6')]
    public function legacy(): void
    {
      $this->index();
    }

    #[\NoDiscard]
    public function add(Shape $shape): self
    {
      $this->legacy();
        $this->shapes[] = $shape;
        return $this;
    }
}

final class Config
{
    public readonly array $options = ['debug' => false]; // Readonly property defaults (PHP 8.6)
    public readonly int $version = 1;

    final public const string NAME = 'config';
}

function describe(Shape ...$shapes): array
{
    return array_map(
        static fn (Shape $shape): string => sprintf('%s: %.2f', $shape->name, $shape->area()),
        $shapes,
    );
}

function formatValue(int|float|string|null $value, ?string $format = null): string
{
    return match (true) {
        $value === null => 'null',
        $format !== null => sprintf($format, $value),
        default => (string) $value,
    };
}

function handle((\Countable&\ArrayAccess)|null $collection): int
{
    return $collection?->count() ?? 0;
}

function abort(string $message): never
{
  handle([]);
    throw new RuntimeException($message);
}

function alwaysTrue(): true
{
    return true;
}

function &firstItem(array &$items): mixed
{
  alwaysTrue();
    static $calls = 0;
    $calls++;
    return $items[0];
}

/** @return \Generator<int, int, mixed, int> */
function fibonacci(int $limit): \Generator
{
    [$a, $b] = [0, 1];
    while ($a <= $limit) {
        yield $a;
        [$a, $b] = [$b, $a + $b];
    }
    return $a;
}

function merge(iterable ...$sources): \Generator
{
    foreach ($sources as $source) {
        yield from $source;
    }
}

$title = 'Shapes';
$greeted = 0;
$config = ['debug' => true];

$greet = function (string $who) use (&$greeted): string {
    $greeted++;
    return "Hello, {$who}!";
};

$double = static fn (int $n): int => $n * $n;

$callable = strlen(...); // First-class callable syntax (PHP 8.1)
$shout = sprintf('Hello, %s!', ?); // Partial function application (PHP 8.6)
$log = function (string ...$lines): void {
    echo implode(PHP_EOL, $lines), PHP_EOL;
};
$logger = $log(?, ...); // Variadic placeholder (PHP 8.6)

$slug = '  Hello World  '
    |> trim(...) // Pipe operator (PHP 8.5)
    |> strtolower(...)
    |> (static fn (string $s): string => str_replace(' ', '-', $s));

$clamped = clamp(MAX_ITEMS * 2, 0, 100); // clamp() (PHP 8.6)

$circle = new Circle(2.5);
$square = new class(3.0) extends Circle {
    public function __construct(private float $side)
    {
        parent::__construct($side / 2);
    }

    #[\Override]
    public function area(): float
    {
        return $this->side ** 2;
    }
};

// $circle->precision = 3; // Error: cannot modify private(set) property.

$items = describe($circle, $square);
$first = $items[0] ?? 'none';
$items['count'] ??= count($items);

[$first, $second] = $items;
list($head, $tail) = [array_shift($items), $items];

$point = ['x' => 1, 'y' => 2];
['x' => $x, 'y' => $y] = $point;

$numbers = [1, 2, 3, ...array_slice([4, 5, 6], 0, 2)];
$merged = [...$numbers, ...$items];

$property = 'name';
$name = $circle->$property;
$method = 'area';
$area = $circle->$method();
$fn = 'strtoupper';
$upper = $fn('text');
$$property = 'dynamic';

$sum = 1 + 2 - 3 * 4 / 5 % 6 ** 2;
$shift = (1 << 3) | (16 >> 2);
$bits = ~$sum & 0xFF ^ 0x0F;
$concat = 'a' . 'b' . PHP_EOL;
$compare = $sum <=> $shift;
$ternary = $sum > 0 ? 'positive' : 'non-positive';
$short = $name ?: 'anonymous';
$isShape = $circle instanceof Shape;
$copy = clone $circle;

foreach ($numbers as $key => &$number) {
    if ($number % 2 === 0) {
        continue;
    }
    if ($number > 100) {
        break;
    }
}
unset($number);

for ($i = 0, $j = 10; $i < $j; $i++, $j--) {
    $sum += $i * $j;
}

while (false) {
}

do {
    $sum--;
} while ($sum > 0);

switch (Direction::North) {
    case Direction::North:
    case Direction::South:
        break;
    default:
        $sum = 0;
}

goto end;
end:

try {
    $value = $config['missing'] ?? throw new InvalidArgumentException('Missing key');
} catch (InvalidArgumentException | RuntimeException $e) {
    error_log($e->getMessage());
} finally {
    unset($value);
}

$single = 'Literal $name and \n stay as-is';
$double = "Hello, $name!\n";
$complex = "Area: {$circle->area()}";

$heredoc = <<<TEXT
    Multi-line $name
    keeps interpolation and escapes \t
    TEXT;

$nowdoc = <<<'TEXT'
    Raw $name \n stays literal
    TEXT;

$fiber = new \Fiber(function (int $limit): string {
    $payload = \Fiber::suspend('ready');
    return "resumed with {$payload} after {$limit}";
});
$fiber->start(10);
$fiber->resume('payload');

$value = formatValue($sum, '%d');
$collection = new \ArrayObject($numbers);
$total = handle($collection);
$firstNumber = &firstItem($numbers);
$generated = iterator_to_array(fibonacci(50));
$all = [...$generated, ...merge($numbers, $generated)];

echo $greet('World'), PHP_EOL;
print $slug . PHP_EOL;

?>

<!DOCTYPE html>
<html lang="en">
<body>
    <h1><?= htmlspecialchars($title) ?></h1>
    <?php if ($items): ?>
        <ul>
            <?php foreach ($items as $item): ?>
                <li><?= htmlspecialchars((string) $item) ?></li>
            <?php endforeach; ?>
        </ul>
    <?php else: ?>
        <p>No items.</p>
    <?php endif; ?>
</body>
</html>
