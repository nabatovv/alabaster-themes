// Full Go syntax sample.
//
// Coverage note: a tour of Go 1.27 syntax for the theme highlighting tests:
// imports (grouped/aliased/blank/dot), constants (typed, untyped, iota, bit
// shifts), variables (multiple, inferred, zero values), basic types, literals
// (raw/interpreted strings, runes, hex/octal/binary, underscores, imaginary),
// pointers, arrays, slices, maps, structs (embedded, tags, anonymous), methods,
// interfaces (embedding, empty, assertions, type switches), type sets and
// generics (constraints, comparable, ~int, generic types and aliases),
// functions (named returns, variadics, closures, defer, panic/recover, init),
// control flow (if with init, all for variants, labels, switch, select, goto),
// goroutines, channels, sync (Mutex/RWMutex/WaitGroup.Go/Once/atomic), context,
// errors (New/Is/As/Join, custom types, wrapping), strings/runes/bytes,
// strconv, fmt, sort/slices/maps, time, io/os, embed with //go:embed,
// iterators (iter.Seq, range-over-func) and the builtins min/max/clear.
package main

import (
	"bytes"
	"context"
	"embed"
	"errors"
	"fmt"
	"io"
	"iter"
	"maps"
	"os"
	"slices"
	"sort"
	"strconv"
	"strings"
	"sync"
	"sync/atomic"
	"time"
	"unicode/utf8"

	. "math/cmplx"       // dot import: Abs/Conj/... land in file scope
	xrand "math/rand/v2" // aliased import
	_ "unsafe"           // blank import: side effects only
)

// Constants: typed, untyped, iota, bit shifts and literal forms.
const (
	Pi     float64    = 3.14159_26535_89793
	Name              = "sample"
	Cmd    rune       = '⌘'
	Mask              = 0xDEAD_BEEF
	Perm              = 0o755 // octal
	Bits              = 0b1010_1010
	Small             = 0x1p-2 // hex float
	Shifty            = 1 << 40
	Imag   complex128 = 1 + 2i
)

const (
	KindUnknown Kind = iota
	KindString
	KindNumber
	KindBool
)

// Variables: zero values, inference and multiple assignment.
var (
	zeroInt   int
	zeroStr   string
	zeroBool  bool
	zeroPtr   *int
	zeroMap   map[string]int
	zeroSlice []byte
	zeroChan  chan struct{}
	zeroErr   error

	inferred         = 42
	paired1, paired2 = 7, "seven"
	derived          = inferred * 2
	initialized      bool
)

//go:embed sample.go
var embedSource string

//go:embed *.go
var embedFS embed.FS

// ErrNotFound is a sentinel error; ValidationError wraps its cause.
var ErrNotFound = errors.New("not found")

type ValidationError struct {
	Field string
	Err   error
}

func (e *ValidationError) Error() string { return e.Field + ": " + e.Err.Error() }
func (e *ValidationError) Unwrap() error { return e.Err }

// Kind is a defined type whose String method has a value receiver.
type Kind int

func (k Kind) String() string { return [...]string{"unknown", "string", "number", "bool"}[k] }

// Base is embedded into Document; Label has a value receiver.
type Base struct {
	ID   int64  `json:"id" db:"pk"`
	Name string `json:"name,omitempty"`
}

func (b Base) Label() string { return b.Name + "#" + strconv.FormatInt(b.ID, 10) }

func (b Base) Draw() string { return fmt.Sprintf("%T(%.1f)", b, float64(b.ID)) }

// Document embeds Base and holds anonymous struct and tagged fields.
type Document struct {
	Base
	Tags []string `json:"tags"`
	Meta struct {
		Author string `json:"author"`
		Words  int    `json:"words"`
	}
	mu sync.Mutex
}

// AddTag uses a pointer receiver; the mutex field is never copied.
func (d *Document) AddTag(tag string) { d.mu.Lock(); defer d.mu.Unlock(); d.Tags = append(d.Tags, tag) }

// Labeller is an interface; Numeric and Ordered are constraints.
type Labeller interface{ Label() string }

type Numeric interface {
	~int | ~int8 | ~int16 | ~int32 | ~int64 |
		~uint | ~uint8 | ~uint16 | ~uint32 | ~uint64 |
		~float32 | ~float64
}

