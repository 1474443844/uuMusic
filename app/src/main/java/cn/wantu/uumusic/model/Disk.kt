package cn.wantu.uumusic.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiskInfo(
    @SerialName("id")
    val id: Long,
    @SerialName("title")
    val title: String,
    @SerialName("picurl")
    val picurl: String
)
data class DiskDetail(
    val title: String,
    val picurl: String,
    val songnum: Int,
    val songs: List<SongInfo>
)