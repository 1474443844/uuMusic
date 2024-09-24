package cn.wantu.uumusic

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import cn.wantu.uumusic.data.SongInfo
import cn.wantu.uumusic.model.getDiskDetail
import cn.wantu.uumusic.ui.theme.UUMusicTheme
import cn.wantu.uumusic.ui.widget.MusicControllerBar
import cn.wantu.uumusic.ui.widget.SongItem
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import org.json.JSONObject

class DiskDisplayActivity : ComponentActivity() {

    private var mediaPlayer = UUApp.getMediaPlayer()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val diskName by mutableStateOf(intent.getStringExtra("name"))
        val diskCover by mutableStateOf(intent.getStringExtra("cover"))
        var songList by mutableStateOf<List<SongInfo>>(emptyList())
        var showErroInfo by mutableStateOf(false)
        var erroInfo by mutableStateOf("")
        enableEdgeToEdge()
        setContent {
            UUMusicTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { finish() }) {
                                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                    }
                                    Text(diskName!!, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                        })
                    },
                    bottomBar = { MusicControllerBar() }
                ) { paddingValues ->
                    LazyColumn(
                        contentPadding = paddingValues,
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
                        if(songList.isNotEmpty()) {
                            items(songList) { song ->
                                SongItem(
                                    song,
                                    modifier = Modifier.fillMaxWidth().padding(10.dp).clickable {
                                        mediaPlayer.playAtNow(song)
                                    })
                                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                            }
                        } else {
                            if (showErroInfo){
                                item {
                                    Text(text = erroInfo, modifier = Modifier.padding(16.dp))
                                }
                            }

                        }
                    }
                }
            }
        }
        val id = intent.getLongExtra("id", 0)
        lifecycleScope.launch {
            try {
                val diskDetail = getDiskDetail(id)
//                diskName = diskDetail.title
//                diskCover = diskDetail.picurl
                songList = diskDetail.songs
            }catch (e:Exception){
                println(e.message)
                val error = JSONObject(e.message!!)
                if(error.getInt("code") == 600){
                    showErroInfo = true
                    erroInfo = error.getString("message")
                }
            }
        }
    }
    companion object{
        fun showDiskDetails(context: Context, id:Long, name:String, cover: String){
            val intent = Intent(context, DiskDisplayActivity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("name", name)
            intent.putExtra("cover", cover)
            context.startActivity(intent)
        }
    }

}