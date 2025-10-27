package net.imknown.kanji

import java.io.File
import java.nio.file.Paths
import kotlin.streams.toList

// region [Ignore]
private const val shouldIgnoreComment = false
private val ignoreComments = listOf("<!--", "//", "/*", "* ", "LogUtil.", "tools:")

private fun ignoreDirectoryPathStrings(directoryPathString: String) = File(directoryPathString, ".gitignore")
    .takeIf { it.exists() }
    ?.bufferedReaderReadLines()
    ?: emptyList()

private val ignoreGit = listOf(".git")
private val ignoreIdea = listOf(".idea", "*.iml") // https://github.com/github/gitignore/blob/main/Global/JetBrains.gitignore
private val ignoreGradle = listOf(".gradle", "build") // https://github.com/github/gitignore/blob/main/Gradle.gitignore
private val ignoreJava = listOf("*.jks", "*.keystore", "*.jar") // https://github.com/github/gitignore/blob/main/Java.gitignore
private val ignoreKotlin = listOf(".kotlin") // https://github.com/github/gitignore/blob/main/Kotlin.gitignore
private val ignoreAndroid = listOf("*.aar", "*.so") // https://github.com/github/gitignore/blob/main/Android.gitignore
private val ignoreMac = listOf(".DS_Store")
private val ignoreSwift = listOf("xcuserdata", "*.swiftdoc", "*.swiftinterface") // https://github.com/github/gitignore/blob/main/Swift.gitignore
private val ignoreIos = listOf("*.nib", "*.a", "_CodeSignature")
private val ignoreBinary = listOf(
    "*.png", "*.jpg", "*.gif", "*.webp", "*.tiff",
    "*.ttf", "*.otf",
    "*.data", "*.pdf", "*.xlsx"
)

private val ignores = (ignoreGit
        + ignoreIdea + ignoreGradle + ignoreJava + ignoreKotlin + ignoreAndroid
        + ignoreMac + ignoreSwift + ignoreIos
        + ignoreBinary + ignoreCustom
        ).filter(String::isNotEmpty)
// endregion [Ignore]

// region [JpKanji]
private val jpKanji6917String = jpKanji("6917.txt").bufferedReaderReadText()
private val jpKanjiExtString = jpKanji("Ext.txt").bufferedReaderReadText()
private val jpKanjiGraphemes = jpKanji6917String.toGraphemes() + jpKanjiExtString.toGraphemes()
// endregion [JpKanji]

// region [Core]
private fun removeIgnored(directoriesTreeWalk: Sequence<File>): Sequence<File> {
    val ignoresFinal = ignores // + ignoreDirectoryPathStrings(directoryPathString)
    return directoriesTreeWalk.filter { file ->
        ignoresFinal.forEach { ignoreOriginal ->
            var ignore = ignoreOriginal
            if (ignore.first() == '/') {
                ignore = ignore.drop(1)
            }
            if (ignore.last() == '/') {
                ignore = ignore.dropLast(1)
            }

            val filePath = file.path

            val isIgnore = if (ignore.startsWith("*")) {
                // File
                ignore = ignore.drop(1)
                filePath.endsWith(ignore)
            } else {
                // Dir
                filePath.contains("/$ignore/") || file.endsWith(ignore)
            }
            if (isIgnore) {
                return@filter false
            }
        }

        return@filter true
    }
}

private fun check(file: File, urlOnline: String, outputFile: File) {
    val inputsLines = file.bufferedReaderReadLines()
    val inputs = if (shouldIgnoreComment) {
        inputsLines.filterNot { input ->
            ignoreComments.forEach { ignore ->
                val inputTrim = input.trim()
                val isIgnore = inputTrim.startsWith(ignore)
                if (isIgnore) {
                    return@filterNot true
                }
            }

            false
        }
    } else {
        inputsLines
    }

    val results = mutableListOf<String>()
    inputs.forEachIndexed { index, input ->
        val inputGraphemes = input.toGraphemes()
        val subtract = inputGraphemes - jpKanjiGraphemes.toSet()
        val finalNotIncluded = subtract.filter { grapheme ->
            val codeInUtf32 = grapheme.codePoints().toList()[0]
            isInCjkBlocks(codeInUtf32)
        }

        if (finalNotIncluded.isNotEmpty()) {
            fun appendResult() {
                val line = "L${index + 1}"
                val finalNotIncludedString = finalNotIncluded.joinToString("")
                results += lineTemplate(urlOnline, line, input, finalNotIncludedString)
            }
            if (shouldIgnoreComment) {
                val first = finalNotIncluded[0]
                val firstIndex = input.indexOf(first)
                val commentIndices = ignoreComments.map { ignore ->
                    input.indexOf(ignore)
                }.filter { it != -1 }

                if (commentIndices.isEmpty() || firstIndex < commentIndices.min()) {
                    appendResult()
                }
            } else {
                appendResult()
            }
        }
    }

    if (results.isNotEmpty()) {
        outputFile.appendText(resultTemplate(file, results))
    }
}

class Repo (
    val localDirectoryPathString: String,
    val onlineUrlWithBranch: String,
)

private fun lineTemplate(urlOnline: String, line: String, input: String, finalNotIncludedString: String) =
    "$urlOnline#$line\t${input.trim()}"

//private fun lineTemplate(urlOnline: String, line: String, input: String, finalNotIncludedString: String) =
//    """
//    |- $line `$input`: $finalNotIncludedString}
//    |  $urlOnline#$line
//    """.trimMargin()

private fun resultTemplate(file: File, results: List<String>) =
    results.joinToString("\n") + "\n"
//private fun resultTemplate(file: File, results: List<String>) =
//    """
//    |
//    |> `${file.path}`
//    |${results.joinToString("\n")}
//    |
//    """.trimMargin()

private fun main() {
    repos.forEach { ic: Repo ->
        val localDirectoryPathString = ic.localDirectoryPathString
        val onlineUrlWithBranch = ic.onlineUrlWithBranch

        val outputParentDir = File("$currentRootDir/output")
        outputParentDir.mkdir()

        val projectDir = Paths.get(localDirectoryPathString).fileName
        val suffix = if (shouldIgnoreComment) "Ignore" else "Check"
        val outputFileExt = "txt"
        val outputFileName = "$projectDir-${suffix}Comments.$outputFileExt"

        val outputFile = File(outputParentDir, outputFileName)
        outputFile.writeText("")

        val directoriesTreeWalk = File(localDirectoryPathString).walk()
            .filter(File::isFile)
        val files = removeIgnored(directoriesTreeWalk)

        val logDir = File(outputParentDir, "log")
        logDir.mkdir()
        val outputFileLog = File(logDir, "$projectDir-Log.$outputFileExt")
        outputFileLog.writeText("--- Ignored ---\n")
        val filesIgnored = directoriesTreeWalk - files.toSet()
        filesIgnored.forEach { file ->
            outputFileLog.appendText(file.path + "\n")
        }
        outputFileLog.appendText("\n--- Checked ---\n")

        val totalSize = files.toList().size
        files.forEachIndexed { index, file ->
            val fileLog = "${index + 1}/$totalSize: ${file.path}"
            println(fileLog)
            outputFileLog.appendText(fileLog + "\n")

            val urlOnline = file.path.replace(localDirectoryPathString, onlineUrlWithBranch)
            check(file, urlOnline, outputFile)
        }
    }
}
// endregion [Core]