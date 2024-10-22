package cn.wantu.uumusic.ui.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.wantu.uumusic.ui.widget.LyricLine
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity2 : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val sampleLyrics = mutableListOf<LyricLine>()
            for (i in 0..100){
                sampleLyrics.add(LyricLine((i*5).toLong(), "第 $i 行歌词"))
            }
            var currentTime by remember { mutableIntStateOf(0) }
            val coroutineScope = rememberCoroutineScope()

            // 模拟播放时间更新
            LaunchedEffect(Unit) {
                coroutineScope.launch {
                    while (true) {
                        delay(1000)
                        currentTime += 1
                    }
                }
            }

            LyricsScreen(lyrics = sampleLyrics, currentTime = currentTime)
        }
    }
}


@Composable
fun LyricsScreen(lyrics: List<LyricLine>, currentTime: Int) {
    val currentIndex = lyrics.indexOfLast { it.time <= currentTime }
    val listState = rememberLazyListState()
    var isUserScrolling by remember { mutableStateOf(false) }

    // 自动滚动到当前歌词
    LaunchedEffect(currentIndex) {
        if (!isUserScrolling) {
            listState.animateScrollToItem(index = currentIndex)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isUserScrolling = true },
                    onDragEnd = { isUserScrolling = false },
                    onDrag = {_, _ ->

                    }
                )
            }
    ) {
        itemsIndexed(lyrics) { index, line ->
            val color by animateColorAsState(
                targetValue = if (index == currentIndex) Color.Yellow else Color.White, label = ""
            )
            val fontWeight = if (index == currentIndex) FontWeight.Bold else FontWeight.Normal

            Text(
                text = line.text,
                color = color,
                fontSize = 18.sp,
                fontWeight = fontWeight,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}
