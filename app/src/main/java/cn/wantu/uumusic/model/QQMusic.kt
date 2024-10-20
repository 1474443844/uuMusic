package cn.wantu.uumusic.model

import androidx.core.net.toUri
import cn.wantu.uumusic.UUApp
import cn.wantu.uumusic.data.DiskDetail
import cn.wantu.uumusic.data.DiskInfo
import cn.wantu.uumusic.data.MediaInfo
import cn.wantu.uumusic.data.QQInfo
import cn.wantu.uumusic.data.SongInfo
import cn.wantu.uumusic.data.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.IOException

private const val baseUrl = "https://api.vkeys.cn/v2/music/tencent"
private val json = Json { ignoreUnknownKeys = true }
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
    UUApp.getClient().newCall(request).execute().use { response ->
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

suspend fun searchMusicByName(name: String): List<SongInfo> = withContext(Dispatchers.IO) {
    val result = baseRequest("/search/song?word=$name")
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


suspend fun getMusicMediaInfo(id: Long, q: Int = 8): MediaInfo = withContext(Dispatchers.IO) {
    val data = baseRequest("/geturl?id=$id&quality=$q").getJSONObject("data")
    var url = data.getString("url")
    if (!checkDownloadUrl(url)) {
        url = baseRequest("/geturl?id=$id").getJSONObject("data").getString("url")
    }
    if (MusicPlayerController.isCache) {
        val destination = File(MusicPlayerController.downloadDir, id.toString())
        if (downloadFile(url, destination))
            return@withContext MediaInfo(
                song = data.getString("song"),
                singer = data.getString("singer"),
                album = data.getString("album"),
                url = destination.toUri().toString(),
                cover = data.getString("cover")
            )
    }
    MediaInfo(
        song = data.getString("song"), singer = data.getString("singer"),
        album = data.getString("album"), url = url, cover = data.getString("cover")
    )
}

suspend fun downloadFile(url: String, destination: File, overwrite: Boolean = false): Boolean =
    withContext(Dispatchers.IO) {
        if (!overwrite && destination.exists()) return@withContext true
        val request = Request.Builder().url(url).build()
        UUApp.getClient().newCall(request).execute().use { response ->
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
    UUApp.getClient().newCall(request).execute().use { response ->
        if (response.isSuccessful) {
            !response.headers("Content-Type").contains("application/json")
        } else {
            println("Request failed: ${response.code}")
            false
        }
    }
}