(ns com.vlnabatov.alabaster.common-annotator
  "Highlighting shared by languages that have no dedicated Alabaster annotator.

  The annotator is written against platform APIs only, so no language plugin is
  needed at build time: token types are recognised by their names and function
  declarations via the PsiNameIdentifierOwner interface. It is registered without
  a `language` attribute, so it applies to every language - including languages
  added by plugins installed later - and skips the languages that have a
  dedicated Alabaster annotator.

  Rules (the same ones the dedicated annotators implement):
    - string literals get the shared string treatment (quotes separated),
    - numeric literals and constant values share one colour,
    - only names of function/method declarations are coloured; calls keep the
      default text colour."
  (:require [clojure.string :as string])
  (:import (com.intellij.lang ASTNode Language)
           (com.intellij.lang.annotation AnnotationHolder)
           (com.intellij.lang.annotation HighlightSeverity)
           (com.intellij.openapi.editor DefaultLanguageHighlighterColors)
           (com.intellij.psi PsiElement PsiNameIdentifierOwner)
           (com.intellij.psi.tree IElementType)
           (com.vlnabatov.alabaster UtilKt))
  (:gen-class :implements [com.intellij.lang.annotation.Annotator
                           com.intellij.openapi.project.PossiblyDumbAware]
              :name       com.vlnabatov.alabaster.extensions.annotation.CommonAnnotator
              :prefix     "-"))


(def constant-values #{"true" "false" "null" "nil" "undefined" "nan" "infinity" "none"})

;; Languages that ship their own Alabaster annotator; the shared annotator must
;; not run there to avoid duplicate annotations.
(def dedicated-languages
  #{"JAVA" "kotlin" "Scala" "Clojure" "go" "JavaScript" "PHP" "XML"})

(def numeric-literal
  #"(?i)^[-+]?(?:0x[0-9a-f_]+|0b[01_]+|0o[0-7_]+|(?:\d[\d_]*(?:\.\d[\d_]*)?|\.\d[\d_]*)(?:[eE][-+]?\d+)?)(?:[a-z]{0,10})?$")

;; Token types are named like STRING_LITERAL, INTEGER_LITERAL, FLOAT, NUMBER, ...
(def string-token
  #"(?i)STRING|(?i)^(?:CHAR|CHARACTER)_?(?:LITERAL|LIT)$")

(def number-token
  #"(?i)NUMBER|NUMERIC|(?i)(?:^|_)(?:INT|INTEGER|FLOAT|DOUBLE|DECIMAL|HEX|OCTAL|BINARY|RATIONAL|LONG|BIGINT|BIGDECIMAL|REAL|NUM|U?INT(?:8|16|32|64|128)|F(?:32|64))(?:_|$)|(?i)(?:INT|INTEGER|FLOAT|DOUBLE|DECIMAL|NUMERIC|HEX|OCTAL|BINARY|LONG)(?:LITERAL|LIT|CONST|NUMBER|TOKEN)")

;; PSI classes of declarations are usually named ...Function, ...Method, ...Def, ...
(def declaration-class
  #"(Function|Method|Func|Routine|Procedure|Sub|Fn|Def)(?![a-z])")

;; ...and references/calls/expressions are not declarations.
(def invocation-class
  #"(Call|Reference|Invocation|Expression|Usage|Type)(?![a-z])")

(defonce ^:private token-kinds (atom {}))


(defn- token-kind
  [^IElementType element-type]
  (or (get @token-kinds element-type)
      (let [name (string/upper-case (str element-type))
            kind (cond
                   (re-find string-token name) :string
                   (re-find number-token name) :number
                   :else nil)]
        (swap! token-kinds assoc element-type kind)
        kind)))


(defn- annotate-as
  [^AnnotationHolder holder text-attributes-key]
  (-> holder
      (.newSilentAnnotation HighlightSeverity/INFORMATION)
      (.textAttributes text-attributes-key)
      (.create)))


(defn- quotation-mark-run
  [^String text from-start?]
  (let [length (.length text)]
    (if (zero? length)
      0
      (let [quote-char (.charAt text (if from-start? 0 (dec length)))]
        (if (not (contains? #{\' \" \u0060} quote-char))
          0
          (loop [run 0]
            (if (and (< run length)
                     (= (.charAt text (if from-start? run (- length 1 run))) quote-char))
              (recur (inc run))
              run)))))))


(defn- annotate-string
  [^PsiElement element ^String text ^AnnotationHolder holder]
  (let [length (.length text)
        opening (quotation-mark-run text true)
        closing (quotation-mark-run text false)]
    (when (and (> opening 0) (> closing 0) (< (+ opening closing) length))
      (UtilKt/annotateSeparationMarks element holder DefaultLanguageHighlighterColors/BRACES
                                      (int opening) (int closing)))))


(defn- function-declaration-name?
  [^PsiElement element]
  (let [parent (.getParent element)]
    (and (instance? PsiNameIdentifierOwner parent)
         (identical? element (.getNameIdentifier ^PsiNameIdentifierOwner parent))
         (let [class-name (.getSimpleName ^Class (class parent))]
           (and (re-find declaration-class class-name)
                (not (re-find invocation-class class-name)))))))


(defn- leaf?
  [^ASTNode node]
  (nil? (.getFirstChildNode node)))


(defn- dedicated-language?
  [^PsiElement element]
  (loop [language (.getLanguage element)]
    (cond
      (nil? language) false
      (contains? dedicated-languages (.getID ^Language language)) true
      :else (recur (.getBaseLanguage ^Language language)))))


(defn -annotate
  [_ ^PsiElement element ^AnnotationHolder holder]
  (when (and (instance? PsiElement element)
             (not (dedicated-language? element)))
    (try
      (let [^ASTNode node (.getNode element)]
        (when (and node (leaf? node))
          (let [^IElementType element-type (.getElementType node)
                text (.getText element)]
            (cond
              (function-declaration-name? element)
              (annotate-as holder DefaultLanguageHighlighterColors/FUNCTION_DECLARATION)

              (contains? constant-values (string/lower-case text))
              (annotate-as holder DefaultLanguageHighlighterColors/NUMBER)

              :else
              (case (token-kind element-type)
                :string (annotate-string element text holder)
                :number (when (re-find numeric-literal text)
                          (annotate-as holder DefaultLanguageHighlighterColors/NUMBER))
                nil)))))
      (catch Throwable _ nil))))
