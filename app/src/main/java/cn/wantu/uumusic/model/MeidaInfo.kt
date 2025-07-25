package cn.wantu.uumusic.model

import kotlinx.serialization.Serializable

@Serializable
data class MediaInfo(
    val song: String,
    val singer: String,
    val album: String,
    val url: String,
    val lrc: String,
    val cover: String
)