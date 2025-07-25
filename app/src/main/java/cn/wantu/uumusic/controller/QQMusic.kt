package cn.wantu.uumusic.controller

import androidx.core.net.toUri
import cn.wantu.uumusic.UUApp
import cn.wantu.uumusic.UUApp.Companion.json
import cn.wantu.uumusic.controller.SettingConfig.downloadDir
import cn.wantu.uumusic.controller.SettingConfig.isCache
import cn.wantu.uumusic.model.DiskDetail
import cn.wantu.uumusic.model.DiskInfo
import cn.wantu.uumusic.model.MediaInfo
import cn.wantu.uumusic.model.QQInfo
import cn.wantu.uumusic.model.SongInfo
import cn.wantu.uumusic.model.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.IOException

private const val baseUrl = "https://api.vkeys.cn/v2/music/tencent"
private const val Result_OK = 200
private const val Params_Error = 302
private const val Token_Invalid = 303
private const val Programme_Error = 500
private const val Closed_Server = 501
private const val Deprecate_Server = 502
private const val System_Error = 503
private const val Vps_Error = 504
private const val Tips_Message = 600
private const val Tips_Error = 601


suspend fun baseRequest(route: String): JSONObject = withContext(Dispatchers.IO) {
    val request = Request.Builder()
        .url("$baseUrl$route")
        .get()
        .build()
    UUApp.client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")
        val responseData = response.body?.string()
        // 解析 JSON 数据
        val result = JSONObject(responseData!!)
        if (result.getInt("code") == Result_OK) {
            result
        } else {
            throw IOException(responseData)
        }
    }
}

suspend fun getQQInfo(qq: String): QQInfo = withContext(Dispatchers.IO) {
    val data = baseRequest("/info?uin=$qq").getJSONObject("data")
    QQInfo(
        json.decodeFromString<UserInfo>(data.getString("info")),
        json.decodeFromString<DiskInfo>(data.getString("likesong")),
        json.decodeFromString<List<DiskInfo>>(data.optString("mydiss", "[]")),
        json.decodeFromString<List<DiskInfo>>(data.optString("likediss", "[]"))
    )
}

suspend fun searchMusicByName(name: String, page: Int = 1): List<SongInfo> = withContext(Dispatchers.IO) {
    val result = baseRequest("/search/song?word=$name&page=$page")
    json.decodeFromString<List<SongInfo>>(result.getString("data"))
}

suspend fun getDiskDetail(id: Long, page: Int = 1): DiskDetail = withContext(Dispatchers.IO) {
    val data = baseRequest("/dissinfo?id=$id&page=$page").getJSONObject("data")
    val info = data.getJSONObject("info")
    DiskDetail(
        picurl = info.getString("picurl"),
        title = info.getString("title"),
        songnum = info.getInt("songnum"),
        songs = json.decodeFromString<List<SongInfo>>(data.getString("list"))
    )
}

fun generateMediaInfo(f: File): MediaInfo = json.decodeFromString(f.readText())
suspend fun getMusicMediaInfo(id: Long, q: Int): MediaInfo = withContext(Dispatchers.IO) {
    val data = baseRequest("/geturl?id=$id&quality=$q").getJSONObject("data")
    var url = data.getString("url")
    if (!checkDownloadUrl(url)) {
        url = baseRequest("/geturl?id=$id").getJSONObject("data").getString("url")
    }
    if (isCache) {
        val song = data.getString("song")
        val singer = data.getString("singer")
        val destination = File(downloadDir, "${song}-$singer".replace("/", "&"))
        val lrcFile = File(downloadDir, "${song}-$singer.lrc".replace("/", "&"))
        if (downloadFile(url, destination)) {
            val mediaInfo = MediaInfo(
                song = song,
                singer = singer,
                album = data.getString("album"),
                url = destination.toUri().toString(),
                lrc = lrcFile.toUri().toString(),
                cover = data.getString("cover")
            )
            File(downloadDir, "info/${SettingConfig.quality}/$id.json").writeText(
                json.encodeToString(
                    mediaInfo
                )
            )
            return@withContext mediaInfo
        }
    }
    MediaInfo(
        song = data.getString("song"), singer = data.getString("singer"),
        album = data.getString("album"), url = url, lrc = "", cover = data.getString("cover")
    )
}

suspend fun downloadFile(url: String, destination: File, overwrite: Boolean = false): Boolean =
    withContext(Dispatchers.IO) {
        if (!overwrite && destination.exists()) return@withContext true
        val request = Request.Builder().url(url).build()
        UUApp.client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext false
            response.body?.byteStream()?.use { input ->
                destination.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: false
            true
        }
    }

suspend fun getMusicDownloadUrl(id: Long, q: Int = 8): String = withContext(Dispatchers.IO) {
    val data = baseRequest("/geturl?id=$id&quality=$q").getJSONObject("data")
    var url = data.getString("url")
    if (!checkDownloadUrl(url)) {
        url = baseRequest("/geturl?id=$id").getJSONObject("data").getString("url")
    }
    url
}

suspend fun checkDownloadUrl(url: String): Boolean = withContext(Dispatchers.IO) {
    val request = Request.Builder().url(url).head().build()
    UUApp.client.newCall(request).execute().use { response ->
        if (response.isSuccessful) {
            !response.headers("Content-Type").contains("application/json")
        } else {
            println("Request failed: ${response.code}")
            false
        }
    }
}

suspend fun getMusicLrc(id: Long) : String = withContext(Dispatchers.IO){
    val data = baseRequest("/lyric?id=$id").getJSONObject("data")
    data.getString("lrc")
}