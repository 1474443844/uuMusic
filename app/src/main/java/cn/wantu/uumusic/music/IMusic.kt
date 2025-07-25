package cn.wantu.uumusic.music

abstract class IMusic {
    protected abstract fun getRefer(): String

    protected abstract fun getDissInfo(): DissInfo

}

data class DissInfo(
    val id: Long,
    val title: String,
    val picurl: String,
    val SongList: List<SongInfo>,
    val num: Int
)

data class SongInfo(val id: Long, val title: String, val time_public: String, val picurl: String)
