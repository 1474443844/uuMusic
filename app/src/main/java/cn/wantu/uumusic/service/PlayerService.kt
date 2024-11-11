package cn.wantu.uumusic.service

import android.content.Intent
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import cn.wantu.uumusic.model.MusicPlayerController

class PlayerService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    // Create your player and media session in the onCreate lifecycle event
    override fun onCreate() {
        super.onCreate()
        mediaSession =
            MediaSession.Builder(this, MusicPlayerController.getInstance().player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        /*TODO("Not yet implemented")*/
        return mediaSession
    }

    // The user dismissed the app from the recent tasks
    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player!!
        if (!player.playWhenReady
            || player.mediaItemCount == 0
            || player.playbackState == Player.STATE_ENDED
        ) {
            // Stop the service if not playing, continue playing in the background
            // otherwise.
            stopSelf()
        }
    }

    // Remember to release the player and media session in onDestroy
    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}