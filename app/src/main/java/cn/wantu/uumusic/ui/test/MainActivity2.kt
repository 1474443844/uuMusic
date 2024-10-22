package cn.wantu.uumusic.ui.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import cn.wantu.uumusic.ui.test.ui.theme.UUMusicTheme

class MainActivity2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UUMusicTheme {
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview3() {
    UUMusicTheme {

    }
}