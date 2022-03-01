package mynameisjeff.skyblockclientupdater.utils

import java.io.InputStream
import java.nio.charset.Charset

fun InputStream.readTextAndClose(charset: Charset = Charsets.UTF_8): String =
    this.bufferedReader(charset).use { it.readText() }