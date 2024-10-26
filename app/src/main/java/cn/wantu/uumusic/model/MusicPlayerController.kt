package cn.wantu.uumusic.model

import android.os.Bundle
import androidx.annotation.OptIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import cn.wantu.uumusic.UUApp
import cn.wantu.uumusic.data.SongInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(UnstableApi::class)
class MusicPlayerController private constructor() {

    // 播放器
    private val player: ExoPlayer

    // 是否在播放
    var isPlaying by mutableStateOf(false)
        private set

    // 是否准备好了
    var isPrepared by mutableStateOf(true)
        private set

    // 进度条
    var progress by mutableFloatStateOf(0f)

    // 播放列表
    var playList = emptyList<MediaItem>().toMutableStateList()
        private set

    // 当前播放进度
    var currentTime by mutableLongStateOf(0L)

    // 当前播放的MediaItem
    val currentMediaItem: MediaItem?
        get() = player.currentMediaItem

    // 当前播放进度
    val currentPosition: Long
        get() = player.currentPosition.also {
            currentTime = it
        }

    // 播放总时长
    val duration: Long
        get() = player.duration

    //当前媒体项索引
    val currentMediaItemIndex: Int
        get() = player.currentMediaItemIndex

    // 媒体项总数
    val mediaItemCount: Int
        get() = playList.size

