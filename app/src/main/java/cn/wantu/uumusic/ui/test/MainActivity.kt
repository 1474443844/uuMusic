package cn.wantu.uumusic.ui.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import cn.wantu.uumusic.R
import cn.wantu.uumusic.model.MusicPlayer
import cn.wantu.uumusic.model.MusicPlayer.Companion.isPlaying
import cn.wantu.uumusic.ui.test.ui.theme.UUMusicTheme
import cn.wantu.uumusic.ui.widget.NewMusicControllerBar
import kotlinx.coroutines.launch

@UnstableApi
class MainActivity : ComponentActivity() {

    private val player = MusicPlayer.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            player.addMediaItem(MusicPlayer.createMediaItem(294235538))
            player.prepare()
        }

        enableEdgeToEdge()
        setContent {

            UUMusicTheme {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 播放/暂停按钮
                    IconButton(onClick = {
                        if (player.isPlaying) {
                            player.pause()
                        } else {
                            player.play()
                        }
                    }) {
                        if (isPlaying) {
                            Image(
                                painter = painterResource(R.drawable.baseline_pause_24),
                                contentDescription = "暂停"
                            )
                        } else {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "播放")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = {
                        lifecycleScope.launch {
                            MusicPlayer.playAtNext(4829977)
//                            player.addMediaItem(1, MusicPlayer.createMediaItem(4829977))
//                            player.addMediaItem(1, MusicPlayer.createMediaItem(1289597))
//                            player.removeMediaItem(0)
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
                    }) {
                        Text(text = "下载")
                    }
                    Text(text = "列表数量: ")
                    NewMusicControllerBar()
                }
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    UUMusicTheme {

    }
}