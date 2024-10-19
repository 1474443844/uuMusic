package cn.wantu.uumusic.ui.widget

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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


@Preview
@Composable
fun SearchBarWithCrossfade() {
    var isExpanded by remember { mutableStateOf(false) }

    Crossfade(targetState = isExpanded, label = "你好") { expanded ->
        if (expanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { isExpanded = false }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back Icon"
                    )
                }
                TextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    placeholder = { Text("搜索...") }
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "应用标题",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall
                )
                IconButton(onClick = { isExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon"
                    )
                }
            }
        }
    }
}

@Composable
fun DropdownMenuExample() {
    // 控制 DropdownMenu 可见性的状态
    var expanded by remember { mutableStateOf(false) }

    // 保存选中选项的状态
    var selectedOption by remember { mutableStateOf("选择一个选项") }

    // DropdownMenu 的选项列表
    val options = listOf("选项 1", "选项 2", "选项 3", "选项 4")

    // 使用 Column 纵向排列元素
    Column(modifier = Modifier.padding(16.dp)) {
        // 触发 DropdownMenu 的按钮
        Button(onClick = { expanded = true }) {
            Text(text = selectedOption)
        }

        // DropdownMenu 组件
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }, // 当点击菜单外部时关闭菜单
            modifier = Modifier.fillMaxWidth() // 可选：根据需要调整宽度
        ) {
            options.forEach { option ->
                DropdownMenuItem(text = {
                    Text(text = option)
                }, onClick = {
                    selectedOption = option
                    expanded = false // 选择后关闭菜单
                })
            }
        }
    }
}

@Composable
fun CustomizedDropdownMenu() {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("选择一个选项") }
    val options = listOf("主页", "个人资料", "设置", "登出")

    Column(modifier = Modifier.padding(16.dp)) {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors()
        ) {
            Text(text = selectedOption, color = Color.White)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .wrapContentWidth()
                .background(Color.White)
        ) {
            options.forEach { option ->
                DropdownMenuItem(onClick = {
                    selectedOption = option
                    expanded = false
                }, text = {
                    Row {
                        when (option) {
                            "主页" -> Icon(
                                Icons.Default.Home,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )

                            "个人资料" -> Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )

                            "设置" -> Icon(
                                Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )

                            "登出" -> Icon(
                                Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                        Text(text = option)
                    }
                })

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DropdownMenuPreview() {
    CustomizedDropdownMenu()
}