    // 初始化媒体播放器
    init {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true) // 允许跨协议重定向（https -> http）
        val dataSourceFactory: DataSource.Factory =
            DefaultDataSource.Factory(UUApp.getContext(), httpDataSourceFactory)
        player = ExoPlayer.Builder(UUApp.getInstance())
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .build()
        setupPlayerListener()
        player.repeatMode = Player.REPEAT_MODE_ALL
        player.prepare()
    }

    private fun setupPlayerListener() {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                println("isPlayingNow: $isPlayingNow")
                isPlaying = isPlayingNow
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                println("Playback state changed: $playbackState")
                when(playbackState){
                    Player.STATE_BUFFERING -> { // 2
                        isPrepared = false
                    }

                    Player.STATE_ENDED -> { // 4
//                            TODO()
                    }

                    Player.STATE_IDLE -> { // 1
//                            TODO()
                    }

                    Player.STATE_READY -> { // 3
                        isPrepared = true
                    }
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                println("Media item transition: $mediaItem, reason: $reason")
                when (reason) {
                    Player.MEDIA_ITEM_TRANSITION_REASON_AUTO -> { // 1
                        player.pause()
                        player.play()
                    }

                    Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED -> { // 3
//                            isPrepared = false
                    }

                    Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT -> { // 0
//                            TODO()
                    }

                    Player.MEDIA_ITEM_TRANSITION_REASON_SEEK -> { // 2
//                            TODO()
                    }
                }

            }
//                override fun onIsLoadingChanged(isLoading: Boolean) {
//                    super.onIsLoadingChanged(isLoading)
//                    isPrepared = !isLoading
//                }
        })
    }

    companion object {
        val downloadDir = File(UUApp.getInstance().cacheDir, "music").also {
            if (!it.exists()) it.mkdirs()
        }
        var isCache = true
        @Volatile
        private var instance: MusicPlayerController? = null

        fun getInstance(): MusicPlayerController =
            instance ?: synchronized(this) {
                instance ?: MusicPlayerController().also { instance = it }
            }
    }

    private suspend fun createMediaItem(id: Long): MediaItem = withContext(Dispatchers.IO) {
        val musicCache = File(downloadDir, "$id.json")
        val mediaInfo = if (musicCache.exists()) {
            generateMediaInfo(musicCache)
        } else {
            getMusicMediaInfo(id)
        }
        val bundle = Bundle().apply {
            putString("cover", mediaInfo.cover)
        }
        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(mediaInfo.song)
            .setArtist(mediaInfo.singer)
            .setAlbumTitle(mediaInfo.album)
            .setExtras(bundle)
            .build()
        MediaItem.Builder()
            .setMediaId(id.toString())
            .setUri(mediaInfo.url)
            .setMediaMetadata(mediaMetadata)
            .build()
    }

    fun play() {
        player.play()
        isPlaying = true
    }

    fun pause(){
        player.pause()
        isPlaying = false
    }

    fun playPrevious() {
        player.seekToPrevious()
    }

    fun playNext() {
        player.seekToNext()
    }
    suspend fun playMusic(id: Long){

    }
    suspend fun playAtNow(song: SongInfo) {
        progress = 0f
        val id = song.id
        if (mediaItemCount == 0) {
            val mediaItem = createMediaItem(id)
            player.addMediaItem(mediaItem)
//            isPrepared = true
            play()
            playList.add(mediaItem)
            return
        }
        pause()
        isPrepared = false
        val mediaItemIndex = findMediaItemIndex(id)
        if (mediaItemIndex == -1) {
            val mediaItem = createMediaItem(id)
            player.addMediaItem(currentMediaItemIndex + 1, mediaItem)
            player.seekToNext()
//            isPrepared = true
            play()
            playList.add(currentMediaItemIndex, mediaItem)
        } else {
            player.seekTo(mediaItemIndex, 0)
            play()
        }
    }
    suspend fun playAtNow(id: Long) {
        progress = 0f
        if (mediaItemCount == 0) {
            val mediaItem = createMediaItem(id)
            player.addMediaItem(mediaItem)
//            isPrepared = true
            play()
            playList.add(mediaItem)
            return
        }
        pause()
        isPrepared = false
        val mediaItemIndex = findMediaItemIndex(id)
        if (mediaItemIndex == -1) {
            val mediaItem = createMediaItem(id)
            player.addMediaItem(currentMediaItemIndex + 1, mediaItem)
            player.seekToNext()
//            isPrepared = true
            play()
            playList.add(currentMediaItemIndex, mediaItem)
        } else {
            player.seekTo(mediaItemIndex, 0)
            play()
        }
    }

    suspend fun playAtNext(song: SongInfo) {
        if (mediaItemCount == 0) {
            playAtNow(song)
            return
        }
        val id = song.id
        pause()
        isPrepared = false
        val mediaItemIndex = findMediaItemIndex(id)

        if (mediaItemIndex == -1) {
            val mediaItem = createMediaItem(id)
            player.addMediaItem(currentMediaItemIndex + 1, mediaItem)
            playList.add(currentMediaItemIndex+1, mediaItem)
        } else {
            println("mediaItemIndex = $mediaItemIndex, currentMediaItemIndex = $currentMediaItemIndex")
            val mediaItem = playList[mediaItemIndex]
            player.moveMediaItem(mediaItemIndex, currentMediaItemIndex + 1)
            playList.remove(mediaItem)
            playList.add(currentMediaItemIndex + 1, mediaItem)
        }
    }
    suspend fun playAtNext(id: Long) {
        if (mediaItemCount == 0) {
            playAtNow(id)
            return
        }
        pause()
        isPrepared = false
        val mediaItemIndex = findMediaItemIndex(id)

        if (mediaItemIndex == -1) {
            val mediaItem = createMediaItem(id)
            player.addMediaItem(currentMediaItemIndex + 1, mediaItem)
            playList.add(currentMediaItemIndex+1, mediaItem)
        } else {
            println("mediaItemIndex = $mediaItemIndex, currentMediaItemIndex = $currentMediaItemIndex")
            val mediaItem = playList[mediaItemIndex]
            player.moveMediaItem(mediaItemIndex, currentMediaItemIndex + 1)
            playList.remove(mediaItem)
            playList.add(currentMediaItemIndex + 1, mediaItem)
        }
    }

    private fun findMediaItemIndex(id: Long): Int {
        for (i in 0 until player.mediaItemCount) {
            val mediaItem = player.getMediaItemAt(i)
            if (mediaItem.mediaId == id.toString()) {
                return i
            }
        }
        return -1
    }

    fun remove(index: Int) {
        playList.removeAt(index)
        player.removeMediaItem(index)
    }
    fun remove(){
        remove(player.currentMediaItemIndex)
    }
//    private fun updatePlayList() {
//        val mediaItemCount = player.mediaItemCount
//        for (i in 0 until mediaItemCount) {
//            val mediaMetadata = player.getMediaItemAt(i).mediaMetadata
//            list.add("${mediaMetadata.title} - ${mediaMetadata.artist}")
//        }
//        playList = list
//    }

    fun playIndex(index: Int) {
        if(index == currentMediaItemIndex){
            return
        }
        if (index in 0 until player.mediaItemCount) {
            player.seekTo(index, 0)
            player.play()
        }
    }

    fun showPlayList() {
        val mediaItemCount = player.mediaItemCount
        println("播放列表中共有 $mediaItemCount 个媒体项")
        for (i in 0 until mediaItemCount) {
            val mediaItem = player.getMediaItemAt(i)
            val title =
                if (mediaItem.mediaMetadata.title != null) mediaItem.mediaMetadata.title.toString() else "未知标题"
            val uri =
                if (mediaItem.localConfiguration != null) mediaItem.localConfiguration!!.uri.toString() else "未知 URI"
            println(
                "媒体项 " + (i + 1) + ": 标题 = " + title + ", URI = " + uri
            )
        }
    }

}
