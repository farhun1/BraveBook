package com.hello.bravebook.utils

import java.net.URL
import java.net.URLDecoder

// Tracking/redirect parameters to strip from Facebook URLs. Sourced from Brave's
// clean-urls.json Facebook entry, plus fbclid (handled upstream by EasyList/EasyPrivacy
// in Brave, but absent from that file). Both the literal and percent-encoded forms of
// __cft__[0] are listed because the bracket may arrive already encoded as %5B0%5D.
private val TRACKED_PARAMS = setOf(
    "fbclid",
    "__cft__[0]", "__cft__%5B0%5D",
    "__tn__",
    "acontext",
    "external_ref",
    "gd_impression_id",
    "idorvanity",
    "ref",
    "referrer",
    "sale_post_id",
    "set",
    "source",
    "store_visit_source",
    "with_pv",
)

fun fbRedirectSanitizer(link: String): String {
    try {
        var url = URL(link)

        if (url.host == "l.facebook.com" && url.path == "/l.php") {
            val params = url.query.split("&").associate {
                val key = it.substringBefore("=")
                val value = if (it.contains("=")) URLDecoder.decode(it.substringAfter("="), "UTF-8") else ""
                key to value
            }
            val dest = params["u"] ?: return link
            // Only follow http(s) destinations. A non-http(s) scheme (tel:,
            // file:, intent:, ...) would otherwise be reconstructed and
            // forwarded to an external viewer intent.
            val destUrl = runCatching { URL(dest) }.getOrNull()
            if (destUrl == null || destUrl.protocol !in setOf("http", "https")) {
                return link
            }
            url = destUrl
        }

        // url.query already contains percent-encoded components, so kept values are
        // passed through verbatim. Re-encoding them (e.g. via URLEncoder) would
        // double-encode the existing '%' and corrupt values like a%2Fb -> a%252Fb.
        val params = url.query?.split("&")
            ?.filter { it.substringBefore("=") !in TRACKED_PARAMS }
            ?.joinToString("&") { param ->
                val key = param.substringBefore("=")
                val value = if (param.contains("=")) param.substringAfter("=") else ""
                if (value.isEmpty()) key else "$key=$value"
            }

        return buildString {
            append("${url.protocol}://${url.host}")
            if (url.port != -1 && url.port != url.defaultPort) append(":${url.port}")
            append(url.path)
            if (!params.isNullOrBlank()) append("?").append(params)
        }
    } catch (_: Exception) {
        return link
    }
}
