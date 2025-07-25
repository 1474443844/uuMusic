package cn.wantu.uumusic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.wantu.uumusic.activity.DefaultActivity

@Composable
fun MusicPlayerScreen() {
    // Dummy data for now; replace with your actual data source
    val albumArtResId = R.drawable.xxz_rabbit // Replace with your image resource
    val songTitle = "Safaera"
    val artistName = "Bad Bunny, Jowell & Randy, and Ñengo Flow"
    val isPlaying = remember { mutableStateOf(true) }
    val isFavorite = remember { mutableStateOf(false) }
    val currentProgress = remember { mutableFloatStateOf(0.3f) } // 0.0 to 1.0

    // State to control the visibility of controls
    val controlsVisible = remember { mutableStateOf(true) }

    // Use a Box to layer elements
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C2732)) // Dark background
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        controlsVisible.value = !controlsVisible.value
                    }
                )
            }

    ) {
        // Album Art (Circular)
        Image(
            painter = painterResource(id = albumArtResId),
            contentDescription = "Album Art",
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.Center)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        // Song Title and Artist (Visible above album art)
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 320.dp), // Position below the album art
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = songTitle,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = artistName,
                color = Color.Gray,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }


        // Controls (Animated Visibility)
        AnimatedVisibility(
            visible = controlsVisible.value,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        )
        {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isFavorite.value) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = Color.White,
                        modifier = Modifier.clickable { isFavorite.value = !isFavorite.value }
                    )
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share",
                        tint = Color.White
                    )
                }


                Spacer(modifier = Modifier.weight(1f))
                // Playback Controls (Previous, Play/Pause, Next)
                PlaybackControls(
                    isPlaying = isPlaying.value,
                    onPlayPauseClick = { isPlaying.value = !isPlaying.value },
                    onPreviousClick = { /*TODO: Handle previous track*/ },
                    onNextClick = { /*TODO: Handle next track*/ }
                )

                // Progress Bar and Timestamps
                ProgressArc(
                    progress = currentProgress.floatValue,
                    onProgressChange = { currentProgress.floatValue = it },
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
                )

            }
        }

    }
}


@Composable
fun PlaybackControls(
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "Previous",
            tint = Color.White,
            modifier = Modifier
                .size(40.dp)
                .clickable { onPreviousClick() }
        )
        Icon(
            imageVector = if (isPlaying) Icons.Default.ArrowDropDown else Icons.Filled.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = Color.White,
            modifier = Modifier
                .size(60.dp)
                .clickable { onPlayPauseClick() }
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Next",
            tint = Color.White,
            modifier = Modifier
                .size(40.dp)
                .clickable { onNextClick() }
        )
    }
}


@Composable
fun ProgressArc(
    progress: Float,
    onProgressChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = with(density) { configuration.screenWidthDp.dp.roundToPx() }
    val radius = (screenWidth * 0.35f)  // Adjust for desired radius

    val arcColor = Color(0xFF29B6F6) // Light blue
    val trackColor = Color.Gray.copy(alpha = 0.5f)
    val strokeWidth = 12.dp

    val sweepAngle = 360 * progress * 0.5f  // Only half circle (0.5f)
    val startAngle = 180f  // Starting at 180 degrees

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(strokeWidth)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(strokeWidth)
                .align(Alignment.BottomStart)
        ) {
            // Draw the track (full arc)
            drawArc(
                color = trackColor,
                startAngle = startAngle,
                sweepAngle = 180f, //Full arc
                useCenter = false,
                topLeft = Offset((size.width / 2) - radius, -radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            // Draw the progress (partial arc)
            drawArc(
                color = arcColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset((size.width / 2) - radius, -radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }

        // Display timestamps
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "1:50", // Calculate from progress
                color = Color.White,
                modifier = Modifier
            )
            Text(
                text = "-1:50", // Calculate from progress
                color = Color.White,
                modifier = Modifier
            )
        }


        // Handle slider-like dragging on the arc.  This is a SIMPLIFIED version.
        // A production-ready implementation would need more sophisticated gesture handling.
        Slider(
            value = progress,
            onValueChange = onProgressChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(strokeWidth + 30.dp)  // Make tappable area bigger
                .align(Alignment.BottomStart)
                .padding(bottom = 0.dp), //remove space between slider and progress
            colors = SliderDefaults.colors(
                thumbColor = Color.Transparent,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            )

        )
    }
}

// Preview for quick development
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MaterialTheme {
        MusicPlayerScreen()
    }
}

// You'll need to create a dummy drawable resource for the album art (R.drawable.album_art)


class NewActivity : DefaultActivity() {

    @Composable
    override fun SetupUI() {

    }

    override fun doBeforeUI() {
    }

}