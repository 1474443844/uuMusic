package cn.wantu.uumusic.model

import android.os.Bundle
import androidx.annotation.OptIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

@OptIn(UnstableApi::class)
class MusicPlayer {

    companion object {
        var isPlaying by mutableStateOf(false)
        var isPrepared by mutableStateOf(true)
        var progress by mutableFloatStateOf(0f)
        private var player: ExoPlayer
        init {
            val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                .setAllowCrossProtocolRedirects(true) // 允许跨协议重定向（https -> http）
            val dataSourceFactory: DataSource.Factory =
                DefaultDataSource.Factory(UUApp.getContext(), httpDataSourceFactory)
            player = ExoPlayer.Builder(UUApp.getInstance()).setMediaSourceFactory(
                DefaultMediaSourceFactory(dataSourceFactory)
            ).build()
            player.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                    isPlaying = isPlayingNow
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
//                    println("Playback state changed: $playbackState")
//                    println("size is ${player.mediaItemCount}")
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
//                    println("Am I ready? $isPrepared")
                }
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    super.onMediaItemTransition(mediaItem, reason)
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
            player.prepare()
        }
        fun getInstance() = player

        suspend fun createMediaItem(id: Long): MediaItem {
            val mediaInfo = getMusicMediaInfo(id)
            val bundle = Bundle()
            bundle.putString("cover", mediaInfo.cover)
            val mediaMetadata = MediaMetadata.Builder()
                .setTitle(mediaInfo.song) // 设置标题
                .setArtist(mediaInfo.singer)    // 可选：设置艺术家
                .setAlbumTitle(mediaInfo.album)  // 可选：设置专辑标题
                .setExtras(bundle)
                .build()
            // 创建 MediaItem 并关联 MediaMetadata
            return MediaItem.Builder()
                .setMediaId(id.toString())
                .setUri(mediaInfo.url)
                .setMediaMetadata(mediaMetadata)
                .build()
        }
        suspend fun playAtNow(id: Long){
            progress = 0f
            if(player.mediaItemCount == 0){
                player.addMediaItem(createMediaItem(id))
                return player.play()
            }
            val i = findMediaItemIndex(id)
            if (i == -1){
                player.addMediaItem(player.currentMediaItemIndex+1, createMediaItem(id))
                player.seekToNext()
                return player.play()
            }
            println("find index is $i")
            player.seekTo(i, 0)
            player.play()
        }
        suspend fun playAtNext(id: Long){
            if (0 == player.mediaItemCount){
                return playAtNow(id)
            }
            val i = findMediaItemIndex(id)
            if(i == -1){
                return player.addMediaItem(player.currentMediaItemIndex+1, createMediaItem(id))
            }
            println("find index is $i")
            player.moveMediaItem(i, player.currentMediaItemIndex+1)
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
        fun showPlayList(){
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
}