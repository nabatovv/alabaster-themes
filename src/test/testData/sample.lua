-- Full Lua syntax sample.
-- Covers locals, tables, functions, varargs, multiple returns, metatables,
-- coroutines, modules, string/math libraries, loops and error handling.

local MAX_ITEMS = 1024
local GREETING = "hello"
local ENABLED = true
local RATIO = 1.5e3

local numbers = { 1, 2, 3, 42 }
local settings = { host = "localhost", port = 8080, [1] = "first" }
local matrix = { { 1, 2 }, { 3, 4 } }

local Point = {}
Point.__index = Point

function Point.new(x, y)
  return setmetatable({ x = x, y = y }, Point)
end

function Point:translate(dx, dy)
  self.x = self.x + dx
  self.y = self.y + dy
  return self
end

function Point:__tostring()
  return string.format("(%d, %d)", self.x, self.y)
end

function Point:__add(other)
  return Point.new(self.x + other.x, self.y + other.y)
end

local Stack = {}
Stack.__index = Stack

function Stack.new()
  return setmetatable({ items = {} }, Stack)
end

function Stack:push(...)
  for _, value in ipairs({ ... }) do
    table.insert(self.items, value)
  end
  return self
end

function Stack:pop()
  return table.remove(self.items)
end

function greet(name, ...)
  local punctuation = select("#", ...) > 0 and "!" or "."
  return GREETING .. ", " .. (name or "world") .. punctuation
end

local function sum(...)
  local total = 0
  for _, value in ipairs({ ... }) do
    total = total + value
  end
  return total
end

local function classify(value)
  if type(value) == "number" then
    if value == 0 then
      return "zero"
    elseif value < 0 then
      return "negative"
    end
    return "positive"
  elseif type(value) == "string" then
    return "text"
  end
  return "unknown"
end

local function safe_divide(a, b)
  if b == 0 then
    error("division by zero", 2)
  end
  return a / b
end

local function producer()
  return coroutine.create(function()
    for index = 1, 3 do
      coroutine.yield(index)
    end
    return "done"
  end)
end

local function iterate()
  for index, value in ipairs(numbers) do
    print(index, value)
  end

  for key, value in pairs(settings) do
    print(key, value)
  end

  local counter = 0
  while counter < 3 do
    counter = counter + 1
  end

  repeat
    counter = counter - 1
  until counter == 0
end

local function string_operations()
  local text = "  Lua strings  "
  local trimmed = text:gsub("^%s+", ""):gsub("%s+$", "")
  return trimmed:upper(), #text, string.format("%q", trimmed), trimmed:find("strings")
end

local function safe_call()
  local ok, result = pcall(safe_divide, 1, 0)
  if not ok then
    print("caught: " .. tostring(result))
  end
  return ok
end

local function main()
  local point = Point.new(1, 2):translate(3, 4)
  local other = Point.new(1, 1)
  local stack = Stack.new():push("a", "b", "c")

  iterate()
  string_operations()
  safe_call()

  local thread = producer()
  local values = {}
  while true do
    local ok, value = coroutine.resume(thread)
    if not ok or value == "done" then
      break
    end
    table.insert(values, value)
  end

  print(tostring(point + other), stack:pop(), stack:pop())
  print(greet("lua"), sum(1, 2, 3), classify(-2), math.max(1, 2), math.floor(RATIO))
  print(table.concat(values, ","), MAX_ITEMS, ENABLED, #matrix, math.sqrt(16))
end

main()
