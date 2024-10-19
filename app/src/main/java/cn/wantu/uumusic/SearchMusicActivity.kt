package cn.wantu.uumusic

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cn.wantu.uumusic.data.SongInfo
import cn.wantu.uumusic.model.MusicPlayer
import cn.wantu.uumusic.model.searchMusicByName
import cn.wantu.uumusic.ui.theme.UUMusicTheme
import cn.wantu.uumusic.ui.widget.MusicItemView
import kotlinx.coroutines.launch

class SearchMusicActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UUMusicTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var query by remember { mutableStateOf("") }
                    var searchResults by remember { mutableStateOf<List<SongInfo>>(emptyList()) }
                    val scope = rememberCoroutineScope()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = innerPadding.calculateTopPadding(), start = 16.dp, end = 16.dp)
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
                                    scope.launch {
                                        searchResults = searchMusicByName(query)
                                    }
                                }
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 搜索结果列表
                        if (searchResults.isNotEmpty()) {
                            LazyColumn {
                                items(searchResults) { song ->
                                    MusicItemView(songInfo = song){
                                        scope.launch {
                                            MusicPlayer.playAtNow(song.id)
                                        }
                                    }
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
            }
        }
    }
    companion object{
        fun gotoSearchMusicActivity(context: Context){
            context.startActivity(Intent(context,SearchMusicActivity::class.java))
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    UUMusicTheme {
        Greeting("Android")
    }
}