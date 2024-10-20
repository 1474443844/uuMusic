package cn.wantu.uumusic.model

import android.os.Bundle
import androidx.annotation.OptIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(UnstableApi::class)
class MusicPlayerController private constructor() {

    private val player: ExoPlayer

    var isPlaying by mutableStateOf(false)
        private set
    var isPrepared by mutableStateOf(true)
        private set
    var progress by mutableFloatStateOf(0f)
    var playList = emptyList<MediaItem>().toMutableStateList()
        private set

    val currentMediaItem: MediaItem?
        get() = player.currentMediaItem
    val currentPosition: Long
        get() = player.currentPosition
    val duration: Long
        get() = player.duration
    val currentMediaItemIndex: Int
        get() = player.currentMediaItemIndex
    val mediaItemCount: Int
        get() = playList.size
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
                isPlaying = isPlayingNow
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                println("Playback state changed: $playbackState")
                when(playbackState){
                    Player.STATE_BUFFERING -> {
                        isPrepared = false
                    }

                    Player.STATE_ENDED -> {
//                            TODO()
                    }

                    Player.STATE_IDLE -> {
//                            TODO()
                    }

                    Player.STATE_READY -> {
                        isPrepared = true
                    }
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                println("Media item transition: $mediaItem, reason: $reason")
                when (reason) {
                    Player.MEDIA_ITEM_TRANSITION_REASON_AUTO -> {
                        player.pause()
                        player.play()
                    }

                    Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED -> {
//                            isPrepared = false
                    }

                    Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT -> {
//                            TODO()
                    }

                    Player.MEDIA_ITEM_TRANSITION_REASON_SEEK -> {
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
        val mediaInfo = getMusicMediaInfo(id)
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
        val musicCache = File(downloadDir, "$id.json")
        if (musicCache.exists()) {

        } else {

            playAtNext(id)
        }
    }


    suspend fun playAtNow(id: Long) {
        progress = 0f
        if (mediaItemCount == 0) {
            val mediaItem = createMediaItem(id)
            player.addMediaItem(mediaItem)
            player.play()
            playList.add(mediaItem)
            return
        }
        val mediaItemIndex = findMediaItemIndex(id)
        if (mediaItemIndex == -1) {
            val mediaItem = createMediaItem(id)
            player.addMediaItem(player.currentMediaItemIndex + 1, mediaItem)
            player.seekToNext()
            player.play()
            playList.add(player.currentMediaItemIndex, mediaItem)
        } else {
            player.seekTo(mediaItemIndex, 0)
            player.play()
        }
    }

    suspend fun playAtNext(id: Long) {
        if (player.mediaItemCount == 0) {
            playAtNow(id)
            return
        }
        val mediaItemIndex = findMediaItemIndex(id)
        if (mediaItemIndex == -1) {
            val mediaItem = createMediaItem(id)
            player.addMediaItem(player.currentMediaItemIndex + 1, mediaItem)
            playList.add(player.currentMediaItemIndex, mediaItem)
        } else {
            player.moveMediaItem(mediaItemIndex, player.currentMediaItemIndex + 1)
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

//    private fun updatePlayList() {
//        val mediaItemCount = player.mediaItemCount
//        for (i in 0 until mediaItemCount) {
//            val mediaMetadata = player.getMediaItemAt(i).mediaMetadata
//            list.add("${mediaMetadata.title} - ${mediaMetadata.artist}")
//        }
//        playList = list
//    }

    fun playIndex(index: Int) {
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
