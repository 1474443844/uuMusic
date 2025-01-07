package cn.wantu.uumusic.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SongInfo(
    @SerialName("id")
    val id: Long,
    @SerialName("mid")
    val mid: String,
    @SerialName("vid")
    val vid: String,
    @SerialName("song")
    val song: String,
    @SerialName("subtitle")
    val subtitle: String,
    @SerialName("album")
    val album: String,
    @SerialName("type")
    val type: Int,
    @SerialName("quality")
    val quality: String,
    @SerialName("cover")
    val cover: String,
    @SerialName("bpm")
    val bpm: Int,
    @SerialName("singer")
    val singer: String,
    @SerialName("singer_list")
    val singerList: List<Singer>? = null,
    @SerialName("time")
    val time: String = "",
)

@Serializable
data class Singer(
    @SerialName("id")
    val id: Int,
    @SerialName("mid")
    val mid: String,
    @SerialName("name")
    val name: String,
    @SerialName("title")
    val title: String
)