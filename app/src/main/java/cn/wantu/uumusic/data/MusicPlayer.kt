package cn.wantu.uumusic.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

var currentSongIndex by mutableIntStateOf(0)
var currentSongList by mutableStateOf(emptyList<SongInfo>())
var currentPlayingState by mutableStateOf(false)
