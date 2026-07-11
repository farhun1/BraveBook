package com.hello.bravebook.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class FetchScriptsTest {

    @Test
    fun `enabled scripts are concatenated in order and disabled are skipped`() {
        val scripts = listOf(
            Script(true, 1, "a.js"),
            Script(false, 2, "b.js"),
            Script(true, 3, "c.js")
        )
        val result = fetchScripts(scripts) { resId -> "content-$resId;" }
        assertEquals("content-1;content-3;", result)
    }

    @Test
    fun `a missing resource yields empty string without aborting the rest`() {
        val scripts = listOf(
            Script(true, 1, "a.js"),
            Script(true, 2, "b.js")
        )
        val result = fetchScripts(scripts) { resId ->
            if (resId == 1) throw RuntimeException("missing resource") else "ok-$resId;"
        }
        assertEquals("ok-2;", result)
    }
}
