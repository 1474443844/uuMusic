package cn.wantu.uumusic.music.qq

import cn.wantu.uumusic.music.qq.sign.getSecuritySign
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.concurrent.thread

@Serializable
data class Comm(
    @SerialName("g_tk")
    val gtk: Int,
    @SerialName("uin")
    val uin: Int,
    @SerialName("format")
    val format: String,
    @SerialName("platform")
    val platform: String
)

@Serializable
data class Param(
    @SerialName("disstid")
    val disstid: Long,
    @SerialName("enc_host_uin")
    val encHostUin: String,
    @SerialName("tag")
    val tag: Int,
    @SerialName("userinfo")
    val userinfo: Int,
    @SerialName("song_begin")
    val songBegin: Int,
    @SerialName("song_num")
    val songNum: Int
)

@Serializable
data class Req0(
    @SerialName("module")
    val module: String,
    @SerialName("method")
    val method: String,
    @SerialName("param")
    val param: Param
)

@Serializable
data class Req(
    @SerialName("req_0")
    val req0: Req0,
    @SerialName("comm")
    val comm: Comm
)

fun newReq(disstid: Long, start: Int, num: Int): Req {
    return Req(
        req0 = Req0(
            module = "music.srfDissInfo.aiDissInfo",
            method = "uniform_get_Dissinfo",
            param = Param(
                disstid = disstid,
                encHostUin = "",
                tag = 1,
                userinfo = 1,
                songBegin = start,
                songNum = num
            )
        ), comm = Comm(gtk = 5381, uin = 0, format = "json", platform = "-1")
    )
}

fun main() {
    thread {

        val client = OkHttpClient()
        val req = newReq(disstid = 7256912512L, start = 0, num = 10)
        val json = Json { ignoreUnknownKeys }
        val data = json.encodeToString(req)
        val request = okhttp3.Request.Builder()
//        .url("https://c.y.qq.com/base/fcgi-bin/fcg_music_express_mobile3.fcg")
            .url("https://u6.y.qq.com/cgi-bin/musics.fcg?sign=${getSecuritySign(data)}&_=${System.currentTimeMillis()}")
            .header("content-type", "application/x-www-form-urlencoded")
            .header("user-agent", "Mozilla/5.0")
            .post(data.toRequestBody())
            .build()
        client.newCall(request).execute().use {
            if (it.isSuccessful) {
                println(it.body?.string())
            } else {
                println("Request failed: ${it.code}")
            }
        }
    }

}