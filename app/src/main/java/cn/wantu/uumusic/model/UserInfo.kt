package cn.wantu.uumusic.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
data class QQInfo(
    val userInfo: UserInfo,
    val likesong: DiskInfo,
    val mydiss: List<DiskInfo>,
    val likediss: List<DiskInfo>
)
@Serializable
data class UserInfo(
    @SerialName("bigpic")
    val bigpic: String,
    @SerialName("Constellation")
    val constellation: String,
    @SerialName("encrypt_uin")
    val encryptUin: String,
    @SerialName("Gender")
    val gender: String,
    @SerialName("ip")
    val ip: String,
    @SerialName("name")
    val name: String,
    @SerialName("pic")
    val pic: String
)