package cn.wantu.uumusic.ui.test
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MusicSearchViewModel : ViewModel() {
    var query by mutableStateOf("")
        private set

    var searchResults by mutableStateOf(listOf<String>())
        private set

    fun onQueryChanged(newQuery: String) {
        query = newQuery
        searchMusic(newQuery)
    }

    private fun searchMusic(query: String) {
        viewModelScope.launch {
            // 模拟网络延迟
            delay(500)
            // 模拟搜索结果
            searchResults = if (query.isNotEmpty()) {
                listOf("歌曲A - $query", "歌曲B - $query", "歌曲C - $query")
            } else {
                emptyList()
            }
        }
    }
}

@Composable
fun MusicSearchScreen(viewModel: MusicSearchViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = viewModel.query,
                onValueChange = viewModel::onQueryChanged,
                placeholder = { Text(text = "搜索音乐") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardActions = KeyboardActions(onSearch = { /* 执行搜索操作 */ }),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
            )
        }


        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.searchResults.isNotEmpty()) {
            LazyColumn {
                items(viewModel.searchResults) { song ->
                    Text(text = song, modifier = Modifier.padding(8.dp))
                    HorizontalDivider()
                }
            }
        } else {
            Text(text = "请输入关键词进行搜索", modifier = Modifier.padding(8.dp))
        }
    }
}
@Preview
@Composable
fun Preview(){
    MusicSearchScreen(viewModel = MusicSearchViewModel())

}