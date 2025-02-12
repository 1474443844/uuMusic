package cn.wantu.uumusic.Icons


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun LinuxDoIcon(modifier: Modifier = Modifier) {
    Box(modifier = modifier.LinuxDoIcon())
}

fun Modifier.LinuxDoIcon() = this.drawWithContent {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val circleRadius = centerX * 5 / 6
    val clipRadius = circleRadius * 47 / 50
    drawCircle(
        color = Color(0xFfF0F0F0),
        radius = circleRadius,
        center = Offset(centerX, centerY)
    )
    val clipPath = Path().apply {
        addOval(
            oval = Rect(
                centerX - clipRadius,
                centerY - clipRadius,
                centerX + clipRadius,
                centerY + clipRadius
            )
        )
    }

    clipPath(clipPath) {
        drawRect(
            color = Color(0xFF1C1C1E),
            topLeft = Offset(centerX - circleRadius, centerY - circleRadius),
            size = Size(2 * circleRadius, circleRadius * 3 / 5)
        )

        drawRect(
            color = Color(0xFFF0F0F0),
            topLeft = Offset(centerX - circleRadius, centerY - circleRadius + circleRadius * 3 / 5),
            size = Size(2 * circleRadius, circleRadius * 4 / 5)
        )

        drawRect(
            color = Color(0xFFFFB003),
            topLeft = Offset(centerX - circleRadius, centerY - circleRadius + circleRadius * 7 / 5),
            size = Size(2 * circleRadius, circleRadius * 3 / 5)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LinuxDoIconPreview() {
    LinuxDoIcon(Modifier.size(120.dp))
}