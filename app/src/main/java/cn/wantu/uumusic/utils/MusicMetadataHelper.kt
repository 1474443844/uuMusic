package cn.wantu.uumusic.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import cn.wantu.uumusic.utils.MusicMetadataHelper.MusicMetadata
import java.lang.Exception

object MusicMetadataHelper {
    /**
     * 获取音频文件的元数据信息（歌名、歌手、专辑、封面等）
     * @param filePath 音乐文件的绝对路径
     * @return 自定义的元数据信息对象
     */
    fun getMusicMetadata(filePath: String): MusicMetadata {
        val mmr = MediaMetadataRetriever()
        val metadata = MusicMetadata()

        try {
            // 设置音频源
            mmr.setDataSource(filePath)

            // 1. 歌曲标题
            metadata.title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            // 2. 歌手
            metadata.artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            // 3. 专辑名
            metadata.album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            // 4. 时长（单位：毫秒）
            val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            metadata.duration = if (durationStr != null) durationStr.toLong() else 0

            // 5. 获取专辑封面
            val pictureData = mmr.embeddedPicture
            if (pictureData != null) {
                val bitmap = BitmapFactory.decodeByteArray(pictureData, 0, pictureData.size)
                metadata.albumCover = bitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mmr.release()
        }

        return metadata
    }

    /**
     * 自定义一个存储元数据信息的类
     */
    class MusicMetadata {
        var title: String? = null
        var artist: String? = null
        var album: String? = null
        var duration: Long = 0 // 毫秒
        var albumCover: Bitmap? = null

        override fun toString(): String {
            return "MusicMetadata{" +
                    "title='" + title + '\'' +
                    ", artist='" + artist + '\'' +
                    ", album='" + album + '\'' +
                    ", duration=" + duration +
                    ", albumCover=" + (albumCover != null) +
                    '}'
        }
    }
}