@file:kotlin.OptIn(DelicateCoroutinesApi::class)

package cn.wantu.uumusic.controller

import android.os.Bundle
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.media3.session.MediaController
import cn.wantu.uumusic.R
import cn.wantu.uumusic.SongDisplayActivity
import cn.wantu.uumusic.UUApp
import cn.wantu.uumusic.controller.SettingConfig.downloadDir
import cn.wantu.uumusic.controller.SettingConfig.isCache
import cn.wantu.uumusic.extenstions.DraggableItem
import cn.wantu.uumusic.extenstions.dragContainer
import cn.wantu.uumusic.extenstions.rememberDragDropState
import cn.wantu.uumusic.model.SongInfo
import coil.compose.AsyncImage
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import java.io.File
import java.io.FileOutputStream

@OptIn(UnstableApi::class)
class MusicPlayerController private constructor() {

    // 是否在播放, 默认不在
    var isPlaying by mutableStateOf(false)
        private set

    // 是否准备好了, 默认准备好了
    var isPrepared by mutableStateOf(true)
        private set

    // 上一次播放列表配置文件
    val plF = File(UUApp.instance.filesDir, "playingList.json")

    // 播放列表
    val playList = emptyList<PlayListItem>().toMutableStateList()

    // 播放器当前的播放位置, 单位毫秒
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


    fun setupPlayerListener() {
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
                        currentPlayingIndex = _mCurrentPlayingIndex
                        progress = 0f
                    }
                }

            }
        })
        player.repeatMode = Player.REPEAT_MODE_ALL
        player.prepare()

        GlobalScope.launch {
            if (plF.exists()) {
                addList2player(UUApp.json.decodeFromString(plF.readText()))
            }
            while (true) {
                if (isPlaying) {
                    updateProgress()
                    delay(100)
                }
            }
        }
    }

    companion object {

        lateinit var player: Player

        @Volatile
        private var instance: MusicPlayerController? = null

        fun getInstance(): MusicPlayerController =
            instance ?: synchronized(this) {
                instance ?: MusicPlayerController().also { instance = it }
            }
        fun setController(controller: MediaController) {
            player = controller
            instance?.setupPlayerListener()
        }
    }

    private suspend fun createMediaItem(id: Long): MediaItem = withContext(Dispatchers.IO) {
        val musicCache = File(downloadDir, "info/${SettingConfig.quality}/$id.json")
        val mediaInfo = if (isCache) {
            if (musicCache.exists()) {
                generateMediaInfo(musicCache)
            } else {
                getMusicMediaInfo(id, SettingConfig.quality)
            }
        } else {
            getMusicMediaInfo(id, SettingConfig.quality)
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

    private fun savePlayingList() {
        FileOutputStream(plF).use { output ->
            output.write(
                UUApp.json.encodeToString(
                    playList.toList().also {
                        println(it)
                    }
                ).toByteArray()
            )
        }
    }

    private suspend fun addList2player(list: List<PlayListItem>) = withContext(Dispatchers.Main) {
        for (item in list) {
            playList.add(item)
            val mediaItem = createMediaItem(item.id) // generate MediaItem
            player.addMediaItem(mediaItem) // add MediaItem to player
        }
    }

    suspend fun playAtNow(song: SongInfo) {
        playAtNow(id = song.id, song = song.song, singer = song.singer, cover = song.cover)
    }

    suspend fun playAtNow(id: Long, song: String, singer: String, cover: String) {
        playAtNow(PlayListItem(id, song, singer, cover))
    }

    suspend fun playAtNow(playItem: PlayListItem) {
        pause() // stop Music
        progress = 0f
        isPrepared = false
        if (playListCount == 0) { // if there is nothing in playList
            currentPlayingIndex = 0 // reset currentIndex = 0
            playList.add(playItem) // add playItem to playList
            savePlayingList()
            val mediaItem = createMediaItem(playItem.id) // generate MediaItem
            player.addMediaItem(mediaItem) // add MediaItem to player
            mPlay() // play music
            return
        }
        val itemIndex = findItemIndex(playItem.id)
        // if there is no item equaling dest
        if (itemIndex == -1) {
            // 将指针移到下一首
            currentPlayingIndex += 1
            // add playItem to playList
            playList.add(currentPlayingIndex, playItem)
            savePlayingList()
            val mediaItem = createMediaItem(playItem.id)
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
        playAtNext(id = song.id, song = song.song, singer = song.singer, cover = song.cover)
    }

    suspend fun playAtNext(id: Long, song: String, singer: String, cover: String) {
        if (playListCount == 0) {
            playAtNow(id, song, singer, cover)
            return
        }
        val itemIndex = findItemIndex(id)
        if (itemIndex == -1) {
            playList.add(
                currentPlayingIndex + 1 + offset,
                PlayListItem(id, song, singer, cover)
            )
            savePlayingList()
            val mediaItem = createMediaItem(id)
            player.addMediaItem(_mCurrentPlayingIndex + 1 + offset, mediaItem)
            println("should be insert ${currentPlayingIndex + 1 + offset}")
            offset += 1
        } else if (itemIndex != currentPlayingIndex) {
            println("mediaItemIndex = $itemIndex, currentMediaItemIndex = $_mCurrentPlayingIndex")
            val mediaItem = playList[itemIndex]
            playList.remove(mediaItem)
            if (itemIndex < currentPlayingIndex) {
                currentPlayingIndex -= 1
            }
            playList.add(currentPlayingIndex + offset + 1, mediaItem)
            savePlayingList()
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

    fun drag(from: Int, to: Int) {
        playList.apply { add(to, removeAt(from)) }
        player.moveMediaItem(from, to)
        savePlayingList()
        currentPlayingIndex = _mCurrentPlayingIndex
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
                val listState = rememberLazyListState()
                val dragDropState =
                    rememberDragDropState(listState) { fromIndex, toIndex ->
                        player.drag(fromIndex, toIndex)
                    }
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxHeight(0.4f)
                        .dragContainer(dragDropState)
                ) {
                    itemsIndexed(player.playList, key = { _, item -> item.id }) { index, item ->
                        DraggableItem(dragDropState, index) { isDragging ->
                            Row(verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { scope.launch { player.playIndex(index) } }) {
                                Text(
                                    text = "${item.title}—${item.singer}",
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
    val title: String = "",
    val singer: String = "",
    val cover: String = "",
)