type Ordered interface {
	comparable
	Numeric
}

// NumberSlice is a generic defined type; SeqAlias is a generic type alias.
type NumberSlice[T Numeric] []T

type SeqAlias[T any] = iter.Seq[T]

// Sum is a generic variadic function.
func Sum[T Numeric](values ...T) (total T) {
	for _, v := range values {
		total += v
	}
	return
}

// squares is a range-over-func iterator; doubled uses defer and a named result.
func squares(n int) iter.Seq[int] {
	return func(yield func(int) bool) {
		for i := 1; i <= n; i++ {
			if !yield(i * i) {
				return
			}
		}
	}
}

func doubled(n int) (out int) { defer func() { out *= 2 }(); return n }

// safeDiv turns a panic into an error.
func safeDiv(a, b int) (result int, err error) {
	defer func() {
		if r := recover(); r != nil {
			err = fmt.Errorf("recovered: %v", r)
		}
	}()
	return a / b, nil
}

func init() { initialized = true }

func main() {
	// Basic types and literals.
	var (
		flag   bool       = true
		text   string     = "café ☕"
		letter rune       = 'é'
		octet  byte       = 0x7f
		small8 int8       = -1
		big64  int64      = 1 << 40
		flt64  float64    = Small
		cplx8  complex128 = Imag * 2
	)
	fmt.Printf("%t %q %q %x %d %d %.1f %v\n", flag, text, letter, octet, small8, big64, flt64, cplx8)
	raw := `first line
second "quoted" \t stays literal
{"json": [1, 2, 3]}`
	interpreted := "escapes: \t \"quoted\" \\ \u00e9 \x41"
	fmt.Println(len(raw), len(interpreted), strings.Contains(raw, "json"), strconv.Quote(interpreted))

	// Strings, runes, bytes and strconv.
	head, tail, found := strings.Cut(text, " ")
	if n, err := strconv.Atoi("41"); err == nil {
		fmt.Println(head, tail, found, n, strconv.Itoa(Bits), utf8.RuneCountInString(text))
	}
	buf := bytes.Buffer{}
	buf.WriteString(text)
	fmt.Println(buf.Len(), bytes.Count(buf.Bytes(), []byte("é")), Abs(cplx8), strings.ToUpper(tail))

	// Pointers.
	answer := 42
	ptr := &answer
	*ptr++
	fmt.Printf("%d %p %d\n", answer, ptr, **(&ptr))

	// Arrays, slices, 3-index slicing and copy.
	array := [...]string{"a", "b", "c"}
	nums := make([]int, 0, 8)
	nums = append(nums, 5, 3, 9, 1)
	three := nums[0:2:3]
	fmt.Println(array, copy(nums[:2], []int{2, 4}), three, cap(three), slices.Contains(nums, 9))

	// Maps: literal, make, comma-ok, delete, clear.
	ages := map[string]int{"ada": 36, "grace": 45}
	ages["alan"] = 41
	v, ok := ages["alan"]
	delete(ages, "ada")
	spare := make(map[string]struct{}, 4)
	spare["token"] = struct{}{}
	clear(spare)
	fmt.Println(v, ok, len(spare), slices.Sorted(maps.Keys(ages)), maps.Clone(ages) != nil)

	// Structs, embedding and methods.
	doc := &Document{Base: Base{ID: 1, Name: "notes"}, Tags: []string{"go"}}
	doc.Meta.Author, doc.Meta.Words = "vlad", 3
	doc.AddTag("syntax")
	fmt.Println(doc.Label(), doc.Draw(), doc.Tags, KindNumber, struct{ X, Y int }{1, 2})

	// Type assertion and a type switch.
	var lab Labeller = doc
	base, isDoc := lab.(*Document)
	var boxed any = doc
	switch t := boxed.(type) {
	case Labeller:
		fmt.Println("labeller:", t.Label(), isDoc, base.Name)
	case NumberSlice[int]:
		fmt.Println("numbers:", Sum(t...))
	default:
		fmt.Printf("unexpected %T\n", t)
	}

	// Errors: wrapping, Is, As and Join.
	wrapped := fmt.Errorf("lookup %q: %w", "nobody", ErrNotFound)
	joined := errors.Join(ErrNotFound, &ValidationError{Field: "name", Err: errors.New("short")})
	var verr *ValidationError
	ok = errors.As(joined, &verr)
	fmt.Println(errors.Is(wrapped, ErrNotFound), ok, verr.Field)

	// Control flow: every for variant, labels and goto.
	for i := 0; i < 2; i++ {
		_ = i
	}
	for n := 2; n > 0; n-- {
		_ = n
	}
	for idx, val := range array {
		_, _ = idx, val
	}
	for key, val := range ages {
		_, _ = key, val
	}
	for pos, r := range "héllo" {
		_, _ = pos, r
	}
	for sq := range squares(3) {
		_ = sq
	}
outer:
	for row := range 3 {
		for range 3 {
			if row == 2 {
				break outer
			}
			continue outer
		}
	}
	hops := 0
again:
	hops++
	if hops < 3 {
		goto again
	}

	// Generics, closures and the min/max builtins.
	fmt.Println(Sum(NumberSlice[int]{1, 2, 3, 4}...), Sum(1, 2, 3), Sum[float64](1.5, 2.5))
	next := func() func() int {
		n := 0
		return func() int { n++; return n }
	}()
	var seq SeqAlias[int] = squares(3)
	for s := range seq {
		fmt.Println("seq:", s, next(), doubled(21), min(3, 7), max(2.5, Small), min("a", "b"))
	}

	// Goroutines, channels, select, context and sync.
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	jobs, results := make(chan int), make(chan int, 4) // unbuffered and buffered
	var wg sync.WaitGroup
	wg.Go(func() { // WaitGroup.Go (Go 1.25)
		for job := range jobs {
			select {
			case results <- job * job:
			case <-ctx.Done():
				return
			}
		}
	})
	go func() {
		defer close(jobs)
		for i := range 4 {
			jobs <- i
		}
	}()
	var hits atomic.Int64
	summed := 0
	for range 4 {
		summed += <-results
		hits.Add(1)
	}
	wg.Wait()
	msgs := make(chan string, 1)
	msgs <- "buffered message"
	close(msgs)
	for msg := range msgs {
		fmt.Println(msg)
	}
	var mu sync.Mutex
	var rw sync.RWMutex
	var once sync.Once
	tally := 0
	once.Do(func() { tally = 1 })
	mu.Lock()
	tally++
	mu.Unlock()
	rw.RLock()
	reading := tally
	rw.RUnlock()
	fmt.Println(reading, hits.Load(), summed, xrand.IntN(6)+1)

	// Time.
	start := time.Now()
	time.Sleep(time.Millisecond)
	fmt.Println(start.UTC().Format(time.RFC3339) != "", time.Since(start) > 0)

	// io and os, guarded so the sample only has to parse.
	dir, dirErr := os.Getwd()
	data, readErr := os.ReadFile("sample.go")
	n, writeErr := io.WriteString(io.Discard, text)
	fmt.Println(dir != "", dirErr == nil, len(data), readErr == nil, n, writeErr == nil)

	// Embedded data.
	entries, embedErr := embedFS.ReadDir(".")
	fmt.Println("embedded:", len(entries), embedErr == nil, strings.Contains(embedSource, "Full Go syntax sample."))

	// sort and slices.
	names, scores := []string{"grace", "ada", "alan"}, []int{9, 2, 5}
	sort.Strings(names)
	sort.Slice(names, func(i, j int) bool { return names[i] < names[j] })
	slices.Sort(names)
	slices.SortFunc(scores, func(a, b int) int { return b - a })
	fmt.Println(names, scores, slices.Min(scores), slices.Max(scores))

	if r, err := safeDiv(1, 0); err != nil {
		fmt.Println("division:", r, err)
	}
	fmt.Println(safeDiv(9, 3), derived, paired1, paired2, zeroInt, zeroStr, zeroBool,
		zeroPtr == nil, zeroMap == nil, zeroSlice == nil, zeroChan == nil, zeroErr == nil,
		initialized, Mask|Bits|Perm|Shifty, Name, Cmd)
}
