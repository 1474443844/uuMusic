package cn.wantu.uumusic.model

import cn.wantu.uumusic.UUApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.IOException
import kotlin.math.ceil

suspend fun getRecommendSong(callBack: (String, String, Long) -> Unit) = withContext(Dispatchers.IO) {
    val request = Request.Builder()
        .url("http://app.wty5.cn/uuMusic.json")
        .get()
        .build()
    UUApp.client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")
        val responseData = response.body?.string()
        // 解析 JSON 数据
        val result = JSONObject(responseData!!)
        val recommendSongId = result.getLong("songId")
        val mediaInfo = if(MusicPlayerController.isCache){
            val f = File(MusicPlayerController.downloadDir, "info/$recommendSongId.json")
            if(f.exists()){
                generateMediaInfo(f)
            }else{
                getMusicMediaInfo(recommendSongId)
            }
        }else{
            getMusicMediaInfo(recommendSongId)
        }
        callBack(mediaInfo.song, mediaInfo.cover, recommendSongId)
    }
}
suspend fun getRecommendSongs(callBack: (String, String) -> Unit) = withContext(Dispatchers.IO) {
    val request = Request.Builder()
        .url("http://app.wty5.cn/uuMusic.json")
        .get()
        .build()
    UUApp.client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")
        val responseData = response.body?.string()
        // 解析 JSON 数据
        val result = JSONObject(responseData!!)
        val recommendSongsId = result.getLong("songsId")
        val recommendSongIndex = result.getInt("index")
        val diskDetail =
            getDiskDetail(recommendSongsId, ceil(recommendSongIndex / 10f).toInt())
        val songInfo = diskDetail.songs[recommendSongIndex - 1]
        callBack(songInfo.song, songInfo.cover)
    }
}