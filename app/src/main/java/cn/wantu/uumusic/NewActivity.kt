package cn.wantu.uumusic

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.materialIcon
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cn.wantu.uumusic.activity.DefaultActivity
import cn.wantu.uumusic.controller.AppConfig
import cn.wantu.uumusic.controller.WithMusicBar
import cn.wantu.uumusic.model.DiskInfo
import cn.wantu.uumusic.ui.widget.ArtistSection
import cn.wantu.uumusic.ui.widget.NewSongsSection
import cn.wantu.uumusic.ui.widget.PlaylistItem
import cn.wantu.uumusic.ui.widget.SectionTitle
import java.io.File

class NewActivity : DefaultActivity() {

    private val cDiskListFile = File(UUApp.instance.filesDir, "diskList.json")
    private var diskList by mutableStateOf(
        if (cDiskListFile.exists()) UUApp.json.decodeFromString(cDiskListFile.readText())
        else emptyList<DiskInfo>()
    )


    private var dropDownMenuExpanded by mutableStateOf(false)

    @Composable
    override fun SetupUI() {
        val scope = rememberCoroutineScope()
        WithMusicBar(topBar = topBar()) { _ ->
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    val image = materialIcon(name = "Linux Do") {
                        path(
                            stroke = SolidColor(Color.Black),
                            strokeLineWidth = 2f
                        ) {
                            //在这里具体绘制
                            //从右上角开始画

                            moveTo(19f, 2f)
                            horizontalLineToRelative(-18f)
                            verticalLineToRelative(18f)
//画到左下角结束

                        }
                    }
                    Icon(image, "LinuxDo")
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }

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
                                    this@NewActivity,
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

    override fun doBeforeUI() {
        val url = "https://um.wty5.com"
        val chooserIntent =
            Intent.createChooser(Intent(Intent.ACTION_VIEW, Uri.parse(url)), "请选择浏览器");
        startActivity(chooserIntent)
    }


    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    private fun topBar() = @Composable {
        /*TopAppBar(
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
        )*/
    }

    @Composable
    private fun DropdownMenu() {
        androidx.compose.material3.DropdownMenu(
            expanded = dropDownMenuExpanded,
            onDismissRequest = { dropDownMenuExpanded = false },
            modifier = Modifier.wrapContentWidth()
        ) {
            if (AppConfig.isLogin) {
                DropdownMenuItem(onClick = {
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
                SettingsActivity.gotoSettingsActivity(this@NewActivity)
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
}