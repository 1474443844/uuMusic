package cn.wantu.uumusic.controller

import cn.wantu.uumusic.UUApp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
object SettingConfig {

    @SerialName("quality")
    var quality: Int = 8

    @SerialName("is_cache")
    var isCache = true

    @SerialName("download_dir")
    val downloadDir = File(UUApp.instance.externalCacheDir, "music").also {
        val f = File(it, "info/$quality")
        if (!f.exists()) {
            f.mkdirs()
        }
    }

    init {

    }

}
