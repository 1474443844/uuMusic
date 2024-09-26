package cn.wantu.uumusic.ui.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.wantu.uumusic.R
import cn.wantu.uumusic.UUApp
import cn.wantu.uumusic.data.DiskInfo
import cn.wantu.uumusic.data.SongInfo
import cn.wantu.uumusic.model.MusicController.Companion.isPlaying
import cn.wantu.uumusic.model.MusicController.Companion.isPrepared
import cn.wantu.uumusic.model.MusicController.Companion.progress
import cn.wantu.uumusic.model.MusicController.Companion.songIndex
import cn.wantu.uumusic.model.MusicController.Companion.songList
import cn.wantu.uumusic.ui.theme.UUMusicTheme
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MusicControllerBar() {
    val mediaPlayer = UUApp.getMediaPlayer()
    BottomAppBar(
        content = {
            if (songList.isNotEmpty()) {
                val songInfo = songList[songIndex]
                AsyncImage(
                    model = songInfo.cover,
                    contentDescription = "Album",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(48.dp)
                )
                Text(
                    text = "${songInfo.song} - ${songInfo.singer}",
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                )
//                IconButton(onClick = onPreviousClicked) {
//                    Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = "上一首")
//                }
                Box(contentAlignment = Alignment.Center) {
                    val scope = rememberCoroutineScope()
                    LaunchedEffect(isPlaying) {
                        scope.launch {
                            while (isPlaying) {
                                progress = mediaPlayer.getCurrentPosition()
                                    .toFloat() / mediaPlayer.getDuration()
                                delay(200)
                            }
                        }
                    }
                    if (isPrepared) {
                        CircularProgressIndicator(
                            progress = { progress },
                            trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
                        )
                    } else {
                        CircularProgressIndicator()
                    }
                    IconButton(onClick = {
                        if (isPlaying) {
                            mediaPlayer.pause()
                        } else {
                            mediaPlayer.start()
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
                }

//                IconButton(onClick = onNextClicked) {
//                    Icon(imageVector = Icons.Default.SkipNext, contentDescription = "下一首")
//                }
            } else {
                Image(
                    painter = painterResource(R.drawable.baseline_library_music_24),
                    contentDescription = "Album",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(48.dp)
                )
                Text(
                    text = "UU音乐 听你想听"
                )
            }
        }
    )
}


@Composable
fun BannerSection() {
    // 这里可以放置轮播图或横幅广告
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            // painter = painterResource(id = R.drawable.banner_placeholder),
            contentDescription = "Banner",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Text(text = "我是一个Banner")
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
fun PlaylistItem(diskInfo: DiskInfo, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.width(120.dp)
    ) {
        AsyncImage(
            model = diskInfo.picurl,
            contentDescription = diskInfo.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Text(
            text = diskInfo.title,
            maxLines = 1,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun SongItem(songInfo: SongInfo, modifier: Modifier = Modifier, play: () -> Unit = {}) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(vertical = 8.dp)
    ) {
        AsyncImage(
            model = songInfo.cover,
            contentDescription = "Album",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Column(
            modifier = Modifier.padding(start = 12.dp).weight(1f)
        ) {
            Text(
                text = songInfo.song,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = songInfo.singer,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        IconButton(onClick = { play() }) {
            Image(painter = painterResource(R.drawable.add_music), contentDescription = "下一首播放")
        }
    }
}

@Composable
fun ArtistSection() {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(10) {
            ArtistItem()
        }
    }
}

@Composable
fun ArtistItem() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Image(
//            painter = painterResource(id = R.drawable.artist_placeholder),
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = "Artist",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(40.dp))
        )
        Text(
            text = "歌手姓名",
            maxLines = 1,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun NewSongsSection() {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        repeat(5) {
            SongItem()
        }
    }
}

@Composable
fun SongItem() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Image(
//            painter = painterResource(id = R.drawable.album_placeholder),
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = "Album",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Column(
            modifier = Modifier.padding(start = 12.dp)
        ) {
            Text(
                text = "歌曲名称",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "歌手名称",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    UUMusicTheme {

    }
}