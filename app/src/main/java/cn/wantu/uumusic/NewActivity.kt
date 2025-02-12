package cn.wantu.uumusic

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.wantu.uumusic.activity.DefaultActivity
import cn.wantu.uumusic.model.DiskInfo
import kotlinx.coroutines.delay
import java.io.File
import kotlin.math.roundToInt

class NewActivity : DefaultActivity() {

    private val cDiskListFile = File(UUApp.instance.filesDir, "diskList.json")
    private var diskList by mutableStateOf(
        if (cDiskListFile.exists()) UUApp.json.decodeFromString(cDiskListFile.readText())
        else emptyList<DiskInfo>()
    )


    private var dropDownMenuExpanded by mutableStateOf(false)

    @Composable
    override fun SetupUI() {
        var progress by remember { mutableStateOf(0f) }
        var isPlaying by remember { mutableStateOf(false) }

        MusicPlayerSlider(
            currentPosition = progress,
            totalDuration = 240000f, // 总时长（毫秒）
            onValueChange = { progress = it },
            onValueChangeFinished = { /* 同步播放进度 */ },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A1A))
                .padding(vertical = 24.dp)
        )
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MusicPlayerSlider(
        currentPosition: Float,
        totalDuration: Float,
        onValueChange: (Float) -> Unit,
        onValueChangeFinished: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        var sliderValue by remember { mutableFloatStateOf(currentPosition) }
        var isDragging by remember { mutableStateOf(false) }

        Column(modifier = modifier.padding(horizontal = 24.dp)) {
            // 时间显示
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(currentPosition),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
                Text(
                    text = formatTime(totalDuration),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }

            val nnnnn = remember { MutableInteractionSource() }
            // 自定义Slider
            Slider(
                value = sliderValue,
                onValueChange = {
                    sliderValue = it
                    isDragging = true
                    onValueChange(it)
                },
                onValueChangeFinished = {
                    isDragging = false
                    onValueChangeFinished()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                /*colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent
                ),*/
                thumb = {
                    SliderDefaults.Thumb(
                        interactionSource = nnnnn,
                        thumbSize = DpSize(16.dp, 16.dp),
                        modifier = Modifier
                            .scale(if (isDragging) 1.2f else 1f)
                            .background(Color.White, CircleShape)
                            .size(16.dp)
                    )
                },
                track = { sliderState ->
                    SliderDefaults.Track(
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.Transparent,
                            inactiveTrackColor = Color.Transparent
                        ), enabled = true, sliderState = sliderState
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                    ) {
                        // 进度条背景
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .drawWithContent {
                                    // 绘制渐变效果
                                    drawRect(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFFFF0000),
                                                Color(0xFF00B8FF)
                                            )
                                        ),
                                        size = Size(
                                            size.width * (sliderValue / totalDuration),
                                            size.height
                                        )
                                    )
                                }
                        )

                    }
                }
            )

            // 播放控制按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = { /* 上一首 */ }) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Previous",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = { /* 播放/暂停 */ },
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.White, CircleShape)
                ) {
                    Icon(
                        imageVector = if (false) Icons.Default.Clear else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(onClick = { /* 下一首 */ }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Next",
                        tint = Color.White
                    )
                }
            }
        }
    }

    private fun formatTime(milliseconds: Float): String {
        val totalSeconds = milliseconds / 1000
        val minutes = (totalSeconds / 60).toInt()
        val seconds = (totalSeconds % 60).toInt()
        return String.format("%02d:%02d", minutes, seconds)
    }

    @Composable
    fun MusicPlayerSlider(
        duration: Long,
        currentPosition: Long,
        onPositionChange: (Long) -> Unit,
        isPlaying: Boolean,
        onTogglePlay: () -> Unit
    ) {
        // 将时间转换为分钟:秒的格式
        val formatTime: (Long) -> String = { millis ->
            val minutes = millis / 1000 / 60
            val seconds = millis / 1000 % 60
            String.format("%02d:%02d", minutes, seconds)
        }

        val progress = remember(currentPosition, duration) {
            if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f
        }
        val animatedProgress by animateFloatAsState(targetValue = progress)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 播放/暂停按钮
            IconButton(onClick = onTogglePlay) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Clear else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(48.dp),
                    tint = Color.DarkGray
                )
            }

            // 美化后的进度条
            CustomSlider(
                progress = animatedProgress,
                onProgressChange = { newProgress ->
                    onPositionChange((newProgress * duration).roundToInt().toLong())
                }
            )


            // 时间显示
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(currentPosition),
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatTime(duration),
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    @Composable
    fun CustomSlider(
        progress: Float,
        onProgressChange: (Float) -> Unit
    ) {
        val trackHeight = 8.dp
        val thumbRadius = 10.dp
        val gradientColors = listOf(Color(0xFF6200EE), Color(0xFF3700B3)) // 自定义渐变色

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight + thumbRadius * 2) // 总高度包含滑块和轨道
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        // 点击事件，计算新的进度
                        val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                        onProgressChange(newProgress)
                    }
                }
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.CenterStart)
            ) {
                // 绘制背景轨道
                drawRoundRect(
                    color = Color.LightGray,
                    size = Size(size.width, trackHeight.toPx()),
                    cornerRadius = CornerRadius(trackHeight.toPx() / 2)
                )

                // 绘制已播放部分轨道（渐变）
                drawRoundRect(
                    brush = Brush.horizontalGradient(gradientColors),
                    size = Size(size.width * progress, trackHeight.toPx()),
                    cornerRadius = CornerRadius(trackHeight.toPx() / 2)
                )
                // 绘制滑块的函数
                fun DrawScope.drawThumb(center: Offset) {
                    // 绘制滑块阴影
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.2f),
                        radius = thumbRadius.toPx() + 2.dp.toPx(),
                        center = center
                    )
                    // 绘制滑块主体
                    drawCircle(
                        color = Color.White,
                        radius = thumbRadius.toPx(),
                        center = center
                    )

                    //绘制滑块外圈
                    drawCircle(
                        color = gradientColors[0], // 使用渐变色的第一个颜色
                        radius = thumbRadius.toPx(),
                        center = center,
                        style = Stroke(width = 2.dp.toPx()) // 设置边框宽度
                    )
                }
                // 绘制滑块
                drawThumb(
                    center = Offset(
                        size.width * progress,
                        trackHeight.toPx() / 2 + thumbRadius.toPx()
                    )
                )

            }
        }
    }

    @Composable
    fun MusicPlayerScreen() {
        var currentPosition by remember { mutableLongStateOf(0L) }
        val duration = 180000L // 假设歌曲时长为3分钟（180000毫秒）
        var isPlaying by remember { mutableStateOf(false) }

        // 模拟播放进度 (实际应用中应从播放器获取)
        LaunchedEffect(isPlaying) {
            if (isPlaying) {
                while (currentPosition < duration) {
                    delay(100) // 每100毫秒更新一次
                    currentPosition += 100
                }
                if (currentPosition >= duration) {
                    isPlaying = false
                    currentPosition = 0
                }
            }
        }

        MusicPlayerSlider(
            duration = duration,
            currentPosition = currentPosition,
            onPositionChange = { newPosition -> currentPosition = newPosition },
            isPlaying = isPlaying,
            onTogglePlay = { isPlaying = !isPlaying }
        )
    }

    override fun doBeforeUI() {
        val url = "https://um.wty5.com"
        val chooserIntent =
            Intent.createChooser(Intent(Intent.ACTION_VIEW, Uri.parse(url)), "请选择浏览器");
        // startActivity(chooserIntent)
    }


}