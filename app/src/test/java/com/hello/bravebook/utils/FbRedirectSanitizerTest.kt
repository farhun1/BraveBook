package com.hello.bravebook.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class FbRedirectSanitizerTest {

    @Test
    fun `strips fbclid from a plain facebook url`() {
        assertEquals(
            "https://www.facebook.com/watch/?v=1",
            fbRedirectSanitizer("https://www.facebook.com/watch/?v=1&fbclid=abc123")
        )
    }

    @Test
    fun `strips each tracked param individually`() {
        assertEquals(
            "https://www.facebook.com/p/?id=5",
            fbRedirectSanitizer("https://www.facebook.com/p/?id=5&__tn__=-R")
        )
        assertEquals(
            "https://www.facebook.com/p/?id=5",
            fbRedirectSanitizer("https://www.facebook.com/p/?id=5&ref=story")
        )
        assertEquals(
            "https://www.facebook.com/p/?id=5",
            fbRedirectSanitizer("https://www.facebook.com/p/?id=5&source=web")
        )
    }

    @Test
    fun `strips multiple tracked params in combination`() {
        assertEquals(
            "https://www.facebook.com/watch/?v=1",
            fbRedirectSanitizer("https://www.facebook.com/watch/?v=1&__tn__=-R&ref=story&source=web&fbclid=xyz")
        )
    }

    @Test
    fun `strips both literal and percent-encoded forms of __cft__0`() {
        assertEquals(
            "https://www.facebook.com/p/?id=5",
            fbRedirectSanitizer("https://www.facebook.com/p/?id=5&__cft__[0]=abc")
        )
        assertEquals(
            "https://www.facebook.com/p/?id=5",
            fbRedirectSanitizer("https://www.facebook.com/p/?id=5&__cft__%5B0%5D=abc")
        )
    }

    @Test
    fun `unwraps l facebook redirect and cleans the destination`() {
        assertEquals(
            "https://example.com/page?real=1",
            fbRedirectSanitizer("https://l.facebook.com/l.php?u=https%3A%2F%2Fexample.com%2Fpage%3Freal%3D1&h=ATn")
        )
    }

    @Test
    fun `keeps legitimate params including percent-encoded values`() {
        assertEquals(
            "https://www.facebook.com/watch/?v=1&path=a%2Fb",
            fbRedirectSanitizer("https://www.facebook.com/watch/?v=1&path=a%2Fb")
        )
        assertEquals(
            "https://www.facebook.com/story.php?story_fbid=1&id=2&multi_permalinks=3",
            fbRedirectSanitizer("https://www.facebook.com/story.php?story_fbid=1&id=2&multi_permalinks=3&fbclid=z")
        )
    }

    @Test
    fun `tolerates valueless flag params without dropping other cleaning`() {
        assertEquals(
            "https://www.facebook.com/watch/?v=1&debug",
            fbRedirectSanitizer("https://www.facebook.com/watch/?v=1&debug&fbclid=abc")
        )
    }

    @Test
    fun `returns original link when l php destination is not http(s)`() {
        assertEquals(
            "https://l.facebook.com/l.php?u=tel%3A12345&h=ATn",
            fbRedirectSanitizer("https://l.facebook.com/l.php?u=tel%3A12345&h=ATn")
        )
    }
}
