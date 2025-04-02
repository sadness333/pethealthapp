package com.example.prettypetsandfriends.data.entities

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class DateTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(8)
        var output = ""
        for (i in trimmed.indices) {
            output += trimmed[i]
            when (i) {
                1, 3 -> if (i < 7) output += "."
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset <= 1 -> offset
                    offset <= 3 -> offset + 1
                    offset <= 8 -> offset + 2
                    else -> 10
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    offset <= 2 -> offset.coerceAtMost(2)
                    offset <= 5 -> (offset - 1).coerceAtMost(4)
                    else -> (offset - 2).coerceIn(0, 8)
                }
            }
        }

        return TransformedText(AnnotatedString(output), offsetMapping)
    }
}