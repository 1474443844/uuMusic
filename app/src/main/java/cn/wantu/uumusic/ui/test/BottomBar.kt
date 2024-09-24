package cn.wantu.uumusic.ui.test

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cn.wantu.uumusic.R
import cn.wantu.uumusic.ui.theme.UUMusicTheme

@Preview
@Composable
private fun MBBPreview() {
    UUMusicTheme {
        MusicBottomBar(
            songTitle = "歌曲名称",
            artistName = "歌手名称",
            isPlaying = true,
            progress = 0.5f,
            onPlayPauseClicked = {},
            onNextClicked = {},
            onPreviousClicked = {})
    }
}

@Composable
fun MusicBottomBar(
    songTitle: String,
    artistName: String,
    isPlaying: Boolean,
    progress: Float,
    onPlayPauseClicked: () -> Unit,
    onNextClicked: () -> Unit,
    onPreviousClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
//        LinearProgressIndicator(
//            progress = { progress },
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(4.dp),
//        )
        BottomAppBar(
            modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer),
            content = {
                Text(
                    text = "$songTitle - $artistName",
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                )
//                IconButton(onClick = onPreviousClicked) {
//                    Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = "上一首")
//                }
                IconButton(onClick = onPlayPauseClicked) {
                    if (isPlaying) {
                        Image(painter = painterResource(R.drawable.baseline_pause_24), contentDescription = "暂停")
                    } else {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "播放")
                    }
                }
//                IconButton(onClick = onNextClicked) {
//                    Icon(imageVector = Icons.Default.SkipNext, contentDescription = "下一首")
//                }
            }
        )
    }
}
