package cn.wantu.uumusic.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import cn.wantu.uumusic.ui.theme.UUMusicTheme

abstract class DefaultActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        doBeforeUI()
        enableEdgeToEdge()
        setContent {
            UUMusicTheme {
                SetupUI()
            }
        }
    }
    @Composable
    protected abstract fun SetupUI()
    protected abstract fun doBeforeUI()
}