package com.vlnabatov.alabaster.extensions.annotation

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity.INFORMATION
import com.intellij.lang.javascript.JSElementTypes.ASSIGNMENT_EXPRESSION
import com.intellij.lang.javascript.JSElementTypes.CLASS
import com.intellij.lang.javascript.JSElementTypes.CLASS_EXPRESSIONS
import com.intellij.lang.javascript.JSElementTypes.DEFINITION_EXPRESSION
import com.intellij.lang.javascript.JSElementTypes.DESTRUCTURING_ELEMENT
import com.intellij.lang.javascript.JSElementTypes.DESTRUCTURING_PROPERTY
import com.intellij.lang.javascript.JSElementTypes.DESTRUCTURING_SHORTHANDED_PROPERTY
import com.intellij.lang.javascript.JSElementTypes.FIELD
import com.intellij.lang.javascript.JSElementTypes.FIELD_STATEMENT
import com.intellij.lang.javascript.JSElementTypes.FUNCTION_DECLARATIONS
import com.intellij.lang.javascript.JSElementTypes.FUNCTION_EXPRESSIONS
import com.intellij.lang.javascript.JSElementTypes.FUNCTION_PROPERTIES
import com.intellij.lang.javascript.JSElementTypes.NEW_EXPRESSION
import com.intellij.lang.javascript.JSElementTypes.PROPERTY
import com.intellij.lang.javascript.JSElementTypes.REFERENCE_EXPRESSION
import com.intellij.lang.javascript.JSElementTypes.STRING_TEMPLATE_EXPRESSION
import com.intellij.lang.javascript.JSElementTypes.VAR_STATEMENT
import com.intellij.lang.javascript.JSElementTypes.VARIABLE
import com.intellij.lang.javascript.JSTokenTypes.BACKQUOTE
import com.intellij.lang.javascript.JSTokenTypes.FALSE_KEYWORD
import com.intellij.lang.javascript.JSTokenTypes.IDENTIFIER
import com.intellij.lang.javascript.JSTokenTypes.NULL_KEYWORD
import com.intellij.lang.javascript.JSTokenTypes.PRIVATE_IDENTIFIER
import com.intellij.lang.javascript.JSTokenTypes.SINGLE_QUOTE_STRING_LITERAL
import com.intellij.lang.javascript.JSTokenTypes.STRING_LITERAL
import com.intellij.lang.javascript.JSTokenTypes.TRUE_KEYWORD
import com.intellij.lang.javascript.JSTokenTypes.UNDEFINED_KEYWORD
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.FUNCTION_DECLARATION
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.NUMBER
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.elementType
import com.vlnabatov.alabaster.annotateSeparationMarks

val identifierTypes = setOf(IDENTIFIER, PRIVATE_IDENTIFIER)

val symbolTypes = setOf(TRUE_KEYWORD, FALSE_KEYWORD, NULL_KEYWORD, UNDEFINED_KEYWORD)

val stringTypes = setOf(STRING_LITERAL, SINGLE_QUOTE_STRING_LITERAL)

val symbolRegex =
  Regex(
    "\\bInfinity|NaN|POSITIVE_INFINITY|NEGATIVE_INFINITY|MAX_VALUE|MIN_VALUE|EPSILON|MAX_SAFE_INTEGER|MIN_SAFE_INTEGER|E|LN2|LN10|LOG2E|LOG10E|PI|SQRT1_2|SQRT2|\\b"
  )

val mathObjectRegex = Regex("\\bMath|Number\\b")

val assignmentExpressionTypes =
  setOf(
    ASSIGNMENT_EXPRESSION,
    VARIABLE,
    FIELD,
    FIELD_STATEMENT,
    PROPERTY,
    VAR_STATEMENT
  )

val functionExpressionTypes =
  setOf(
    *FUNCTION_DECLARATIONS.types,
    *FUNCTION_EXPRESSIONS.types,
    *FUNCTION_PROPERTIES.types,
    *CLASS_EXPRESSIONS.types,
  )

val classTypes = setOf(CLASS, *CLASS_EXPRESSIONS.types)


val functionConstructorRegex =
  Regex("\\bFunction|AsyncFunction|GeneratorFunction|AsyncGeneratorFunction\\b")

class JSAnnotator : Annotator {
  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    if (element !is LeafPsiElement) return

    try {
      when {
        isSymbolIdentifier(element) ->
          holder.newSilentAnnotation(INFORMATION).textAttributes(NUMBER).create()

        element.elementType in identifierTypes &&
            (isFunctionDefinition(element) || element.parent.elementType in classTypes) ->
          holder.newSilentAnnotation(INFORMATION).textAttributes(FUNCTION_DECLARATION).create()

        element.elementType in stringTypes ||
            (element.elementType === BACKQUOTE &&
                element.parent.elementType === STRING_TEMPLATE_EXPRESSION &&
                (element === element.parent.firstChild || element === element.parent.lastChild)) ->
          annotateSeparationMarks(element, holder)
      }
    } catch (e: Exception) {
      /* Should not happen */
    }
  }

  // Imprecise detection given current PSI info
  fun isSymbolIdentifier(element: PsiElement): Boolean = when {
    element.elementType in symbolTypes -> true

    element.text.matches(symbolRegex) &&
        element.parent.elementType == REFERENCE_EXPRESSION &&
        (element.parent as JSReferenceExpressionImpl).referencedName!!.matches(symbolRegex) -> true

    element.text.matches(symbolRegex) &&
        element.parent.elementType == DESTRUCTURING_PROPERTY &&
        element.parent.parent.parent.elementType == DESTRUCTURING_ELEMENT &&
        element.parent.parent.parent.children[1].text.matches(mathObjectRegex) -> true

    else -> element.text.matches(symbolRegex) &&
        element.parent.parent.elementType == DESTRUCTURING_SHORTHANDED_PROPERTY &&
        element.parent.parent.parent.parent.elementType == DESTRUCTURING_ELEMENT &&
        element.parent.parent.parent.parent.children[1].text.matches(mathObjectRegex)
  }


  private fun isFunctionExpression(element: PsiElement) =
    element.elementType in functionExpressionTypes ||
        (element.elementType == NEW_EXPRESSION &&
            element.children[0].text.matches(functionConstructorRegex))


  fun isFunctionDefinition(element: PsiElement): Boolean = when {
    element.parent.elementType in functionExpressionTypes -> true

    element.parent.elementType in assignmentExpressionTypes &&
        element.parent.firstChild === element &&
        isFunctionExpression(element.parent.lastChild) -> true

    else -> element.parent.elementType == REFERENCE_EXPRESSION &&
        element.parent.parent.elementType == DEFINITION_EXPRESSION &&
        element.parent.parent.parent.elementType == ASSIGNMENT_EXPRESSION &&
        isFunctionExpression(element.parent.parent.parent.children[1])
  }
}
