package cn.wantu.uumusic.ui.test

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import cn.wantu.uumusic.ui.theme.UUMusicTheme

@Preview(showBackground = true)
@Composable
private fun Progress() {
    UUMusicTheme {
        CircularProgressIndicator()
//        CircularProgressIndicator(
//            progress = { 0.8f },
//            trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
//        )
    }

}