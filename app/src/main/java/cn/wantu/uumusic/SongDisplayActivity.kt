package cn.wantu.uumusic

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import cn.wantu.uumusic.model.MusicPlayerController
import cn.wantu.uumusic.model.getMusicLrc
import cn.wantu.uumusic.ui.theme.UUMusicTheme
import cn.wantu.uumusic.ui.widget.LrcFile
import cn.wantu.uumusic.ui.widget.LyricsScreen
import cn.wantu.uumusic.ui.widget.parseLrcString

class SongDisplayActivity : ComponentActivity() {
    private var id = 0L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        id = intent.getLongExtra("id", 0)
        enableEdgeToEdge()
        setContent {
            UUMusicTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)){
                        LyricsApp()
                    }
                }
            }
        }
    }
    companion object{
        fun showSongDetails(context: Context, id: Long) {
            val intent = Intent(context, SongDisplayActivity::class.java)
            intent.putExtra("id", id)
            context.startActivity(intent)
        }
    }
    @Composable
    fun LyricsApp() {
        val player = MusicPlayerController.getInstance()
        var lrcFile by remember { mutableStateOf<LrcFile?>(null) }

        // 模拟从网络获取歌词
        LaunchedEffect(Unit) {
            val lrcContent = getMusicLrc(id)
            lrcFile = parseLrcString(lrcContent)
        }


        if (lrcFile != null) {
            LyricsScreen(
                lyrics = lrcFile!!.lyrics,
                currentTime = player.currentTime,
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
