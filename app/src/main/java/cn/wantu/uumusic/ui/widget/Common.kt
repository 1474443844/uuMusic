package cn.wantu.uumusic.ui.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.wantu.uumusic.R
import cn.wantu.uumusic.data.DiskInfo
import cn.wantu.uumusic.data.SongInfo
import cn.wantu.uumusic.model.MusicPlayerController
import cn.wantu.uumusic.ui.theme.UUMusicTheme
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithMusicBar(modifier: Modifier = Modifier,  topBar: @Composable (() -> Unit)? = null, content: @Composable (MusicPlayerController) -> Unit) {
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val player = MusicPlayerController.getInstance()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom).asPaddingValues()) // 动态适配状态栏和导航栏
    ) {
        BottomSheetScaffold(
            modifier = modifier,
            scaffoldState = scaffoldState,
            topBar = topBar,
            sheetContent = {
                Row (Modifier.height(128.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {

                        println("player.playList.size = ${player.playList.size}, player.isPrepared = ${player.isPrepared}")
                        if (player.playList.size > 0 || !player.isPrepared) {
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
                                        player.showPlayList()
                                    }
                            )
                            Text(
                                text = "${mediaMetadata?.title} - ${mediaMetadata?.artist}",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp)
                            )
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                LaunchedEffect(player.isPlaying) {
                                    scope.launch {
                                        while (player.isPlaying) {
                                            player.progress =
                                                player.currentPosition.toFloat() / player.duration
                                            delay(200)
                                        }
                                    }
                                }
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
                    items(player.playList.size) { index ->
                        HorizontalDivider()
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable {
                                scope.launch {
                                    player.playIndex(index)
                                }
                            }) {
                            Text(
                                text = "${player.playList[index].mediaMetadata.title}—${player.playList[index].mediaMetadata.artist}",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(8.dp).weight(1.0f)
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

/*
 * 分页加载列表
@Composable
fun PagingLazyColumn(count: Int, loadData: () -> Unit) {
    val listState = rememberLazyListState()
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex + listState.layoutInfo.visibleItemsInfo.size }
            .collect { visibleItemCount ->
                val totalItemCount = listState.layoutInfo.totalItemsCount
                if (totalItemCount < count) {
                    if (visibleItemCount >= totalItemCount - 5 && !isLoading) {
                        isLoading = true
                        loadData()
                        isLoading = false
                    }
                }
            }
    }
}
*/


 //* 音乐控制栏
@Composable
fun NewMusicControllerBar() {
    val player = MusicPlayerController.getInstance()
    Box(Modifier.height(128.dp)) {
        BottomAppBar {
            if (player.playList.size > 0 || !player.isPrepared) {
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
                            player.showPlayList()
                        }
                )
                Text(
                    text = "${mediaMetadata?.title} - ${mediaMetadata?.artist}",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(end = 8.dp)) {
                    val scope = rememberCoroutineScope()
                    LaunchedEffect(player.isPlaying) {
                        scope.launch {
                            while (player.isPlaying) {
                                player.progress =
                                    player.currentPosition.toFloat() / player.duration
                                delay(200)
                            }
                        }
                    }
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
        items(player.playList) {
            HorizontalDivider()
            Text(text = it.mediaMetadata.title.toString(), modifier = Modifier.padding(8.dp))
        }
    }

}
@Composable
fun NewBannerSection(cover: String, title: String, modifier: Modifier = Modifier) {
    // 这里可以放置轮播图或横幅广告
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(8.dp)),
//        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = cover),
            // painter = painterResource(id = R.drawable.banner_placeholder),
            contentDescription = "Banner",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Text(text = title, modifier = Modifier.background(Color.Black.copy(alpha = 0.6f)).padding(6.dp).align(Alignment.BottomEnd), color = Color.White)
    }
}
// 横幅
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
fun MusicItemView(songInfo: SongInfo, onClick: () -> Unit = {}) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                // 处理点击事件，例如播放音乐或显示详情
                onClick()
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            // 使用 Coil 加载图片
            Image(
                painter = rememberAsyncImagePainter(model = songInfo.cover),
                contentDescription = "${songInfo.song} 封面",
                modifier = Modifier
                    .size(64.dp)
                    .padding(end = 8.dp)
            )
            Column {
                Text(text = songInfo.song, style = MaterialTheme.typography.titleSmall)
                Text(text = songInfo.singer, style = MaterialTheme.typography.bodyMedium)
            }
        }
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
            Image(
                painter = painterResource(R.drawable.baseline_add_music_24),
                contentDescription = "下一首播放",
                colorFilter = ColorFilter.tint(if (isSystemInDarkTheme()) Color.White else Color.Black)
            )
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
            painter = painterResource(id = R.drawable.xxz_rabbit),
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
            painter = painterResource(id = R.drawable.xxz_rabbit),
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