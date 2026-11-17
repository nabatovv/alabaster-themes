package com.vlnabatov.alabaster.extensions.annotation

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity.INFORMATION
import com.intellij.lang.annotation.HighlightSeverity.TEXT_ATTRIBUTES
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.BRACES
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.xml.XmlTokenType
import java.awt.Font

// Tag names and the angle brackets of element-based languages keep the default text colour.
private val tagPartTypes = setOf(
    XmlTokenType.XML_START_TAG_START,
    XmlTokenType.XML_END_TAG_START,
    XmlTokenType.XML_TAG_NAME,
    XmlTokenType.XML_TAG_END,
    XmlTokenType.XML_EMPTY_ELEMENT_END
)

class XMLAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        try {
            when (element.elementType) {
                // delimiters
                XmlTokenType.XML_EQ,
                XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER,
                XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER ->
                    holder.newSilentAnnotation(TEXT_ATTRIBUTES).textAttributes(BRACES).create()

                in tagPartTypes -> {
                    val foreground = EditorColorsManager.getInstance().schemeForCurrentUITheme.defaultForeground
                    holder
                        .newSilentAnnotation(INFORMATION)
                        .enforcedTextAttributes(TextAttributes(foreground, null, null, null, Font.PLAIN))
                        .create()
                }
            }
        } catch (e: Exception) {
            /* Should not happen */
        }
    }
}
