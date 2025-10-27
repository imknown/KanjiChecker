package net.imknown.kanji

import java.io.BufferedReader
import java.io.File

val currentRootDir = System.getProperty("user.dir") + "/src/main/kotlin/net/imknown/kanji"

fun String.bufferedReaderReadText(): String =
    File("$currentRootDir/$this").bufferedReaderReadText()
fun File.bufferedReaderReadText(): String =
    bufferedReaderRead(BufferedReader::readText)

fun String.bufferedReaderReadLines(): List<String> =
    File("$currentRootDir/$this").bufferedReaderReadLines()
fun File.bufferedReaderReadLines(): List<String> =
    bufferedReaderRead(BufferedReader::readLines)

private fun <R> File.bufferedReaderRead(block: (BufferedReader) -> R): R =
    bufferedReader().use(block)

fun jpKanji(fileName: String) = ".hidden/JpKanji/$fileName"