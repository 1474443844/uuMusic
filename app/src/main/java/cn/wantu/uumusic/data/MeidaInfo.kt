package cn.wantu.uumusic.data

import kotlinx.serialization.Serializable

@Serializable
data class MediaInfo(
    val song: String,
    val singer: String,
    val album: String,
    val url: String,
    val cover: String
)
