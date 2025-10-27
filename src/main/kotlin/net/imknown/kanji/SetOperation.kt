package net.imknown.kanji

import java.io.File

private val kanji5859String = jpKanji("5859.txt").bufferedReaderReadText()
private val kanji6257String = jpKanji("6257.txt").bufferedReaderReadText()

private val kanji5859: List<String> = kanji5859String.toGraphemes()
private val kanji6257: List<String> = kanji6257String.toGraphemes()

private fun printSize() {
    val kanji5859Size = kanji5859.size
    val kanji6257Size = kanji6257.size
    println("Kanji 5859 size: $kanji5859Size")
    println("Kanji 6257 size: $kanji6257Size")
}

private fun printDuplicated() {
    fun Collection<*>.findDuplicated(): Map<Any?, Int> =
        groupingBy { it }.eachCount().filter { (_, count) -> count > 1 }
    val duplicatedKanji5859 = kanji5859.findDuplicated()
    val duplicatedKanji6257 = kanji6257.findDuplicated()
    println("Duplicated Kanji 5859: $duplicatedKanji5859")
    println("Duplicated Kanji 6257: $duplicatedKanji6257")
}

private fun printDifferenceAndCreateUnion() {
    val intersection: Set<String> = kanji5859 intersect kanji6257.toSet()
    // println("Intersection set: ${intersection.joinToString("")}")

    val differenceKanji5859: List<String> = kanji5859 - intersection
    val differenceKanji6257: List<String> = kanji6257 - intersection
    println("Difference set: Kanji 5859: size: ${differenceKanji5859.size}, ${differenceKanji5859.joinToString("")}")
    println("Difference set: Kanji 6257: size: ${differenceKanji6257.size}, ${differenceKanji6257.joinToString("")}")

    val unionSet = (kanji5859 + differenceKanji6257).joinToString("")
    val outputFile = File("$currentRootDir/${jpKanji("6917.txt")}") // 5859 + 1058 = 6917
    outputFile.writeText(unionSet)
}

private fun main() {
    printSize()
    printDuplicated()
    printDifferenceAndCreateUnion()
}