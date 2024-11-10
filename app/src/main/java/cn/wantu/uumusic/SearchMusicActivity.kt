package cn.wantu.uumusic

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import cn.wantu.uumusic.activity.DefaultActivity
import cn.wantu.uumusic.data.SongInfo
import cn.wantu.uumusic.model.WithMusicBar
import cn.wantu.uumusic.model.searchMusicByName
import cn.wantu.uumusic.ui.widget.MusicItemView
import kotlinx.coroutines.launch

class SearchMusicActivity : DefaultActivity() {
    private var page = 1

    @Composable
    override fun SetupUI() {
        val listState = rememberLazyListState()
        val scope = rememberCoroutineScope()
        var isLoading by remember { mutableStateOf(false) }
        var query by remember { mutableStateOf("") }
        val searchResults = remember { mutableStateListOf<SongInfo>() }

        LaunchedEffect(listState) {
            snapshotFlow { listState.firstVisibleItemIndex + listState.layoutInfo.visibleItemsInfo.size }
                .collect { visibleItemCount ->
                    val totalItemCount = listState.layoutInfo.totalItemsCount
                    if(query != "") {
                        if (visibleItemCount >= totalItemCount - 5 && !isLoading) {
                            isLoading = true
                            val songs = searchMusicByName(query, page)
                            searchResults.addAll(songs)
                            page++
                            isLoading = false
                            println("page: $page")
                        }
                    }
                }
        }
        WithMusicBar { player ->

            Column(
                modifier = Modifier
                    .systemBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, bottom = 0.dp)
                    .fillMaxSize()
            ) {
                // 搜索栏
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("搜索音乐") },
                    modifier = Modifier
                        .fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = "搜索图标"
                        )
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            // 执行搜索操作
                            scope.launch {
                                searchResults.addAll(searchMusicByName(query))
                                page = 2
                            }
                        }
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 搜索结果列表
                if (searchResults.isNotEmpty()) {
                    LazyColumn(state = listState) {
                        items(searchResults) { song ->
                            MusicItemView(songInfo = song) {
                                scope.launch {
                                    player.playAtNow(song)
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

    override fun doBeforeUI() {

    }

    companion object {
        fun gotoSearchMusicActivity(context: Context) {
            context.startActivity(Intent(context, SearchMusicActivity::class.java))
        }
    }
}
