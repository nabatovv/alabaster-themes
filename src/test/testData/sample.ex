# Full Elixir syntax sample.
# Covers modules, functions with guards, pattern matching, pipes, structs,
# protocols, behaviours, GenServer, comprehensions, sigils and error handling.

defmodule Alabaster.Sample do
  @moduledoc """
  Full Elixir syntax sample.
  """

  @max_items 1024
  @greeting "hello"
  @ratio 1.5e3
  @enabled true

  defstruct name: "world", age: 0, tags: []

  @type person :: %__MODULE__{name: String.t(), age: non_neg_integer(), tags: [atom()]}

  @spec greet(String.t(), String.t()) :: String.t()
  def greet(name \\ "world", punctuation \\ "!") do
    @greeting <> ", " <> name <> punctuation
  end

  def classify(0), do: "zero"
  def classify(value) when is_integer(value) and value < 0, do: "negative"
  def classify(value) when is_integer(value), do: "positive"
  def classify(value) when is_binary(value), do: "text " <> value
  def classify(_value), do: "unknown"

  def sum(numbers) when is_list(numbers) do
    Enum.reduce(numbers, 0, &(&1 + &2))
  end

  def pipeline(numbers) do
    numbers
    |> Enum.filter(&(rem(&1, 2) == 0))
    |> Enum.map(&(&1 * 2))
    |> Enum.sum()
  end

  def pattern_matching do
    {first, second} = {1, 2}
    [head | tail] = [3, 4, 5]
    %{name: name} = %{name: "map", value: 42}

    case {first, second} do
      {0, _} -> "starts with zero"
      {^first, ^second} -> "matched"
      _ -> "fallback"
    end
    |> String.upcase()

    cond do
      first > second -> :greater
      first == second -> :equal
      true -> :less
    end

    with {:ok, value} <- fetch(first),
         {:ok, doubled} <- double(value) do
      doubled
    else
      {:error, reason} -> {:failed, reason}
    end

    {head, tail, name}
  end

  def fetch(value) when value > 0, do: {:ok, value}
  def fetch(_value), do: {:error, :invalid}

  def double(value), do: {:ok, value * 2}

  def comprehensions do
    for number <- 1..10, rem(number, 2) == 0, into: %{}, do: {number, number * number}
  end

  def sigils do
    text = ~s(double "quoted" string)
    regex = ~r/^\d{4}-\d{2}-\d{2}$/
    words = ~w(one two three)a
    date = ~D[2026-11-13]
    {text, regex, words, date}
  end

  def string_operations do
    text = " Elixir Strings "
    %{
      trimmed: String.trim(text),
      upper: String.upcase(text),
      length: String.length(text),
      replaced: String.replace(text, " ", "_"),
      interpolated: "#{@greeting} #{@max_items} #{@ratio} #{@enabled}"
    }
  end

  def error_handling do
    try do
      raise ArgumentError, message: "boom"
    rescue
      error in ArgumentError -> Exception.message(error)
    catch
      :exit, reason -> to_string(reason)
    after
      :ok
    end
  end

  defprotocol Describable do
    @spec describe(t) :: String.t()
    def describe(value)
  end

  defimpl Describable, for: Alabaster.Sample do
    def describe(%{name: name}), do: "sample named " <> name
  end

  defimpl Describable, for: Integer do
    def describe(value), do: "integer " <> Integer.to_string(value)
  end

  defmodule Server do
    use GenServer

    @impl true
    def init(state), do: {:ok, state}

    @impl true
    def handle_call({:get, key}, _from, state) do
      {:reply, Map.get(state, key), state}
    end

    @impl true
    def handle_cast({:put, key, value}, state) do
      {:noreply, Map.put(state, key, value)}
    end
  end

  def run do
    sample = %__MODULE__{name: "elixir", age: 13, tags: [:functional, :beam]}

    IO.puts(greet("world"))
    IO.puts(classify(-5))
    IO.puts(sum([1, 2, 3]))
    IO.puts(pipeline([1, 2, 3, 4, 5]))
    IO.inspect(pattern_matching())
    IO.inspect(comprehensions())
    IO.inspect(sigils())
    IO.inspect(string_operations())
    IO.puts(error_handling())
    IO.puts(Describable.describe(sample))
    IO.puts(Describable.describe(42))
  end
end
