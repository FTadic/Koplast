package com.example.koplast

import android.text.InputFilter
import android.text.Spanned

class InputFilterMinMax (private val min: Int, private val max: Int) : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        try {
            val newVal = (dest.toString().substring(0, dstart) +
                    source.toString().substring(start, end) +
                    dest.toString().substring(dend))

            val input = newVal.toInt()
            if (input in min..max) return null
        } catch (e: NumberFormatException) {
            // Ignoriraj ako nije broj
        }
        return ""
    }
}