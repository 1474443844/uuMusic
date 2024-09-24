package cn.wantu.uumusic.ui.test

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import cn.wantu.uumusic.ui.theme.UUMusicTheme
import cn.wantu.uumusic.ui.widget.SongItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LazyColumnWithPagination() {
    val listState = rememberLazyListState()
    val items = remember { mutableStateListOf(9,8,7,6,5,4,3,2,1,0) }
    val isLoading = remember { mutableStateOf(false) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex + listState.layoutInfo.visibleItemsInfo.size }
            .collect { visibleItemCount ->
                val totalItemCount = listState.layoutInfo.totalItemsCount
                if (visibleItemCount >= totalItemCount - 5 && !isLoading.value) {
                    isLoading.value = true
                    loadMoreData { newItems ->
                        items.addAll(newItems)
                        isLoading.value = false
                    }
                }
            }
    }

    LazyColumn(state = listState) {
        items(items) { item ->
            // 显示每个列表项
            SongItem()
            Text(text = "Item $item", modifier = Modifier.clickable {
                println(items.size)
            })
        }

        if (isLoading.value) {
            item {
                // 显示加载进度条
            }
        }
    }
}

fun loadMoreData(onDataLoaded: (List<Int>) -> Unit) {
    // 模拟网络请求或数据加载
    CoroutineScope(Dispatchers.IO).launch {
        val newData = fetchDataFromNetwork()
        withContext(Dispatchers.Main) {
            onDataLoaded(newData)
        }
    }
}

fun fetchDataFromNetwork(): List<Int> = listOf(0,1,2,3,4,5,6,7,8,9)

@Preview
@Composable
private fun PreviewLazyColumnWithPagination() {
    UUMusicTheme {
        LazyColumnWithPagination()

    }
}