package net.imknown.kanji

class ConfigTemplate {
    private val projectBaseDir = "/Users/imknown/Projects"
    private val project1 = "$projectBaseDir/Project1"
    val repos = listOf(
        Repo(project1, "https://example.com/repo/repo_android/-/blob/master"),
    )

    val ignoreCustom = listOf("3rdLibDir", "*.bin")
}