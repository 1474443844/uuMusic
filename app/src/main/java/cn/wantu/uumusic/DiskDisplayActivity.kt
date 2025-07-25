package cn.wantu.uumusic

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import cn.wantu.uumusic.activity.DefaultActivity
import cn.wantu.uumusic.controller.WithMusicBar
import cn.wantu.uumusic.controller.getDiskDetail
import cn.wantu.uumusic.model.SongInfo
import cn.wantu.uumusic.ui.widget.SongItem
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import org.json.JSONObject

class DiskDisplayActivity : DefaultActivity() {
    private var songSize = -1
    private var page = 1
    private var id = 0L
    private val songList = emptyList<SongInfo>().toMutableStateList()
    private var showErrorInfo by mutableStateOf(false)
    private var errorInfo by mutableStateOf("")


    private lateinit var diskName: String
    private lateinit var diskCover: String

    override fun doBeforeUI() {
        diskName = intent.getStringExtra("name")!!
        diskCover = intent.getStringExtra("cover")!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        id = intent.getLongExtra("id", 0)
        lifecycleScope.launch {
            try {
                val diskDetail = getDiskDetail(id, page)
                songSize = diskDetail.songnum
                songList.addAll(diskDetail.songs)
                page++
            } catch (e: Exception) {
                println(e.message)
                val error = JSONObject(e.message!!)
                if (error.getInt("code") == 600) {
                    showErrorInfo = true
                    errorInfo = error.getString("message")
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun SetupUI() {
        val scope = rememberCoroutineScope()
        val listState = rememberLazyListState()
        var isLoading by remember { mutableStateOf(false) }

        LaunchedEffect(listState) {
            snapshotFlow { listState.firstVisibleItemIndex + listState.layoutInfo.visibleItemsInfo.size }
                .collect { visibleItemCount ->
                    val totalItemCount = listState.layoutInfo.totalItemsCount
                    if (totalItemCount < songSize) {
                        if (visibleItemCount >= totalItemCount - 5 && !isLoading) {
                            isLoading = true
                            val diskDetail = getDiskDetail(id, page)
                            songList.addAll(diskDetail.songs)
                            page++
                            isLoading = false
                        }
                    }
                }
        }

        WithMusicBar(topBar = {
            TopAppBar(title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { finish() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Text(diskName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            })
        }
        ) { player ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                item { Spacer(modifier = Modifier.height(16.dp)) }
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = diskCover,
                            contentDescription = "Cover",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                if (songList.isNotEmpty()) {
                    items(songList) { song ->
                        SongItem(
                            song,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                                .clickable {
                                    scope.launch {
                                        player.playAtNow(song)
                                    }
                                }) {
                            scope.launch {
                                player.playAtNext(song)
                            }
                        }
                        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                    }
                } else {
                    if (showErrorInfo) {
                        item {
                            Text(text = errorInfo, modifier = Modifier.padding(16.dp))
                        }
                    }

                }
            }
        }
    }

    companion object {
        fun showDiskDetails(context: Context, id: Long, name: String, cover: String) {
            val intent = Intent(context, DiskDisplayActivity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("name", name)
            intent.putExtra("cover", cover)
            context.startActivity(intent)
        }
    }

}