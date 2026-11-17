# Full R syntax sample.
# Covers vectors, factors, matrices, lists, data frames, functions, apply
# family, control flow, strings, formulas, S3 classes and NA handling.

MAX_ITEMS <- 1024L
GREETING <- "hello"
ENABLED <- TRUE
RATIO <- 1.5e3
MISSING <- NA

numbers <- c(1, 2, 3, 42)
mixed <- c(1L, 2.5, TRUE, "text")
sequence_values <- seq(1, 10, by = 2)
repeated <- rep(c("a", "b"), times = 3)

vector_math <- numbers * 2 + 1
logical <- numbers > 2 & !is.na(numbers)

colors <- factor(c("red", "green", "red"), levels = c("red", "green", "blue"))

matrix_values <- matrix(1:9, nrow = 3, ncol = 3, byrow = TRUE)
transposed <- t(matrix_values)
product <- matrix_values %*% t(matrix_values)

person <- list(name = "alice", age = 30L, tags = c("admin", "dev"))
data <- data.frame(
  name = c("alice", "bob", "carol"),
  age = c(30L, 17L, 45L),
  score = c(1.5, 2.5, 3.5),
  stringsAsFactors = FALSE
)

subset_data <- data[data$age >= 18, c("name", "score")]

greet <- function(name = "world", punctuation = "!") {
  paste0(GREETING, ", ", name, punctuation)
}

sum_values <- function(...) {
  values <- list(...)
  Reduce(`+`, values)
}

classify <- function(value) {
  if (is.null(value)) {
    "null"
  } else if (length(value) > 1) {
    "vector"
  } else if (is.na(value)) {
    "missing"
  } else if (value == 0) {
    "zero"
  } else if (value < 0) {
    "negative"
  } else {
    "positive"
  }
}

describe_shape <- function(shape) {
  UseMethod("describe_shape")
}

describe_shape.circle <- function(shape) {
  paste("circle with radius", shape$radius)
}

describe_shape.rectangle <- function(shape) {
  paste("rectangle", shape$width, "x", shape$height)
}

create_circle <- function(radius) {
  structure(list(radius = radius), class = "circle")
}

control_flow <- function(limit) {
  total <- 0
  for (index in seq_len(limit)) {
    total <- total + index
  }

  count <- 0
  while (count < limit) {
    count <- count + 1
  }

  repeat {
    count <- count - 1
    if (count <= 0) break
  }

  switch(as.character(limit),
    "0" = "none",
    "1" = "one",
    "many"
  )
}

apply_family <- function(values) {
  vapply(values, function(value) value * 2, numeric(1))
}

model <- lm(score ~ age, data = data)
coefficients_values <- coef(model)
summary_text <- summary(model)

strings <- paste(c("a", "b"), collapse = "-")
formatted <- sprintf("%s has %d items", GREETING, length(numbers))
trimmed <- trimws("  padded  ")
upper <- toupper(GREETING)

set.seed(42)
random_values <- rnorm(5, mean = 0, sd = 1)

cat(formatted, "\n")
print(unique(colors))
print(subset_data)
print(seq_along(numbers))
print(apply_family(numbers))
print(sapply(data$age, classify))
print(control_flow(3))
print(describe_shape(create_circle(2)))
print(coefficients_values)
print(stringr_placeholder <- paste(strings, trimmed, upper, sep = " | "))
print(sum_values(1, 2, 3))
print(list(vector_math = vector_math, logical = logical, matrix = matrix_values[1, ]))
print(list(transposed = transposed[1, ], product = product[1, ]))
print(c(mixed[1], as.numeric(mixed[2]), as.logical(mixed[3]), mixed[4]))
print(is.na(MISSING), length(repeated), length(sequence_values), MAX_ITEMS, RATIO, ENABLED)
