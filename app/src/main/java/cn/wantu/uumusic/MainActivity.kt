package cn.wantu.uumusic

import android.content.ComponentName
import android.widget.Toast
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import cn.wantu.uumusic.activity.DefaultActivity
import cn.wantu.uumusic.controller.SettingConfig
import cn.wantu.uumusic.controller.SettingConfig.downloadDir
import cn.wantu.uumusic.controller.WithMusicBar
import cn.wantu.uumusic.controller.generateMediaInfo
import cn.wantu.uumusic.controller.getQQInfo
import cn.wantu.uumusic.controller.getRecommendSong
import cn.wantu.uumusic.model.DiskInfo
import cn.wantu.uumusic.service.PlayerService
import cn.wantu.uumusic.ui.widget.ArtistSection
import cn.wantu.uumusic.ui.widget.BannerSection
import cn.wantu.uumusic.ui.widget.NewSongsSection
import cn.wantu.uumusic.ui.widget.PlaylistItem
import cn.wantu.uumusic.ui.widget.SectionTitle
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutionException

class MainActivity : DefaultActivity() {

    // Test 试行区
    private var recommendCover by mutableStateOf("")
    private var recommendTitle by mutableStateOf("")
    private var recommendId by mutableLongStateOf(0L)

    // Test

    private val userInfoEditor =
        UUApp.instance.getSharedPreferences("UserInfo", MODE_PRIVATE)


    private val diskListCacheFile = File(UUApp.instance.filesDir, "diskList.json")

    private var dropDownMenuExpanded by mutableStateOf(false)
    private var showEditQQDialog by mutableStateOf(false)

    private var qq by mutableStateOf(userInfoEditor.getString("qq", ""))
    private var username by mutableStateOf(
        userInfoEditor.getString(
            "username",
            UUApp.instance.getString(R.string.not_login)
        )
    )
    private var avatar by mutableStateOf(userInfoEditor.getString("avatar", ""))
    private var isLogin by mutableStateOf(qq != "")
    private var diskList by mutableStateOf(
        if (diskListCacheFile.exists()) UUApp.json.decodeFromString(diskListCacheFile.readText())
        else emptyList<DiskInfo>()
    )

    @Composable
    override fun SetupUI() = MainLayout()

    override fun doBeforeUI() {
        lifecycleScope.launch {
            getRecommendSong { title, cover, id ->
                recommendTitle = title
                recommendCover = cover
                recommendId = id
            }
        }
        if (isLogin) { // 有账号的信息
            lifecycleScope.launch {
                login(qq!!) // 更新信息
            }
        }
    }

