/* Full C syntax sample.
 * Covers the preprocessor, storage classes, types, structs, unions, enums,
 * pointers, arrays, loops, switch, varargs, dynamic memory and function pointers.
 */

#include <stdarg.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define MAX_BUFFER 256
#define SQUARE(x) ((x) * (x))
#define STRINGIFY(x) #x
#define CONCAT(a, b) a##b

typedef unsigned long ulong_t;
typedef int (*binary_op_t)(int, int);
typedef struct point point_t;

struct point {
    int x;
    int y;
};

union number {
    int i;
    float f;
    char bytes[4];
};

enum color {
    COLOR_RED = 1,
    COLOR_GREEN = 2,
    COLOR_BLUE = 4,
};

typedef struct {
    unsigned int flag_a : 1;
    unsigned int flag_b : 2;
    unsigned int rest : 5;
} bitfield_t;

static const char *GREETING = "hello, world";
static const double PI = 3.14159;
static long counter = 0L;
static _Bool enabled = true;
static char letter = 'A';

int add(int a, int b);
static double average(int count, ...);
void swap(int *a, int *b);

int add(int a, int b) {
    return a + b;
}

double average(int count, ...) {
    va_list args;
    double sum = 0.0;
    va_start(args, count);
    for (int i = 0; i < count; i++) {
        sum += va_arg(args, int);
    }
    va_end(args);
    return count > 0 ? sum / count : 0.0;
}

void swap(int *a, int *b) {
    int temp = *a;
    *a = *b;
    *b = temp;
}

static int compare(const void *left, const void *right) {
    const int *a = left;
    const int *b = right;
    return (*a > *b) - (*a < *b);
}

int main(void) {
    int numbers[] = {5, 3, 9, 1, 7};
    size_t count = sizeof(numbers) / sizeof(numbers[0]);
    char buffer[MAX_BUFFER];
    point_t origin = {0, 0};
    struct point other = {.x = 3, .y = 4};
    union number value = {.i = 42};
    enum color favorite = COLOR_GREEN;

    printf("sizeof: %zu\n", sizeof(point_t));
    snprintf(buffer, sizeof buffer, "%s (%d)", GREETING, SQUARE(7));
    puts(buffer);

    swap(&origin.x, &other.x);
    qsort(numbers, count, sizeof(int), compare);

    for (size_t i = 0; i < count; i++) {
        printf("%d ", numbers[i]);
    }
    putchar('\n');

    int j = 0;
    while (j < 3) {
        j++;
    }
    do {
        j--;
    } while (j > 0);

    switch (favorite) {
        case COLOR_RED:
            puts("red");
            break;
        case COLOR_GREEN:
            puts("green");
            break;
        default:
            puts("other");
            break;
    }

    int *dynamic = malloc(10 * sizeof(int));
    if (dynamic == NULL) {
        return EXIT_FAILURE;
    }
    memset(dynamic, 0, 10 * sizeof(int));
    free(dynamic);

    binary_op_t operation = add;
    printf("%d %.2f\n", operation(2, 3), average(3, 1, 2, 3));
    return EXIT_SUCCESS;
}
