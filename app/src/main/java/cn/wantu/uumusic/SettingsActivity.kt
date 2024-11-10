package cn.wantu.uumusic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cn.wantu.uumusic.ui.theme.UUMusicTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UUMusicTheme {
                SettingsScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Settings () {
    UUMusicTheme {
        SettingsScreen()
    }
}

@Composable
fun SettingsScreen() {
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