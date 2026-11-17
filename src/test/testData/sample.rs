//! Full Rust syntax sample.
//! Covers modules, items, generics, traits, lifetimes, patterns, closures,
//! iterators, error handling, macros, async and tests.

use std::collections::HashMap;
use std::fmt::{self, Display};
use std::sync::{Arc, Mutex};

pub const MAX_ITEMS: usize = 1_000;
pub static GREETING: &str = "hello";
const HEX: u32 = 0xDEAD_BEEF;
const FLOAT: f64 = 1.5e3;

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct Point<T> {
    pub x: T,
    pub y: T,
}

pub struct Unit;

pub struct Pair(pub i32, pub String);

impl<T: Copy + std::ops::Add<Output = T>> Point<T> {
    pub fn new(x: T, y: T) -> Self {
        Self { x, y }
    }

    pub fn sum(&self) -> T {
        self.x + self.y
    }
}

impl<T: Display> Display for Point<T> {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "({}, {})", self.x, self.y)
    }
}

#[derive(Debug)]
enum Shape {
    Circle { radius: f64 },
    Rectangle(f64, f64),
    Unit,
}

impl Shape {
    fn area(&self) -> f64 {
        match self {
            Shape::Circle { radius } => 3.14159 * radius * radius,
            Shape::Rectangle(w, h) => w * h,
            Shape::Unit => 0.0,
        }
    }
}

pub trait Drawable {
    fn draw(&self);
    fn describe(&self) -> String {
        String::from("a drawable")
    }
}

impl Drawable for Shape {
    fn draw(&self) {
        println!("drawing {:?}", self);
    }
}

pub fn generic_max<T: PartialOrd + Copy>(items: &[T]) -> Option<T> {
    let mut max = *items.first()?;
    for &item in items.iter().skip(1) {
        if item > max {
            max = item;
        }
    }
    Some(max)
}

fn longest<'a>(left: &'a str, right: &'a str) -> &'a str {
    if left.len() >= right.len() { left } else { right }
}

fn classify(value: i32) -> &'static str {
    match value {
        0 => "zero",
        1..=9 => "single digit",
        n if n < 0 => "negative",
        _ => "large",
    }
}

fn iterators() -> Vec<i32> {
    let numbers = vec![1, 2, 3, 4, 5];
    numbers
        .iter()
        .filter(|n| **n % 2 == 0)
        .map(|n| n * 2)
        .collect()
}

fn closures() {
    let add = |a: i32, b: i32| a + b;
    let mut counter = 0;
    let mut increment = || {
        counter += 1;
        counter
    };
    println!("{} {} {}", add(1, 2), increment(), counter);
}

fn control_flow() {
    let mut total = 0;
    for i in 0..10 {
        total += i;
    }
    let mut n = 10;
    while n > 0 {
        n -= 1;
    }
    loop {
        break;
    }
    if let Some(value) = Some(42) {
        println!("{}", value);
    }
    let label = 'outer: loop {
        loop {
            break 'outer "done";
        }
    };
    println!("{}", label);
}

fn error_handling() -> Result<HashMap<String, i32>, String> {
    let mut map = HashMap::new();
    let parsed: i32 = "42".parse().map_err(|e| format!("{}", e))?;
    map.insert(GREETING.to_string(), parsed);
    Ok(map)
}

fn ownership() {
    let mut shared = Arc::new(Mutex::new(Vec::new()));
    let clone = Arc::clone(&shared);
    let handle = std::thread::spawn(move || {
        clone.lock().unwrap().push(1);
    });
    handle.join().unwrap();
    shared.lock().unwrap().push(2);
}

async fn fetch(value: u32) -> Option<String> {
    Some(value.to_string())
}

macro_rules! square {
    ($x:expr) => {
        $x * $x
    };
}

unsafe fn raw_pointer() -> i32 {
    let value = 5;
    let pointer = &value as *const i32;
    *pointer
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn it_works() {
        assert_eq!(square!(4), 16);
        assert_eq!(generic_max(&[1, 5, 3]), Some(5));
        assert_eq!(longest("abc", "de"), "abc");
    }
}

fn main() {
    println!("{}", square!(3));
}
