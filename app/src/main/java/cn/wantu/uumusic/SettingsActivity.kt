package cn.wantu.uumusic

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.wantu.uumusic.activity.DefaultActivity
import cn.wantu.uumusic.ui.theme.UUMusicTheme


class SettingsActivity : DefaultActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @Composable
    override fun SetupUI() = SettingsScreen()

    override fun doBeforeUI() {}

    companion object {
        fun gotoSettingsActivity(context: Context) {
            context.startActivity(Intent(context, SearchMusicActivity::class.java))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var syncEmail by remember { mutableStateOf(true) }
    var downloadAttachments by remember { mutableStateOf(true) }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("SettingsActivity") },
                navigationIcon = {
                    IconButton(onClick = { (context as ComponentActivity).finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {

            SettingGroup("Messages") {
                SettingItem("Your signature", "未设置")
                SettingItem("Default reply action", "Reply")
            }

            SettingGroup("Sync") {
                SettingSwitchItem("Sync email periodically", syncEmail) {
                    syncEmail = it
                }
                SettingSwitchItem(
                    "Download incoming attach..",
                    downloadAttachments,
                    subtitle = "Automatically download\nattachments for incoming emails"
                ) { downloadAttachments = it }
            }


        }
    }
}

@Composable
fun SettingGroup(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 18.sp
        )
        content()
    }
}


@Composable
fun SettingItem(title: String, subtitle: String, onClick: (() -> Unit)? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick ?: {})
            .padding(vertical = 8.dp)

    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title)
            if (subtitle.isNotEmpty()) {
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun SettingSwitchItem(title: String, checked: Boolean, subtitle: String = "", onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp)

    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title)
            if (subtitle.isNotEmpty()) {
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview2() {
    SettingsScreen()
}


@Preview(showBackground = true)
@Composable
private fun Settings () {
    UUMusicTheme {
        SettingsScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreens() {
    val notificationsEnabled = remember { mutableStateOf(true) }
    val darkModeEnabled = remember { mutableStateOf(false) }
    val username = remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Text(text = "Settings", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        // Notifications toggle
        SettingRow(label = "Enable Notifications") {
            Checkbox(
                checked = notificationsEnabled.value,
                onCheckedChange = { notificationsEnabled.value = it }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Dark mode toggle
        SettingRow(label = "Enable Dark Mode") {
            Checkbox(
                checked = darkModeEnabled.value,
                onCheckedChange = { darkModeEnabled.value = it }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Username input
        Text(text = "Username")
        // Here you can replace this with a TextField if needed
        SettingRow(label = "Username") {
            Button(onClick = { username.value = "User_${(1..100).random()}" }) {
                Text(text = "Generate Username")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save button
        Button(onClick = {
            // Save settings logic here
            println("Settings saved")
        }) {
            Text(text = "Save Settings")
        }
    }
}

@Composable
fun SettingRow(label: String, content: @Composable () -> Unit) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = label)
        content()
    }
}