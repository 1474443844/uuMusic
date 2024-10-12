package cn.wantu.uumusic.ui.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import cn.wantu.uumusic.data.DiskInfo
import cn.wantu.uumusic.data.SongInfo
import cn.wantu.uumusic.model.MusicPlayer
import cn.wantu.uumusic.ui.theme.UUMusicTheme
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NewMusicControllerBar(isExpanded: Boolean = false, onExpand: () -> Unit = {}) {
    val player = MusicPlayer.getInstance()
    Column {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + slideInVertically(
                // 从按钮下方向上展开
                initialOffsetY = { it },
                animationSpec = tween(durationMillis = 300)
            ),
            exit = fadeOut() + slideOutVertically(
                // 向下收回
                targetOffsetY = { it },
                animationSpec = tween(durationMillis = 300)
            ),

        ) {
            // 列表项，可以根据需求自定义
            Card(modifier = Modifier.fillMaxWidth().height(200.dp).padding(8.dp), elevation = CardDefaults.cardElevation(8.dp)) {
                LazyColumn {
                    items(MusicPlayer.playList.size){ i ->
                        Text(
                            text = MusicPlayer.playList[i],
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { MusicPlayer.playIndex(i) }
                                .padding(8.dp)
                        )
                    }
                }
            }

        }
        BottomAppBar(
            content = {
                if (player.mediaItemCount > 0 || !MusicPlayer.isPrepared) {
                    val mediaItem = player.currentMediaItem
                    val mediaMetadata = mediaItem?.mediaMetadata
                    val extras = mediaMetadata?.extras
                    AsyncImage(
                        model = extras?.getString("cover"),
                        contentDescription = "Album",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(48.dp)
                            .clickable {
                                MusicPlayer.showPlayList()
                            }
                    )
                    Text(
                        text = "${mediaMetadata?.title} - ${mediaMetadata?.artist}",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )
//                IconButton(onClick = {
//                    MusicPlayer.playPrevious()
//                }) {
//                    Image(painter = painterResource(R.drawable.baseline_skip_previous_24), contentDescription = "上一首")
//                }
                    Box(contentAlignment = Alignment.Center) {
                        val scope = rememberCoroutineScope()
                        LaunchedEffect(MusicPlayer.isPlaying) {
                            scope.launch {
                                while (MusicPlayer.isPlaying) {
                                    MusicPlayer.progress = player.currentPosition.toFloat()/player.duration
                                    delay(200)
                                }
                            }
                        }
//                    println("isPrepared: ${MusicPlayer.isPrepared}")
                        if (MusicPlayer.isPrepared) {
                            CircularProgressIndicator(
                                progress = { MusicPlayer.progress },
                                trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
                            )
                        } else {
                            CircularProgressIndicator()
                        }
                        IconButton(onClick = {
                            if (MusicPlayer.isPlaying) {
                                player.pause()
                            } else {
                                player.play()
                            }
                        }) {
                            Image(
                                painter = painterResource(if(MusicPlayer.isPlaying) R.drawable.baseline_pause_24 else R.drawable.baseline_play_arrow_24),
                                contentDescription = if(MusicPlayer.isPlaying) "暂停" else "播放"
                            )
                        }
                    }

                    IconButton(onClick = { onExpand() }) {
                        Image(painter = painterResource(R.drawable.baseline_music_list_24), contentDescription = "播放列表")
                    }
                } else {
                    Image(
                        painter = painterResource(R.drawable.baseline_music_disc_24),
                        contentDescription = "Album",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(36.dp)
                    )
                    Text(
                        text = "UU音乐 听你想听"
                    )
                }
            }
        )
    }

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
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
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
            Image(painter = painterResource(R.drawable.baseline_add_music_24), contentDescription = "下一首播放")
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