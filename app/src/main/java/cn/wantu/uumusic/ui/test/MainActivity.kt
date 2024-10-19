package cn.wantu.uumusic.ui.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.wantu.uumusic.R
import cn.wantu.uumusic.ui.test.ui.theme.UUMusicTheme
import coil.compose.rememberAsyncImagePainter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 设置 Compose 内容
        setContent {
            UUMusicTheme {
                MusicSearchScreen()
            }
        }
    }
}

// 数据模型
data class MusicItem(
    val title: String,
    val artist: String,
    val albumArtUrl: String
)

// 示例音乐数据
val sampleMusicData = listOf(
    MusicItem("Shape of You", "Ed Sheeran", "https://linktoimage.com/shape_of_you.jpg"),
    MusicItem("Blinding Lights", "The Weeknd", "https://linktoimage.com/blinding_lights.jpg"),
    MusicItem("Levitating", "Dua Lipa", "https://linktoimage.com/levitating.jpg"),
    // 添加更多示例数据
)

// 主界面
@Composable
fun MusicSearchScreen() {
    var query by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<MusicItem>>(emptyList()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 搜索栏
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("搜索音乐") },
            modifier = Modifier
                .fillMaxWidth(),
            singleLine = true,
            trailingIcon = { Icon(Icons.Filled.Search, contentDescription = "搜索图标") },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    // 执行搜索操作
                    searchResults = performSearch(query)
                }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 搜索结果列表
        if (searchResults.isNotEmpty()) {
            LazyColumn {
                items(searchResults) { music ->
                    MusicItemView(music = music)
                }
            }
        } else {
            // 如果没有搜索结果，可以显示提示信息
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("请输入关键词搜索音乐")
            }
        }
    }
}

// 搜索逻辑（示例）
fun performSearch(query: String): List<MusicItem> {
    if (query.isBlank()) return emptyList()
    // 简单的过滤示例，实际应用中可以调用API
    return sampleMusicData.filter {
        it.title.contains(query, ignoreCase = true) ||
                it.artist.contains(query, ignoreCase = true)
    }
}

// 单个音乐项视图
@Composable
fun MusicItemView(music: MusicItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                // 处理点击事件，例如播放音乐或显示详情
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            // 使用 Coil 加载图片
            Image(
                painter = rememberAsyncImagePainter(model = music.albumArtUrl),
                contentDescription = "${music.title} 封面",
                modifier = Modifier
                    .size(64.dp)
                    .padding(end = 8.dp)
            )
            Column {
                Text(text = music.title, style = MaterialTheme.typography.titleSmall)
                Text(text = music.artist, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
    TopAppBar(
        title = { Text(text = stringResource(R.string.app_name), fontSize = 18.sp) },

    )
}

@Preview(showBackground = true)
@Composable
fun TopBarPreview() {
    TopBar()
}