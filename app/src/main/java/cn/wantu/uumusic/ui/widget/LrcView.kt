package cn.wantu.uumusic.ui.widget

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

data class LrcMetadata(
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null
)
data class LyricLine(val time: Long, val text: String)
data class LrcFile(
    val metadata: LrcMetadata,
    val lyrics: List<LyricLine>
)
fun parseLrcString(lrcContent: String): LrcFile {
    val lyrics = mutableListOf<LyricLine>()
    val timeRegex = "\\[(\\d{2}):(\\d{2}(?:\\.\\d{1,2})?)]".toRegex()
    val metadataRegex = "\\[(\\w+):(.+)]".toRegex()

    var title: String? = null
    var artist: String? = null
    var album: String? = null

    // 将字符串按行分割
    val lines = lrcContent.lines()

    lines.forEach { line ->
        when {
            metadataRegex.matches(line) -> {
                val matchResult = metadataRegex.find(line)
                val tag = matchResult?.groupValues?.get(1)
                val value = matchResult?.groupValues?.get(2)?.trim()
                when (tag) {
                    "ti" -> title = value
                    "ar" -> artist = value
                    "al" -> album = value
                    // 可以添加更多的元数据处理
                }
            }
            timeRegex.containsMatchIn(line) -> {
                val timeMatches = timeRegex.findAll(line)
                val text = line.replace(timeRegex, "").trim()

                timeMatches.forEach { matchResult ->
                    val minutes = matchResult.groupValues[1].toLongOrNull()
                    val seconds = matchResult.groupValues[2].toDoubleOrNull()
                    if (minutes == null || seconds == null) {
                        // 无效的时间戳，跳过
                        return@forEach
                    }
                    val totalMilliseconds = (minutes * 60 * 1000) + (seconds * 1000).toLong()

                    lyrics.add(LyricLine(time = totalMilliseconds, text = text))
                }
            }
            else -> {
                // 处理其他情况，如空行或无效格式
            }
        }
    }

    return LrcFile(
        metadata = LrcMetadata(title = title, artist = artist, album = album),
        lyrics = lyrics.sortedBy { it.time }
    )
}
fun parseLrcFile(inputStream: InputStream): List<LyricLine> {
    val lyrics = mutableListOf<LyricLine>()
    val reader = BufferedReader(InputStreamReader(inputStream))
    val timeRegex = "\\[(\\d{2}):(\\d{2}\\.\\d{2})]".toRegex()

    reader.useLines { lines ->
        lines.forEach { line ->
            // 跳过空行
            if (line.isBlank()) return@forEach

            // 匹配所有时间戳
            val timeMatches = timeRegex.findAll(line)

            // 提取歌词文本（去除所有时间戳后的剩余部分）
            val text = line.replace(timeRegex, "").trim()

            timeMatches.forEach { matchResult ->
                val minutes = matchResult.groupValues[1].toLongOrNull() ?: 0
                val seconds = matchResult.groupValues[2].toDoubleOrNull() ?: 0.0
                val totalMilliseconds = (minutes * 60 * 1000) + (seconds * 1000).toLong()

                lyrics.add(LyricLine(time = totalMilliseconds, text = text))
            }
        }
    }

    // 按时间排序
    return lyrics.sortedBy { it.time }
}
@Composable
fun LyricsScreen(
    lyrics: List<LyricLine>,
    currentTime: Long,
    metadata: LrcMetadata,
    modifier: Modifier = Modifier
) {
    val currentIndex = lyrics.indexOfLast { it.time <= currentTime }
    val listState = rememberLazyListState()
    var isUserScrolling by remember { mutableStateOf(false) }

    // 自动滚动到当前歌词
    LaunchedEffect(currentIndex) {
        if (!isUserScrolling && currentIndex != -1) {
            listState.animateScrollToItem(index = currentIndex)
        }
    }

    Column(
        modifier = modifier
            .background(Color.Black)
    ) {
        // 显示元数据（标题和艺术家）

        // 显示歌词列表
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { isUserScrolling = true },
                        onDragEnd = { isUserScrolling = false },
                        onDrag = { _, _ ->
                        }
                    )
                }
        ) {
            itemsIndexed(lyrics) { index, line ->
                val color by animateColorAsState(
                    targetValue = if (index == currentIndex) Color.Yellow else Color.White
                )
                val fontWeight = if (index == currentIndex) FontWeight.Bold else FontWeight.Normal

                Text(
                    text = line.text,
                    color = color,
                    fontSize = 18.sp,
                    fontWeight = fontWeight,
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp)
                )
            }
        }
    }
}
