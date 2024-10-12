package cn.wantu.uumusic.ui.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cn.wantu.uumusic.ui.test.ui.theme.UUMusicTheme

class MainActivity2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UUMusicTheme {
                ExpandUpListScreen()
            }
        }
    }

    @Composable
    fun ExpandUpListScreen() {
        // 用于控制列表的显示与隐藏
        var isExpanded by remember { mutableStateOf(false) }

        // 外层容器，填满整个屏幕，内容在底部对齐
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 使用动画切换列表的可见性
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn() + slideInVertically(
                        // 从按钮下方向上展开
                        initialOffsetY = { it },
                        animationSpec = tween(durationMillis = 300)
                    ),
                    exit = fadeOut() + slideOutVertically(
                        // 向下收回
                        targetOffsetY = { it },
                        animationSpec = tween(durationMillis = 300)
                    )
                ) {
                    // 列表项，可以根据需求自定义
                    Column(
                        modifier = Modifier
                            .background(Color.LightGray, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        listOf("选项 1", "选项 2", "选项 3").forEach { item ->
                            Text(
                                text = item,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { /* 处理点击事件 */ }
                                    .padding(8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 按钮，点击时切换列表的显示状态
                Button(
                    onClick = { isExpanded = !isExpanded }
                ) {
                    Text(text = if (isExpanded) "收起列表" else "展开列表")
                }
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