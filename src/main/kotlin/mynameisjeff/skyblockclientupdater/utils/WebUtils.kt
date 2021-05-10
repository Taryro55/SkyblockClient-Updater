package mynameisjeff.skyblockclientupdater.utils

import mynameisjeff.skyblockclientupdater.SkyClientUpdater
import org.apache.http.HttpRequest
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.protocol.HttpContext
import java.io.InputStream
import java.nio.charset.Charset

object WebUtils {
    val builder =
        HttpClients.custom().setUserAgent("SkyblockClient-Updater/" + SkyClientUpdater.VERSION)
            .addInterceptorFirst { request: HttpRequest, _: HttpContext? ->
                if (!request.containsHeader("Pragma")) request.addHeader("Pragma", "no-cache")
                if (!request.containsHeader("Cache-Control")) request.addHeader("Cache-Control", "no-cache")
            }

    fun fetchResponse(urlString: String): String {
        builder.build().use {
            val res = it.execute(HttpGet(urlString))
            return res.entity.content.readTextAndClose()
        }
    }
}

fun InputStream.readTextAndClose(charset: Charset = Charsets.UTF_8): String =
    this.bufferedReader(charset).use { it.readText() }