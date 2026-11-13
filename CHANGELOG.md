<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Alabaster Theme Changelog

## [Unreleased]

## [1.0.103]

- PHP and the shared (all other languages) annotator highlight function/method definitions again; call sites keep the default text colour
- The function colour is back on `DEFAULT_FUNCTION_DECLARATION` in all three themes

## [1.0.102]

- Restored the previous behaviour of the Java, Kotlin, Scala, Go, Clojure and JavaScript annotators

## [1.0.101]

- Function and method highlighting now applies to call sites only: definitions keep the default text colour
- The function colour moved from `DEFAULT_FUNCTION_DECLARATION` to `DEFAULT_FUNCTION_CALL` in all three themes
- Call highlighting is implemented per language for PHP (FunctionReference/MethodReference), Java, Kotlin, Scala, Go, JavaScript and Clojure, and heuristically by the shared annotator for every other language

## [1.0.100]

- The shared annotator is now registered implicitly for every language, so newly installed or unknown languages get the Alabaster highlighting automatically; languages with a dedicated annotator (Java, Kotlin, Scala, Clojure, Go, JavaScript, PHP, XML) are skipped to avoid duplicate annotations

## [1.0.99]

- Highlighting for the remaining dynamic functional languages: Erlang, Julia, Perl, Raku, Groovy, PowerShell, Scheme, Racket, Lisp, Smalltalk and Prolog
- The shared annotator is now registered for every other language available through JetBrains IDE language plugins (Haskell, OCaml, F#, VB.NET, Objective-C, Ada, Fortran, COBOL, MATLAB, Wolfram, Solidity, VHDL/Verilog/SystemVerilog, Terraform/HCL, Dockerfile, Makefile, CMake, Nix, Zig, Nim, Crystal, D, Elm, CoffeeScript, Sass/SCSS/LESS/Stylus, Pug/Jade, Handlebars/Mustache, Vue, Svelte, HTML, JSON, YAML, TOML, Markdown, Properties, Gherkin, AsciiDoc, GraphQL, Protobuf, RegExp, XPath/XQuery, FreeMarker, Velocity, Thymeleaf, Liquid, Twig, Batch, T-SQL, PL/SQL, Nginx, Thrift, GLSL/HLSL, reStructuredText, Cython and Assembly)

## [1.0.98]

- The plugin now targets IntelliJ IDEA Community 2026.3 (the latest release) instead of IntelliJ IDEA Ultimate

## [1.0.97]

- Highlighting for 15 more languages: Rust, C, C++, Python, TypeScript, C#, Ruby, Shell Script, Swift, Dart, R, Lua, Elixir, CSS and SQL
- Full-syntax sample files for every newly supported language

## [1.0.96]

- PHP support: function declarations, constants and numeric literals, and strings are highlighted like in the other supported languages
- Multi-line and doc comments now use the same color as single-line comments
- Only the latest IntelliJ IDEA release (2026.3) is supported

## [1.0.95]

- Fixed plugin loading failure on modern JDKs (`VerifyError` in `isDumbAware()` of the Clojure-generated classes)
- Compatibility with the latest JetBrains IDE releases (up to 2026.3)
- Updated all dependencies to their latest versions

## [1.0.93]

- Vibe coding fixes by Deepseek

## [1.0.92]

- Go support

## [1.0.91]

- Update theme to Light with Light Header and Dark UIs defaults.

## [1.0.90]

- Fixed broken Clojure support after Cursive's upgrade.

## [1.0.83]

- Strings' content background color in BG heme stays the same when the line is highlighted by IDE, previously background changed to line highlight color.
- String's quotation marks' background color in dynamic now and changes to a line highlight color, previously it was always of a default background color.
- String quotations marks rendering speed improvement. 
- Better Scala support: strings, constants.

## [1.0.82]

- Updated changelog for previous versions.

## [1.0.81]

- Svelte support, BG delimiter background fix, etc.

## [1.0.73]

- Better Javascript highlighting by virtue of [Nik Lopin](https://github.com/nlopin).

## [1.0.60]

- Fix Java's metadata in the dark theme

## [1.0.59]

- Improvements to console in the dark theme

## [1.0.58]

- The dark theme is darker

## [1.0.57]

- White color text for the dark theme

## [1.0.56]

- Even better comment's color in dark theme

## [1.0.55]

- Better comment's color in dark theme
- [1.0.54]
- Default theme fix

## [1.0.53]

- BG theme improvements

## [1.0.52]

- Many small improvements

## [1.0.51]

- Fancier Clojure highlighting

## [1.0.50]

- Fancy strings, XML (HTML, etc.) improvements, Kotlin constants

## [1.0.42]

- Warning highlighting improvements, etc.

## [1.0.41]

- Better highlighting for XML, JSON, etc.

## [1.0.40]

- Cleanup

## [1.0.39]

- Proper theme names

## [1.0.38]

- Support the new versions of IDEA

## [1.0.37]

- Better highlighting for the light themes

## [1.0.36]

- More fixes

## [1.0.35]

- Fixed second light theme

## [1.0.34]

- Better highlighting for Clojure light themes

## [1.0.33]

- Cleanup

## [1.0.32]

- Added second light theme
- Non Clojure STD macro calls are in italic font style

## [1.0.31]

- Updated to 2023-IU

## [1.0.30]

- Cleanup

## [1.0.29]

- Light and dark themes bundled in one plugin

## [1.0.25]

- Python improved

[Unreleased]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.103...HEAD
[1.0.103]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.102...v1.0.103
[1.0.102]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.101...v1.0.102
[1.0.101]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.100...v1.0.101
[1.0.100]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.99...v1.0.100
[1.0.99]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.98...v1.0.99
[1.0.98]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.97...v1.0.98
[1.0.97]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.96...v1.0.97
[1.0.96]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.95...v1.0.96
[1.0.95]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.93...v1.0.95
[1.0.94]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.93...v1.0.94
[1.0.93]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.92...v1.0.93
[1.0.92]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.91...v1.0.92
[1.0.91]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.90...v1.0.91
[1.0.90]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.83...v1.0.90
[1.0.83]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.82...v1.0.83
[1.0.82]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.81...v1.0.82
[1.0.81]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.73...v1.0.81
[1.0.73]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.60...v1.0.73
[1.0.60]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.59...v1.0.60
[1.0.59]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.58...v1.0.59
[1.0.58]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.57...v1.0.58
[1.0.57]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.56...v1.0.57
[1.0.56]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.55...v1.0.56
[1.0.55]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.53...v1.0.55
[1.0.53]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.52...v1.0.53
[1.0.52]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.51...v1.0.52
[1.0.51]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.50...v1.0.51
[1.0.50]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.42...v1.0.50
[1.0.42]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.41...v1.0.42
[1.0.41]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.40...v1.0.41
[1.0.40]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.39...v1.0.40
[1.0.39]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.38...v1.0.39
[1.0.38]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.37...v1.0.38
[1.0.37]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.36...v1.0.37
[1.0.36]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.35...v1.0.36
[1.0.35]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.34...v1.0.35
[1.0.34]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.33...v1.0.34
[1.0.33]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.32...v1.0.33
[1.0.32]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.31...v1.0.32
[1.0.31]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.30...v1.0.31
[1.0.30]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.29...v1.0.30
[1.0.29]: https://github.com/nabatovv/alabaster-themes/compare/v1.0.25...v1.0.29
[1.0.25]: https://github.com/nabatovv/alabaster-themes/commits/v1.0.25
