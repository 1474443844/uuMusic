package cn.wantu.uumusic

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import cn.wantu.uumusic.data.DiskInfo
import cn.wantu.uumusic.model.getQQInfo
import cn.wantu.uumusic.ui.theme.UUMusicTheme
import cn.wantu.uumusic.ui.widget.ArtistSection
import cn.wantu.uumusic.ui.widget.BannerSection
import cn.wantu.uumusic.ui.widget.NewMusicControllerBar
import cn.wantu.uumusic.ui.widget.NewSongsSection
import cn.wantu.uumusic.ui.widget.PlaylistItem
import cn.wantu.uumusic.ui.widget.SectionTitle
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {

    private val json = Json { ignoreUnknownKeys = true }
    private val sharedPreferences =
        UUApp.getInstance().getSharedPreferences("UserInfo", MODE_PRIVATE)
    private val file = File(UUApp.getInstance().filesDir, "diskList.json")

    private var isExpanded by mutableStateOf(false)
    private var qq by mutableStateOf(sharedPreferences.getString("qq", ""))
    private var username by mutableStateOf(sharedPreferences.getString("username", "暂未登录"))
    private var avatar by mutableStateOf(sharedPreferences.getString("avatar", ""))
    private var isLogin by mutableStateOf(qq != "")
    private var showDialog by mutableStateOf(!isLogin)
    private var diskList by mutableStateOf(
        if (file.exists()) json.decodeFromString(file.readText())
        else listOf(DiskInfo(id = 0, title = "暂无", picurl = ""))
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isLogin) {
            login(qq!!)
        }
        enableEdgeToEdge()
        setContent {
            UUMusicTheme {
                MainLayout()
            }
        }
//        lifecycleScope.launch {
//            // 在这里编写协程代码
//            searchMusicByName("杀青")
//        }
    }

    @Composable
    private fun MainLayout() {
        Scaffold(
            topBar = topBar(),
            bottomBar = {
                NewMusicControllerBar(isExpanded, onExpand = { isExpanded = !isExpanded })
            },
        ) { paddingValues ->
            EditQQDialog(showDialog = showDialog,
                initialText = qq!!,
                onConfirm = {
                    showDialog = false
                    qq = it
                    if (qq != "") {
                        isLogin = true
                        sharedPreferences.edit().putString("qq", it).apply()
                        login(qq!!)
                    } else {
                        logout()
                    }
                }) {
                showDialog = false
            }
            LazyColumn(
                contentPadding = paddingValues,
                modifier = Modifier.fillMaxSize()
            ) {
                item { Spacer(modifier = Modifier.height(16.dp)) }
                item { BannerSection() }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionTitle(title = "我的歌单")
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(diskList) { disk ->
                            PlaylistItem(disk, modifier = Modifier.clickable {
                                if (disk.id == 0L) return@clickable
                                DiskDisplayActivity.showDiskDetails(
                                    this@MainActivity,
                                    disk.id, disk.title, disk.picurl
                                )
                            })
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionTitle(title = "热门歌手")
                    ArtistSection()
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionTitle(title = "新歌速递")
                    NewSongsSection()
                }
            }
        }
    }

    private fun login(qq: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val qqInfo =
                    getQQInfo(qq = qq)
                username = qqInfo.userInfo.name
                avatar = qqInfo.userInfo.pic
                diskList =
                    listOf(qqInfo.likesong) + qqInfo.mydiss + qqInfo.likediss
                FileOutputStream(file).use { output ->
                    output.write(
                        json.encodeToString(
                            diskList
                        ).toByteArray()
                    )
                }
                sharedPreferences.edit()
                    .putString("username", username)
                    .putString("avatar", avatar)
                    .apply()
            } catch (e: Exception) {
                isLogin = (avatar != "")
                e.printStackTrace()
            }
        }
    }

    private fun logout() {
        qq = ""
        username = "暂未登录"
        avatar = ""
        isLogin = false
        sharedPreferences.edit().clear().apply()
        diskList = listOf(DiskInfo(id = 0, title = "暂无", picurl = ""))
        file.delete()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && isExpanded) {
            isExpanded = false
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    @Composable
    fun EditQQDialog(
        showDialog: Boolean,
        initialText: String,
        onConfirm: (String) -> Unit,
        onNegative: () -> Unit
    ) {
        if (showDialog) {
            var text by remember { mutableStateOf(initialText) }
            AlertDialog(
                onDismissRequest = {},
                title = { Text(text = getString(R.string.edit_qq_title), fontSize = 18.sp) },
                text = {
                    Column {
                        Text(text = getString(R.string.edit_qq_content))
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = text,
                            onValueChange = { newText -> text = newText.filter { it.isDigit() } },
                            label = { Text(text = getString(R.string.edit_qq_hint)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { onConfirm(text) }
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { onConfirm(text) }) {
                        Text(getString(R.string.edit_qq_confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onNegative() }) {
                        Text(getString(R.string.edit_qq_dismiss))
                    }
                }
            )
        }
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    private fun topBar() = @Composable {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(if (isLogin) avatar else R.mipmap.ic_launcher)
                            .memoryCachePolicy(CachePolicy.DISABLED)
                            .diskCachePolicy(CachePolicy.DISABLED)
                            .build(),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(48.dp)
                            .padding(horizontal = 8.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null) {

                                showDialog = true
                            }
                    )
                    Text(text = username!!)
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { isExpanded = true }, modifier = Modifier.padding(end = 8.dp)) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon"
                        )
                    }
                }
            },)
    }
}
