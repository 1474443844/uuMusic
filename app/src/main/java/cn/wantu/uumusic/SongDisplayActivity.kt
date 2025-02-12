package cn.wantu.uumusic

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toFile
import cn.wantu.uumusic.activity.DefaultActivity
import cn.wantu.uumusic.controller.MusicPlayerController
import cn.wantu.uumusic.controller.SettingConfig
import cn.wantu.uumusic.controller.SettingConfig.downloadDir
import cn.wantu.uumusic.controller.generateMediaInfo
import cn.wantu.uumusic.controller.getMusicLrc
import cn.wantu.uumusic.ui.widget.LrcFile
import cn.wantu.uumusic.ui.widget.LyricsScreen
import cn.wantu.uumusic.ui.widget.parseLrcString
import cn.wantu.uumusic.utils.MusicMetadataHelper
import java.io.File

class SongDisplayActivity : DefaultActivity() {
    private val player = MusicPlayerController.getInstance()

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun SetupUI() {
        var lrcFile by remember { mutableStateOf<LrcFile?>(null) }
        var title by remember { mutableStateOf("未知标题") }
        var artist by remember { mutableStateOf("未知艺术家") }
        // 模拟从网络获取歌词
        LaunchedEffect(player.currentPlayingIndex, player.playList) {
            val currentSong = player.playList[player.currentPlayingIndex]

            val musicCache =
                File(downloadDir, "info/${SettingConfig.quality}/${currentSong.id}.json")
            if (musicCache.exists()) {
                val info = MusicMetadataHelper.getMusicMetadata(
                    Uri.parse(generateMediaInfo(musicCache).url).toFile().toString()
                )
                title = info.title!!
                artist = info.artist!!
            }
            val lrcContent = getMusicLrc(currentSong.id)

            lrcFile = parseLrcString(lrcContent)
        }
        Scaffold(topBar = {
            TopAppBar(title = {
                Text(
                    text = "$title - $artist",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(16.dp)
                )
            })

        }, modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {

                if (lrcFile != null) {
                    LyricsScreen(
                        lyrics = lrcFile!!.lyrics,
                        currentTime = player.currentPosition,
                        metadata = lrcFile!!.metadata,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                } else {
                    // 显示加载中或占位界面
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "加载中...", color = Color.White, fontSize = 18.sp)
                    }
                }

                SeekBar(modifier = Modifier.padding(16.dp))
            }
        }
    }

    override fun doBeforeUI() {

    }

    @Composable
    fun SeekBar(modifier: Modifier = Modifier) {

        var sliderPosition by remember { mutableFloatStateOf(player.currentPosition.toFloat()) }
        Slider(
            value = sliderPosition,
            onValueChange = {
                sliderPosition = it
            },
            valueRange = 0f..player.duration.toFloat(),  // 设置范围
            onValueChangeFinished = {
                MusicPlayerController.player.seekTo(sliderPosition.toLong())
            },
            modifier = modifier
        )
    }

    companion object{
        fun showSongDetails(context: Context) {
            val intent = Intent(context, SongDisplayActivity::class.java)
            context.startActivity(intent)
        }
    }

}
