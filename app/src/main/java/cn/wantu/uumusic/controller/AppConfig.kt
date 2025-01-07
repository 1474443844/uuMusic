package cn.wantu.uumusic.controller

import cn.wantu.uumusic.UUApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import kotlin.math.ceil


suspend fun getRecommendSong(callBack: (String, String, Long) -> Unit) = withContext(Dispatchers.IO) {
//    val request = Request.Builder()
//        .url("http://app.wty5.cn/uuMusic.json")
//        .get()
//        .build()
//    UUApp.client.newCall(request).execute().use { response ->
//        if (!response.isSuccessful) throw IOException("Unexpected code $response")
//        val responseData = response.body?.string()
//        // 解析 JSON 数据
//        val result = JSONObject(responseData!!)
//        val recommendSongId = result.getLong("songId")
//        val mediaInfo = if(SettingConfig.isCache){
//            val f = File(SettingConfig.downloadDir, "info/$recommendSongId.json")
//            if(f.exists()){
//                generateMediaInfo(f)
//            }else{
//                getMusicMediaInfo(recommendSongId, SettingConfig.quality)
//            }
//        }else{
//            getMusicMediaInfo(recommendSongId, SettingConfig.quality)
//        }
//        callBack(mediaInfo.song, mediaInfo.cover, recommendSongId)
//    }
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

object AppConfig {
    val domain = "uumusic.wty5.com"
    val protocol = "https"
    val url = "$protocol://$domain/api"
    var isLogin = false
}