    private suspend fun login(qq: String) = withContext(Dispatchers.IO) {
        try {
            val qqInfo =
                getQQInfo(qq = qq)
            username = qqInfo.userInfo.name
            avatar = qqInfo.userInfo.pic
            diskList = listOf(qqInfo.likesong) + qqInfo.mydiss + qqInfo.likediss
            FileOutputStream(diskListCacheFile).use { output ->
                output.write(
                    UUApp.json.encodeToString(
                        diskList
                    ).toByteArray()
                )
            }
            userInfoEditor.edit()
                .putString("username", username)
                .putString("avatar", avatar)
                .apply()
        } catch (e: Exception) {
            try {
                val jsonObj = JSONObject(e.message!!)
                if (jsonObj.getInt("code") == 500) {
                    if (avatar == "") {
                        logout()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(UUApp.context, "登录失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            e.printStackTrace()
        }
    }

    private fun logout() {
        qq = ""
        username = getString(R.string.not_login)
        avatar = ""
        isLogin = false
        userInfoEditor.edit().clear().apply()
        diskList = emptyList()
        diskListCacheFile.delete()
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    @Composable
    private fun MainLayout() {
        val scope = rememberCoroutineScope()
        WithMusicBar(topBar = topBar()) { _ ->
            EditQQDialog {
                showEditQQDialog = false
                qq = it
                if (qq != "") {
                    isLogin = true
                    userInfoEditor.edit().putString("qq", it).apply()
                    scope.launch {
                        login(qq!!)
                    }
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item { Spacer(modifier = Modifier.height(16.dp)) }
                item {
                    BannerSection(recommendCover, recommendTitle, modifier = Modifier.clickable {
                        val sessionToken = SessionToken(
                            this@MainActivity, ComponentName(
                                this@MainActivity,
                                PlayerService::class.java
                            )
                        )
                        var mediaController: MediaController? = null

                        var mediaControllerFuture =
                            MediaController.Builder(this@MainActivity, sessionToken).buildAsync()
                        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                        mediaControllerFuture.addListener({
                            try {
                                mediaController = mediaControllerFuture.get()
                                val mediaInfo = generateMediaInfo(
                                    File(
                                        downloadDir,
                                        "info/${SettingConfig.quality}/214003654.json"
                                    )
                                )
                                val mediaMetadata = MediaMetadata.Builder()
                                    .setTitle(mediaInfo.song)
                                    .setArtist(mediaInfo.singer)
                                    .setAlbumTitle(mediaInfo.album)
                                    .build()

                                mediaController?.setMediaItem(
                                    MediaItem.Builder()
                                        .setMediaId("214003654")
                                        .setUri(mediaInfo.url)
                                        .setMediaMetadata(mediaMetadata)
                                        .build()
                                )  // 或者使用 addMediaItem 添加多个
                                mediaController?.prepare()
                                mediaController?.play()
                            } catch (e: ExecutionException) {
                                // 捕获异步任务的异常
                                println("绑定失败: ${e.cause?.message}")
//                                showToast("无法连接到播放服务")
                            } catch (e: InterruptedException) {
                                Thread.currentThread().interrupt()
                            }

                        }, ContextCompat.getMainExecutor(UUApp.instance))
                        if (recommendId != 0L) {
                            scope.launch {
//                                MusicPlayerController.getInstance().playAtNow(recommendId)
                            }
                        }
                    })
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionTitle(title = stringResource(if (diskList.isNotEmpty()) R.string.my_song_list else R.string.empty_song_list))
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
                    Spacer(modifier = Modifier.height(4.dp))
                    ArtistSection()
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionTitle(title = "新歌速递")
                    Spacer(modifier = Modifier.height(4.dp))
                    NewSongsSection()
                }
            }
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
                            .data(if (isLogin) avatar else R.drawable.xxz_rabbit)
                            .memoryCachePolicy(CachePolicy.DISABLED)
                            .diskCachePolicy(CachePolicy.DISABLED)
                            .build(),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(48.dp)
                            .padding(horizontal = 8.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                dropDownMenuExpanded = true
                            }
                    )
                    Text(text = username!!, modifier = Modifier, fontSize = 18.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = { SearchMusicActivity.gotoSearchMusicActivity(this@MainActivity) },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon"
                        )
                    }
                }
                DropdownMenu()
            },
        )
    }

    @Composable
    private fun DropdownMenu() {
        DropdownMenu(
            expanded = dropDownMenuExpanded,
            onDismissRequest = { dropDownMenuExpanded = false },
            modifier = Modifier.wrapContentWidth()
        ) {
            if (isLogin) {
                DropdownMenuItem(onClick = {
                    logout()
                    dropDownMenuExpanded = false
                }, text = {
                    Row {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = stringResource(R.string.logout),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(text = stringResource(R.string.logout))
                    }
                })
            } else {
                DropdownMenuItem(onClick = {
                    showEditQQDialog = true
                    dropDownMenuExpanded = false
                }, text = {
                    Row {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = stringResource(R.string.login),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(text = stringResource(R.string.login))
                    }
                })
            }
            DropdownMenuItem(onClick = {
                dropDownMenuExpanded = false
                SettingsActivity.gotoSettingsActivity(this@MainActivity)
            }, text = {
                Row {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = stringResource(R.string.settings),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(text = stringResource(R.string.settings))
                }
            })
        }
    }

    @Composable
    fun EditQQDialog(onConfirm: (String) -> Unit) {
        if (showEditQQDialog) {
            var text by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = {},
                title = { Text(text = stringResource(R.string.edit_qq_title), fontSize = 18.sp) },
                text = {
                    Column {
                        Text(text = stringResource(R.string.edit_qq_content))
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = text,
                            onValueChange = { newText -> text = newText.filter { it.isDigit() } },
                            label = {
                                Text(
                                    text = stringResource(R.string.edit_qq_hint),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            },
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
                        Text(stringResource(R.string.edit_qq_confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditQQDialog = false }) {
                        Text(stringResource(R.string.edit_qq_dismiss))
                    }
                }
            )
        }
    }

}
