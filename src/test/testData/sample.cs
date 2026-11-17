// Full C# syntax sample.
// Covers namespaces, records, classes, interfaces, generics, LINQ, async,
// pattern matching, enums, structs, delegates, events and nullable types.

using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace Alabaster.Sample;

public const int MaxItems = 1024;
public const string Greeting = "hello";
public const bool Enabled = true;
public const double Ratio = 1.5e3;

public enum Status
{
    Active,
    Inactive,
    Pending,
}

[Flags]
public enum Permissions
{
    None = 0,
    Read = 1,
    Write = 2,
    Execute = 4,
}

public readonly record struct Point(double X, double Y);

public record Person(string Name, int Age = 0)
{
    public bool IsAdult => Age >= 18;
}

public interface IRepository<T> where T : class
{
    T? Find(int id);
    void Add(T item);
    IEnumerable<T> All();
}

public abstract class Shape
{
    protected Shape(string name) => Name = name;

    public string Name { get; }

    public abstract double Area();

    public virtual string Describe() => $"{Name}: {Area():F2}";
}

public sealed class Circle : Shape
{
    public Circle(double radius) : base("circle") => Radius = radius;

    public double Radius { get; }

    public override double Area() => Math.PI * Radius * Radius;
}

public class Stack<T> : IRepository<T> where T : class
{
    private readonly List<T> _items = new();
    private int _nextId;

    public event EventHandler<T>? ItemAdded;

    public delegate void Handler(T item);

    public void Add(T item)
    {
        _items.Add(item);
        ItemAdded?.Invoke(this, item);
    }

    public T? Find(int id) => id >= 0 && id < _items.Count ? _items[id] : null;

    public IEnumerable<T> All() => _items;

    public int Count => _items.Count;

    public T this[int index] => _items[index];
}

public static class Program
{
    private static async Task<int> FetchAsync(int id)
    {
        await Task.Delay(0);
        return id;
    }

    private static string Classify(object value) => value switch
    {
        int and > 0 => "positive",
        int and < 0 => "negative",
        0 => "zero",
        string { Length: > 3 } text => $"long text {text}",
        Person { IsAdult: true } person => person.Name,
        null => "null",
        _ => "unknown",
    };

    public static async Task Main(string[] args)
    {
        var people = new List<Person>
        {
            new("alice", 30),
            new("bob", 17),
        };

        var adults = people
            .Where(person => person.IsAdult)
            .OrderBy(person => person.Name)
            .Select(person => person with { Name = person.Name.ToUpperInvariant() })
            .ToList();

        var stack = new Stack<Person>();
        stack.ItemAdded += (_, person) => Console.WriteLine($"added {person.Name}");
        stack.Add(new Person("carol", 40));

        var total = args.Length > 0 ? int.Parse(args[0]) : 0;
        var (x, y) = new Point(1.0, 2.0);
        var status = Status.Pending;
        var permissions = Permissions.Read | Permissions.Write;

        try
        {
            total += await FetchAsync(42);
        }
        catch (Exception exception) when (exception is not ArgumentNullException)
        {
            Console.WriteLine(exception.Message);
        }
        finally
        {
            Console.WriteLine("done");
        }

        Console.WriteLine($"{Greeting} {Enabled} {MaxItems} {Ratio:F1}");
        Console.WriteLine($"{x} {y} {status} {permissions} {Classify(total)}");
        Console.WriteLine(string.Join(", ", adults.Select(person => person.Name)));
        Console.WriteLine($"{stack.Count} {stack[0].Name} {string.Join(",", stack.All().Select(p => p.Name))}");
    }
}
