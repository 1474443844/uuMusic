package cn.wantu.uumusic.model

import android.media.MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import cn.wantu.uumusic.data.SongInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MusicController {
    private val mediaPlayer = MediaPlayer()

    companion object{
        var isPlaying by mutableStateOf(false)
        var songIndex by mutableIntStateOf(0)
        var songList = emptyList<SongInfo>().toMutableStateList()
        var progress by mutableFloatStateOf(0f)
        var isPrepared by mutableStateOf(false)
    }

    fun playAtNow(song: SongInfo) {
        songList.indexOfFirst {
            it.id == song.id
        }.also { index ->
            if (index != -1) {
                songIndex = index
                playNew()
            } else {
                if(songList.isNotEmpty()){
                    songIndex += 1
                }
                songList.add(songIndex, song)
                playNew()
            }
        }
    }

    fun playAtNext(song: SongInfo) {
        songList.remove(song)
        if (songList.size == songIndex){
            songList.add(song)
        }else{
            songList.add(songIndex+1, song)
        }
        if(songList.size == 1){
            playNew()
        }
        setOnCompletionListener {
            songIndex++
            playNew()
        }
    }

    private fun playNew(){
        if(songIndex == songList.size){
            songIndex = 0
        }
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope.launch {
            reset()
            isPrepared = false
            println(songList[songIndex])
            val url = getMusicDownloadUrl(songList[songIndex].id)
            println(url)
            setDataSource(url)
            prepareAsync()
            setOnPreparedListener {
                progress = 0f
                start()
                setOnCompletionListener {}
            }
        }
    }

    fun getDuration(): Int {
        return mediaPlayer.duration
    }

    fun prepare() {
        mediaPlayer.prepare()
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer.currentPosition
    }

    fun seekTo(pos: Int) {
        mediaPlayer.seekTo(pos)
    }

    fun isPlaying(): Boolean {
        isPlaying = mediaPlayer.isPlaying
        return isPlaying
    }

    fun setOnBufferingUpdateListener(listener: (progress: Int) -> Unit){
        mediaPlayer.setOnBufferingUpdateListener { _, progress ->
            listener(progress)
        }
    }

    fun getUserSetting() = 0 // 0 -> 顺序播放, 1 -> 单曲循环

    fun setOnCompletionListener(listener: () -> Unit){
        mediaPlayer.setOnCompletionListener {
            isPlaying = false
            when(getUserSetting()) {
                0 -> {
                    songIndex++
                    playNew()
                }
                1 -> {
                    playNew()
                }
            }
            listener()
        }
    }
    fun setOnPreparedListener(listener: () -> Unit){
        mediaPlayer.setOnPreparedListener {
            isPrepared = true
            listener()
        }
    }
    fun setDataSource(url: String) {
        mediaPlayer.setDataSource(url)
    }
    fun prepareAsync(){
        isPrepared = false
        mediaPlayer.prepareAsync()
    }
    fun pause(){
        isPlaying = false
        mediaPlayer.pause()
    }
    fun start() {
        isPlaying = true
        mediaPlayer.start()
        setOnCompletionListener {  }

    }

    fun reset() {
        isPlaying = false
        mediaPlayer.reset()
    }
}