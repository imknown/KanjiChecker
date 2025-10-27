package net.imknown.kanji

import java.text.BreakIterator
import kotlin.streams.toList

fun String.toGraphemes(): List<String> {
    val list = mutableListOf<String>()

    val iterator = BreakIterator.getCharacterInstance().apply {
        setText(this@toGraphemes)
    }

    var start = iterator.first()
    var end = iterator.next()
    while (end != BreakIterator.DONE) {
        list += substring(start, end)
        start = end
        end = iterator.next()
    }

    return list
}

// https://www.compart.com/en/unicode/search?q=CJK#blocks
// https://www.compart.com/en/unicode/search?q=Hiragana#blocks
// https://www.compart.com/en/unicode/search?q=kana#blocks
fun isInCjkBlocks(codeInUtf32: Int) =
    codeInUtf32 in 0x2E80..0x2EFF // CJK Radicals Supplement
//            || code in 0x3000..0x303F // CJK Symbols and Punctuation
//            || code in 0x31C0..0x31EF // CJK Strokes
            || codeInUtf32 in 0x3200..0x32FF // Enclosed CJK Letters and Months
            || codeInUtf32 in 0x3300..0x33FF // CJK Compatibility
            || codeInUtf32 in 0x3400..0x4DBF // CJK Unified Ideographs Extension A
            || codeInUtf32 in 0x4E00..0x9FFF // CJK Unified Ideographs
            || codeInUtf32 in 0xF900..0xFAFF // CJK Compatibility Ideographs
//            || code in 0xFE30..0xFE4F // CJK Compatibility Forms
            || codeInUtf32 in 0x20000..0x2A6DF // CJK Unified Ideographs Extension B
            || codeInUtf32 in 0x2A700..0x2B73F // CJK Unified Ideographs Extension C
            || codeInUtf32 in 0x2B740..0x2B81F // CJK Unified Ideographs Extension D
            || codeInUtf32 in 0x2B820..0x2CEAF // CJK Unified Ideographs Extension E
            || codeInUtf32 in 0x2CEB0..0x2EBEF // CJK Unified Ideographs Extension F
            || codeInUtf32 in 0x2F800..0x2FA1F // CJK Compatibility Ideographs Supplement
            || codeInUtf32 in 0x30000..0x3134F // CJK Unified Ideographs Extension G

/**
 * https://www.compart.com/en/unicode/U+3000A
 * 𰀊 (\uD880\uDC0A) → [D880, DC0A]
 */
fun String.toUtf16HexStringList(): List<String> {
    val list = mutableListOf<String>()
    for (char in this) {
        list += String.format("%04X", char.code)
    }
    return list
}

/**
 * https://www.compart.com/en/unicode/U+3000A
 * 𰀊 (\uD880\uDC0A) → 196618
 */
@Throws
fun String.toUtf32Int(): Int {
    if (isEmpty()){
        throw Exception("`$this`: is empty")
    }

    val list = codePoints().toList()
    if(list.size > 1) {
        throw Exception("`$this`: code points size > 1")
    }

    val result = list[0]
    println(result.toString(16))
    return result
}

/**
 * https://www.compart.com/en/unicode/U+3000A
 * 196618 → 3000A
 */
fun Int.toHexString() = toString(16)