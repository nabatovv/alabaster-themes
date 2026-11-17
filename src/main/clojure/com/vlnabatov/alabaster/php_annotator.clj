(ns com.vlnabatov.alabaster.php-annotator
  (:require [clojure.string :as string])
  (:import (com.intellij.lang.annotation AnnotationHolder HighlightSeverity)
           (com.intellij.openapi.editor DefaultLanguageHighlighterColors HighlighterColors)
           (com.intellij.openapi.editor.colors EditorColorsManager TextAttributesKey)
           (com.intellij.openapi.editor.markup TextAttributes)
           (com.intellij.psi PsiComment PsiElement)
           (com.intellij.psi.tree IElementType)
           (com.jetbrains.php.lang.highlighter PhpHighlightingData)
           (com.jetbrains.php.lang.lexer PhpTokenTypes)
           (com.jetbrains.php.lang.psi.elements ClassConstantReference ConstantReference Function FunctionReference PhpEnumCase PhpNamedElement PhpReference StringLiteralExpression)
           (com.vlnabatov.alabaster UtilKt)
           (java.awt Font))
  (:gen-class :implements [com.intellij.lang.annotation.Annotator
                           com.intellij.openapi.project.PossiblyDumbAware]
              :name       com.vlnabatov.alabaster.extensions.annotation.PhpAnnotator
              :prefix     "-"))


(def constant-values #{"true" "false" "null"})


(defn- annotate-as
  [^AnnotationHolder holder ^TextAttributesKey key]
  (-> holder
      (.newSilentAnnotation HighlightSeverity/INFORMATION)
      (.textAttributes key)
      (.create)))


(defn- scheme
  []
  (.getGlobalScheme (EditorColorsManager/getInstance)))


;; The scheme resolves fallback keys, so this also yields the inherited text
;; colour for schemes that do not define their own identifiers.
(defn- scheme-attributes
  [^TextAttributesKey key]
  (some-> (.getAttributes (scheme) key) (.clone)))


(defn- annotate-as-enforced
  [^AnnotationHolder holder ^TextAttributesKey key]
  (if-let [attributes (scheme-attributes key)]
    (-> holder
        (.newSilentAnnotation HighlightSeverity/INFORMATION)
        (.enforcedTextAttributes attributes)
        (.create))
    (annotate-as holder key)))


;; Variables, function/method calls (including the standard library ones) and
;; keywords keep the default text colour instead of the colour the PHP plugin's
;; syntax highlighter assigns to them. The foreground is taken from the TEXT key
;; of the selected editor scheme: asking the scheme for its default foreground
;; alone yields nil for some schemes, which silently made these annotations a
;; no-op.
(defn- annotate-as-default-text
  [^AnnotationHolder holder]
  (let [colors (scheme)
        foreground (or (some-> (.getAttributes colors HighlighterColors/TEXT) (.getForegroundColor))
                       (some-> (.getAttributes colors DefaultLanguageHighlighterColors/IDENTIFIER) (.getForegroundColor))
                       (.getDefaultForeground colors))]
    (-> holder
        (.newSilentAnnotation HighlightSeverity/INFORMATION)
        (.enforcedTextAttributes (TextAttributes. foreground nil nil nil Font/PLAIN))
        (.create))))


(defn- separators?
  [^String text]
  (or (and (string/starts-with? text "'") (string/ends-with? text "'"))
      (and (string/starts-with? text "\"") (string/ends-with? text "\""))))


(defn- magic-constant?
  [^String text]
  (and (> (count text) 3)
       (string/starts-with? text "__")
       (string/ends-with? text "__")))


(defn- constant-reference?
  [element-type ^PsiElement element]
  (or (= element-type PhpTokenTypes/PREDEFINED_IDENTIFIER)
      (instance? ConstantReference (.getParent element))))


(defn- constant-value?
  [^PsiElement element]
  (contains? constant-values (string/lower-case (.getText element))))


(defn- function-declaration?
  [^PsiElement element]
  (let [parent (.getParent element)]
    (and (instance? Function parent)
         (identical? (.getNode element) (.getNameNode ^PhpNamedElement parent)))))


;; Method calls implement FunctionReference as well, so this covers both plain
;; function calls and $object->method() / Class::method() call sites.
(defn- function-call?
  [^PsiElement element]
  (instance? FunctionReference (.getParent element)))


(defn- enum-case-declaration?
  [^PsiElement element]
  (let [parent (.getParent element)]
    (and (instance? PhpEnumCase parent)
         (identical? (.getNode element) (.getNameNode ^PhpNamedElement parent)))))


(defn- enum-case-reference?
  [^PsiElement element]
  (let [parent (.getParent element)]
    (and (instance? ClassConstantReference parent)
         (identical? (.getNode element) (.getNameNode ^PhpReference parent))
         (try
           (instance? PhpEnumCase (.resolve ^PhpReference parent))
           (catch Throwable _ false)))))


(defn- in-comment?
  [^PsiElement element]
  (loop [current element]
    (cond
      (nil? current) false
      (instance? PsiComment current) true
      :else (recur (.getParent current)))))


(defn- variable?
  [element-type]
  (or (= element-type PhpTokenTypes/VARIABLE)
      (= element-type PhpTokenTypes/VARIABLE_NAME)
      (= element-type PhpTokenTypes/VARIABLE_VARIABLE)))


(defn- php-keyword?
  [element-type]
  (.contains PhpTokenTypes/tsKEYWORDS element-type))


(defn -annotate

  [_ ^PsiElement element ^AnnotationHolder holder]
  (let [^IElementType element-type (.. element getNode getElementType)]
    (cond
      (instance? StringLiteralExpression element) (when (and (not (.isHeredoc ^StringLiteralExpression element))
                                                             (separators? (.getText element)))
                                                    (UtilKt/annotateSeparationMarks element holder))

      (variable? element-type) (annotate-as-default-text holder)

      (function-call? element) (annotate-as-default-text holder)

      ;; enum cases are constants to the PHP plugin, but a theme should render
      ;; them like ordinary text, not like constants
      (enum-case-reference? element) (annotate-as-default-text holder)

      (enum-case-declaration? element) (annotate-as-default-text holder)

      (.contains PhpTokenTypes/tsNUMBERS element-type) (annotate-as holder DefaultLanguageHighlighterColors/NUMBER)

      ;; true/false/null share the number colour
      (and (<= (.getTextLength element) 5)
           (constant-value? element)
           (not (in-comment? element)))
      (annotate-as-enforced holder DefaultLanguageHighlighterColors/NUMBER)

      ;; magic constants share the colour of regular constants
      (and (constant-reference? element-type element) (magic-constant? (.getText element)))
      (annotate-as-enforced holder PhpHighlightingData/CONSTANT)

      ;; predefined constants (PHP_EOL, E_ALL, ...)
      (constant-reference? element-type element)
      (annotate-as-enforced holder PhpHighlightingData/CONSTANT)

      (php-keyword? element-type) (annotate-as-default-text holder)

      (function-declaration? element) (annotate-as holder DefaultLanguageHighlighterColors/FUNCTION_DECLARATION))))
