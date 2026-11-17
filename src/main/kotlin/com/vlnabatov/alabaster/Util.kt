package com.vlnabatov.alabaster

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.BRACES
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.IDENTIFIER
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.STRING
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import java.awt.Color
import java.awt.Font

val transparentColor: Color = Color(0, 0, 0, 0)

const val theme = "Alabaster"
const val BGTheme = "$theme BG"
const val darkTheme = "$theme Dark"

private fun currentScheme() = EditorColorsManager.getInstance().globalScheme

fun isDefaultTheme() = currentScheme().name.endsWith(theme)
fun isBGTheme() = currentScheme().name.endsWith(BGTheme)
fun isDarkTheme() = currentScheme().name.endsWith(darkTheme)

fun isAlabasterTheme() = isDefaultTheme() || isBGTheme() || isDarkTheme()

/**
 * The selected editor scheme resolves fallback keys, so this also yields the
 * inherited text colour. Asking the scheme for its default foreground alone can
 * yield null, which made the enforced attributes a no-op and left quotation
 * marks in the string colour.
 */
private fun defaultTextForeground() =
    currentScheme().getAttributes(HighlighterColors.TEXT)?.foregroundColor
        ?: currentScheme().getAttributes(IDENTIFIER)?.foregroundColor
        ?: currentScheme().defaultForeground

@JvmOverloads
fun annotateSeparationMarks(element: PsiElement, holder: AnnotationHolder, textAttributesKey: TextAttributesKey = BRACES, numberOfQuotationMarks: Int = 1) {
    annotateSeparationMarks(element, holder, textAttributesKey, numberOfQuotationMarks, numberOfQuotationMarks)
}

fun annotateSeparationMarks(element: PsiElement, holder: AnnotationHolder, textAttributesKey: TextAttributesKey = BRACES, numberOfOpeningQuotationMarks: Int = 1, numberOfClosingQuotationMarks: Int = 1) {
    // Standard annotations for non-Alabaster editor themes
    holder
        .newSilentAnnotation(HighlightSeverity.INFORMATION)
        .range(TextRange(element.textRange.startOffset, element.textRange.startOffset + numberOfOpeningQuotationMarks))
        .textAttributes(textAttributesKey)
        .create()

    holder
        .newSilentAnnotation(HighlightSeverity.INFORMATION)
        .range(TextRange(element.textRange.endOffset - numberOfClosingQuotationMarks, element.textRange.endOffset))
        .textAttributes(textAttributesKey)
        .create()

    // Keep the string body in the string colour: some languages (e.g. PHP call arguments)
    // otherwise render it with the default text colour.
    val contentStart = element.textRange.startOffset + numberOfOpeningQuotationMarks
    val contentEnd = element.textRange.endOffset - numberOfClosingQuotationMarks
    if (contentEnd > contentStart) {
        holder
            .newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(TextRange(contentStart, contentEnd))
            .textAttributes(STRING)
            .create()
    }

    if (isAlabasterTheme()) {
        val foreground = defaultTextForeground()
        val background = transparentColor
        // Differently colored quotation marks for Alabaster themes
        holder
            .newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(TextRange(element.textRange.startOffset, element.textRange.startOffset + numberOfOpeningQuotationMarks))
            .enforcedTextAttributes(TextAttributes(foreground, background, null, null, Font.PLAIN))
            .create()

        holder
            .newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(TextRange(element.textRange.endOffset - numberOfClosingQuotationMarks, element.textRange.endOffset))
            .enforcedTextAttributes(TextAttributes(foreground, background, null, null, Font.PLAIN))
            .create()

        // Ensure string's background doesn't change when it's highlighted by the IDE or/and its plugins.
        if (isBGTheme()) {
            holder
                .newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
                .range(TextRange(element.textRange.startOffset + numberOfOpeningQuotationMarks, element.textRange.endOffset - numberOfClosingQuotationMarks))
                .enforcedTextAttributes(TextAttributes(foreground, currentScheme().getAttributes(STRING)?.backgroundColor, null, null, Font.PLAIN))
                .create()
        }
    }
}