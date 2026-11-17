(ns com.vlnabatov.alabaster.kotlin-annotator
  (:import (com.intellij.lang.annotation AnnotationHolder)
           (com.intellij.lang.annotation HighlightSeverity)
           (com.intellij.openapi.editor DefaultLanguageHighlighterColors HighlighterColors)
           (com.intellij.psi PsiElement)
           (com.intellij.psi.tree IElementType)
           (org.jetbrains.kotlin.lexer KtTokens)
           (org.jetbrains.kotlin.psi.stubs.elements KtStubElementTypes)
           (com.intellij.openapi.editor.colors EditorColorsManager)
           (com.intellij.openapi.editor.markup TextAttributes)
           (java.awt Font)
           (com.vlnabatov.alabaster UtilKt))
  (:gen-class :implements [com.intellij.lang.annotation.Annotator
                           com.intellij.openapi.project.PossiblyDumbAware]
              :name       com.vlnabatov.alabaster.extensions.annotation.KotlinAnnotator
              :prefix     "-"))



;; The scheme resolves fallback keys, so this also yields the inherited text
;; colour. Asking the scheme for its default foreground alone can yield nil,
;; which made the enforced attributes a no-op and left string quotes in the
;; string colour.
(defn- default-foreground
  []
  (let [scheme (.getGlobalScheme (EditorColorsManager/getInstance))]
    (or (some-> (.getAttributes scheme HighlighterColors/TEXT) (.getForegroundColor))
        (some-> (.getAttributes scheme DefaultLanguageHighlighterColors/IDENTIFIER) (.getForegroundColor))
        (.getDefaultForeground scheme))))


(defn- annotate-default-text
  [^AnnotationHolder holder]
  (-> holder
      (.newSilentAnnotation HighlightSeverity/INFORMATION)
      (.enforcedTextAttributes (TextAttributes. (default-foreground) nil nil nil Font/PLAIN))
      (.create)))


(defn- quotation-mark-count
  [^PsiElement element]
  (let [child (.getFirstChild element)]
    (if (nil? child) 1 (max 1 (.getTextLength ^PsiElement child)))))


(defn -annotate
  [_ ^PsiElement element ^AnnotationHolder holder]
  (let [^IElementType elementType (.. element getNode getElementType)]
    (cond (or (= KtTokens/OPEN_QUOTE elementType) (= KtTokens/CLOSING_QUOTE elementType))
            (do (annotate-default-text holder))

          (= KtStubElementTypes/STRING_TEMPLATE elementType) (do (UtilKt/annotateSeparationMarks element holder DefaultLanguageHighlighterColors/BRACES (int (quotation-mark-count element))))
          (contains? #{KtStubElementTypes/NULL KtStubElementTypes/BOOLEAN_CONSTANT KtStubElementTypes/FLOAT_CONSTANT
                       KtStubElementTypes/CHARACTER_CONSTANT KtStubElementTypes/INTEGER_CONSTANT}
                     elementType)
            (do (-> holder
                    (.newSilentAnnotation HighlightSeverity/INFORMATION)
                    (.textAttributes DefaultLanguageHighlighterColors/NUMBER)
                    (.create))))))