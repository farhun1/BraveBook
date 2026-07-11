package com.hello.bravebook.utils

import androidx.annotation.RawRes

const val SCRIPT_SRC = "https://raw.githubusercontent.com/eepiemi/Materialbook/refs/heads/main/app/src/main/res/raw/"

data class Script(
    val isEnabled: Boolean,
    @param:RawRes val resourceId: Int,
    val scriptTitle: String
)

fun fetchScripts(
    scripts: List<Script>,
    fallbackContent: (Int) -> String
): String {
    return buildString {
        scripts.filter { it.isEnabled }.forEach { script ->
            val content = runCatching { fallbackContent(script.resourceId) }
                .getOrDefault("")
            append(content)
        }
    }
}
