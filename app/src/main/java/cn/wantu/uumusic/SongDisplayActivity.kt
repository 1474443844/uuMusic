package cn.wantu.uumusic

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import cn.wantu.uumusic.activity.DefaultActivity
import cn.wantu.uumusic.model.MusicPlayerController
import cn.wantu.uumusic.model.getMusicLrc
import cn.wantu.uumusic.ui.widget.LrcFile
import cn.wantu.uumusic.ui.widget.LyricsScreen
import cn.wantu.uumusic.ui.widget.parseLrcString

class SongDisplayActivity : DefaultActivity() {
    private val player = MusicPlayerController.getInstance()

    @Composable
    override fun SetupUI() {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)){
                LyricsApp()
            }
        }
    }

    override fun doBeforeUI() {
    }

    companion object{
        fun showSongDetails(context: Context) {
            val intent = Intent(context, SongDisplayActivity::class.java)
            context.startActivity(intent)
        }
    }
    @Composable
    fun LyricsApp() {
        var lrcFile by remember { mutableStateOf<LrcFile?>(null) }

        // 模拟从网络获取歌词
        LaunchedEffect(player.currentPlayingIndex, player.playList) {
            val lrcContent = getMusicLrc(player.playList[player.currentPlayingIndex].id)

            lrcFile = parseLrcString(lrcContent)
        }


        if (lrcFile != null) {
            LyricsScreen(
                lyrics = lrcFile!!.lyrics,
                currentTime = player.currentPosition,
                metadata = lrcFile!!.metadata
            )
        } else {
            // 显示加载中或占位界面
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "加载中...", color = Color.White, fontSize = 18.sp)
            }
        }
    }
}
