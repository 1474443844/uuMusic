package cn.wantu.uumusic.music.qq.sign

import java.security.MessageDigest
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


private fun md5(text: String): ByteArray {
    return MessageDigest.getInstance("MD5").digest(text.toByteArray(Charsets.UTF_8))
}

@OptIn(ExperimentalEncodingApi::class)
fun getSecuritySign(payload: String): String {
    val digest = md5(payload)
    val hash = digest.joinToString("") { "%02x".format(it) }.lowercase()
    val idx1 = listOf(21, 4, 9, 26, 16, 20, 27, 30)
    val idx2 = listOf(18, 11, 3, 2, 1, 7, 6, 25)
    val scramble = listOf(212, 45, 80, 68, 195, 163, 163, 203, 157, 220, 254, 91, 204, 79, 104, 6)

    val part1 = idx1.map { hash[it] }.joinToString("")
    val part2 = idx2.map { hash[it] }.joinToString("")

    val xorBytes = scramble.mapIndexed { i, value ->
        (digest[i].toInt() and 0xFF xor value).toByte()
    }.toByteArray()

//    val base64Str = Base64.encodeToString(xorBytes, Base64.DEFAULT)
    val base64Str = Base64.encode(xorBytes, 0, xorBytes.size)
    val part3 = base64Str.replace("[\\\\/+=]".toRegex(), "").lowercase()

    return "zzb$part1$part3$part2"
}