package cn.wantu.uumusic.model

import android.os.Bundle
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import cn.wantu.uumusic.R
import cn.wantu.uumusic.SongDisplayActivity
import cn.wantu.uumusic.UUApp
import cn.wantu.uumusic.data.SongInfo
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
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

    // 播放列表
    var playList = emptyList<PlayListItem>().toMutableStateList()
        private set

    private val _mCurrentPosition: Long
        get() = player.currentPosition.also {
            currentPosition = it
            progress = it.toFloat() / duration
        }

    // 当前播放进度
    var currentPosition by mutableLongStateOf(0L)

    // 播放总时长
    val duration: Long
        get() = player.duration

    // 播放进度
    var progress by mutableFloatStateOf(0f)

    private suspend fun updateProgress() {
        withContext(Dispatchers.Main) {
            _mCurrentPosition
        }
    }

    // 当前媒体项索引
    private val _mCurrentPlayingIndex: Int
        get() = player.currentMediaItemIndex

    // 当前播放索引
    var currentPlayingIndex by mutableIntStateOf(0)

    // 媒体项总数
    val playListCount: Int
        get() = playList.size

    private var offset = 0

    // 初始化媒体播放器
    init {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true) // 允许跨协议重定向（https -> http）
        val dataSourceFactory: DataSource.Factory =
            DefaultDataSource.Factory(UUApp.context, httpDataSourceFactory)
        player = ExoPlayer.Builder(UUApp.instance)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .build()
        setupPlayerListener()
        player.repeatMode = Player.REPEAT_MODE_ALL
        player.prepare()
        GlobalScope.launch {
            while (true) {
                if (isPlaying) {
                    updateProgress()
                    delay(100)
                }
            }
        }
    }

    private fun setupPlayerListener() {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                isPlaying = isPlayingNow
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
//                println("Playback state changed: $playbackState")
                when (playbackState) {
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
                offset = 0
                when (reason) {
                    Player.MEDIA_ITEM_TRANSITION_REASON_AUTO -> { // 1
                        pause()
                        play()
                        currentPlayingIndex = _mCurrentPlayingIndex
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
        })
    }

    companion object {
        val downloadDir = File(UUApp.instance.externalCacheDir, "music").also {
            val f = File(it, "info")
            if (!f.exists()) {
                f.mkdirs()
            }
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
        val mediaInfo = if (isCache) {
            val musicCache = File(downloadDir, "info/$id.json")
            if (musicCache.exists()) {
                generateMediaInfo(musicCache)
            } else {
                getMusicMediaInfo(id)
            }
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
    }

    fun pause() {
        player.pause()
    }

    fun playPrevious() {
        player.seekToPrevious()
    }

    fun playNext() {
        player.seekToNext()
    }


    suspend fun playAtNow(song: SongInfo) {
        pause() // stop Music
        progress = 0f
        isPrepared = false
        val id = song.id
        val playItem = PlayListItem(id, song.song, song.singer, song.cover) // generate playListItem
        if (playListCount == 0) { // if there is nothing in playList
            currentPlayingIndex = 0 // reset currentIndex = 0
            playList.add(playItem) // add playItem to playList
            val mediaItem = createMediaItem(id) // generate MediaItem
            player.addMediaItem(mediaItem) // add MediaItem to player
            mPlay() // play music
            return
        }
        val itemIndex = findItemIndex(id)
        // if there is no item equaling dest
        if (itemIndex == -1) {
            // 将指针移到下一首
            currentPlayingIndex += 1
            // add playItem to playList
            playList.add(currentPlayingIndex, playItem)
            val mediaItem = createMediaItem(id)
            // add MediaItem to player
            player.addMediaItem(_mCurrentPlayingIndex + 1, mediaItem)
            // play music
            mPlay()
        } else {
            // 指针移到歌曲
            currentPlayingIndex = itemIndex
            // play music
            mPlay()
        }
    }

    suspend fun playAtNext(song: SongInfo) {
        if (playListCount == 0) {
            playAtNow(song)
            return
        }
        val id = song.id
        val itemIndex = findItemIndex(id)
        if (itemIndex == -1) {
            playList.add(
                currentPlayingIndex + 1 + offset,
                PlayListItem(id, song.song, song.singer, song.cover)
            )
            val mediaItem = createMediaItem(id)
            player.addMediaItem(_mCurrentPlayingIndex + 1 + offset, mediaItem)
            println("should be insert ${currentPlayingIndex + 1 + offset}")
            offset += 1
        } else if(itemIndex != currentPlayingIndex) {
            println("mediaItemIndex = $itemIndex, currentMediaItemIndex = $_mCurrentPlayingIndex")
            val mediaItem = playList[itemIndex]
            playList.remove(mediaItem)
            if(itemIndex < currentPlayingIndex){
                currentPlayingIndex -= 1
            }
            playList.add(currentPlayingIndex+offset+1, mediaItem)
            player.moveMediaItem(itemIndex, _mCurrentPlayingIndex + 1 + offset)
            offset += 1
        }
    }

    private fun findItemIndex(id: Long) = playList.indexOfFirst { it.id == id }

    fun remove(index: Int) {
        player.removeMediaItem(index)
        currentPlayingIndex = _mCurrentPlayingIndex
        playList.removeAt(index)
    }

    // play currentMediaItemIndex
    private fun mPlay(index: Int = currentPlayingIndex) {
        if (isPlaying) return
        if (index in 0 until player.mediaItemCount) {
            player.seekTo(index, 0)
            player.play()
            println("currentIndex: $currentPlayingIndex, currentMediaItemIndex: $_mCurrentPlayingIndex")
        }
    }

    fun playIndex(index: Int) {
        println("playIndex: $index, currentIndex: $currentPlayingIndex")
        if (index == currentPlayingIndex) {
            return
        }
        pause()
        currentPlayingIndex = index
        mPlay()
    }

    fun showPlayList() {
        println("currentIndex: $currentPlayingIndex, currentMediaItemIndex: $_mCurrentPlayingIndex")
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

    fun fixPlayerList() {

    }
}


@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithMusicBar(
    modifier: Modifier = Modifier,
    topBar: @Composable (() -> Unit)? = null,
    content: @Composable (MusicPlayerController) -> Unit
) {
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val player = MusicPlayerController.getInstance()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                WindowInsets.systemBars
                    .only(WindowInsetsSides.Bottom)
                    .asPaddingValues()
            ) // 动态适配状态栏和导航栏
    ) {
        BottomSheetScaffold(
            modifier = modifier,
            scaffoldState = scaffoldState,
            topBar = topBar,
            sheetContent = {
                Row(Modifier.height(96.dp)) {
                    val context = LocalContext.current
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = if (player.playListCount != 0) {
                            Modifier.clickable {
                                SongDisplayActivity.showSongDetails(context)
                            }
                        } else Modifier
                    ) {
//                        println("player.playListCount = ${player.playListCount}, player.isPrepared = ${player.isPrepared}")
                        if (player.playListCount > 0) {
                            val playingItem = player.playList[player.currentPlayingIndex]
//                            println("playingItem = $playingItem")
                            AsyncImage(
                                model = playingItem.cover,
                                contentDescription = "Album",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(48.dp)
                                    .clickable {
                                        player.showPlayList()
                                    }
                            )
                            Text(
                                text = "${playingItem.title} - ${playingItem.singer}",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp)
                            )
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {

                                if (player.isPrepared) {
                                    CircularProgressIndicator(
                                        progress = { player.progress },
                                        trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
                                    )
                                } else {
                                    CircularProgressIndicator()
                                }
                                IconButton(onClick = {
                                    if (player.isPlaying) {
                                        player.pause()
                                    } else {
                                        player.play()
                                    }
                                }) {
                                    Image(
                                        painter = painterResource(if (player.isPlaying) R.drawable.baseline_pause_24 else R.drawable.baseline_play_arrow_24),
                                        contentDescription = if (player.isPlaying) "暂停" else "播放",
                                        colorFilter = ColorFilter.tint(if (isSystemInDarkTheme()) Color.White else Color.Black)
                                    )
                                }
                            }
                        } else {
                            Image(
                                painter = painterResource(R.drawable.baseline_music_disc_24),
                                contentDescription = "Album",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(36.dp),
                                colorFilter = ColorFilter.tint(if (isSystemInDarkTheme()) Color.White else Color.Black)
                            )
                            Text(
                                text = "UU音乐 听你想听"
                            )
                        }
                    }
                }
                LazyColumn(modifier = Modifier.fillMaxHeight(0.4f)) {
                    items(player.playListCount) { index ->
                        HorizontalDivider()
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch {
                                        player.playIndex(index)
                                    }
                                }) {
                            Text(
                                text = "${player.playList[index].title}—${player.playList[index].singer}",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (player.currentPlayingIndex == index) Color.Unspecified else Color.Gray,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .weight(1.0f)
                            )
                            IconButton(onClick = {
                                player.remove(index)
                            }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear")
                            }
                        }
                    }
                }
            },
            sheetPeekHeight = 128.dp,
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                content(player)
            }
        }
    }
}

@Serializable
data class PlayListItem(
    val id: Long,
    var title: String = "",
    var singer: String = "",
    var cover: String = "",
)