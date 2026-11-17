# Full Markdown syntax sample

A fixture covering CommonMark plus GitHub Flavored Markdown for the theme's
syntax-highlighting tests.

## Headings

Heading level two
-----------------

### Heading level three

#### Heading level four

##### Heading level five

###### Heading level six

## Emphasis and inline markup

*emphasis*, _also emphasis_, **strong**, __also strong__, ***both***,
~~strikethrough~~, `inline code`, and a [link](https://example.com "Title").

An autolink: <https://example.com/path?query=value> and an email:
<mailto:hello@example.com>.

An escaped \*asterisk\*, \_underscore\_, \`backtick\`, and \# hash.

## Links and images

[Inline link](https://example.com) and [reference link][ref].

[ref]: https://example.com/reference "Reference title"

![Alt text](/assets/image.png "Image title")

![Reference image][img]

[img]: /assets/reference.png

## Lists

- Unordered item
- Another item
  - Nested item
    - Deeper item
- Item with **strong** and `code`

1. Ordered item
2. Second item
   1. Nested ordered item
   2. Another nested item
3. Third item

- [x] Completed task
- [ ] Open task
- [ ] Task with a [link](https://example.com)

## Block quotes

> A block quote.
>
> > A nested block quote.
>
> Back to the first level.
>
> - A list inside a quote
> - Another item

## Code

An indented code block:

    indented code
    with a second line

A fenced code block:

```kotlin
fun greet(name: String): String = "Hello, $name"
```

A fenced block with no language and tildes:

~~~
plain text
~~~

## Tables

| Left aligned | Center aligned | Right aligned |
| :----------- | :------------: | ------------: |
| alpha        |      beta      |         gamma |
| delta        |     epsilon    |           zeta |
| `code`       | **strong**     | [link](https://example.com) |

## Horizontal rules

---

***

___

## HTML

<div class="note" data-kind="warning">
  <p>Inline <strong>HTML</strong> is allowed in Markdown.</p>
</div>

Inline <kbd>Ctrl</kbd> + <kbd>C</kbd> and <abbr title="HyperText Markup Language">HTML</abbr>.

## Footnotes

A sentence with a footnote reference.[^1]

[^1]: The footnote definition.

## Miscellaneous

- Emoji shortcodes: :sparkles: :rocket:
- A bare URL: https://example.com
- A hard line break at the end of this line  
  continues here.
- A definition-style list term

  : definition (not part of CommonMark, but commonly supported)

> [!NOTE]
> A GitHub alert block.
