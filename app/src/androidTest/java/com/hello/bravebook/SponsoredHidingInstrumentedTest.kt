package com.hello.bravebook

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.json.JSONObject
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class SponsoredHidingInstrumentedTest {

    @Test
    fun hidesSponsoredStoriesKeepsNormal() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val latch = CountDownLatch(1)
        var result: String? = null

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val webView = WebView(context)
            webView.settings.javaScriptEnabled = true

            val adblockJs = context.resources.openRawResource(R.raw.adblock)
                .bufferedReader().use { it.readText() }
            val fixture = context.assets.open("sponsored_fixture.html")
                .bufferedReader().use { it.readText() }

            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    view?.evaluateJavascript(adblockJs) {
                        view.postDelayed({
                            val probe = """
                                ({
                                  hidden: document.querySelectorAll('[data-bravebook-sponsored="true"]').length,
                                  normalDisplay: getComputedStyle(document.querySelector('[data-pagelet="FeedUnit2"]')).display
                                })
                            """.trimIndent()
                            view.evaluateJavascript(probe) { json ->
                                result = json
                                latch.countDown()
                            }
                        }, 400)
                    }
                }
            }

            webView.loadDataWithBaseURL(
                "https://localhost/", fixture, "text/html", "utf-8", null
            )
        }

        assertTrue("timed out waiting for WebView", latch.await(20, TimeUnit.SECONDS))
        val json = result ?: throw AssertionError("no result from WebView")
        // evaluateJavascript returns the object JSON-encoded; parse it.
        val obj = JSONObject(json)
        assertEquals(2, obj.getInt("hidden"))
        assertEquals("block", obj.getString("normalDisplay"))
    }
}
