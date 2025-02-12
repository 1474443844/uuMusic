package cn.wantu.uumusic.service

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import cn.wantu.uumusic.MainActivity
import cn.wantu.uumusic.R
import cn.wantu.uumusic.controller.MusicPlayerController

@UnstableApi
class PlayerService : MediaSessionService() {
    private val player = MusicPlayerController.player
    private var mediaSession: MediaSession? = null
    private lateinit var playerNotificationManager: PlayerNotificationManager

    companion object {
        const val NOTIFICATION_ID = 123
        const val CHANNEL_ID = "music_playback_channel"
        const val ACTION_STOP_SERVICE = "com.example.myapp.action.STOP_SERVICE" //自定义action
    }

    // Create your player and media session in the onCreate lifecycle event
    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSession.Builder(this, player)
            .setCallback(object : MediaSession.Callback {

            }).build()
        playerNotificationManager =
            PlayerNotificationManager.Builder(this, NOTIFICATION_ID, CHANNEL_ID)
                .setMediaDescriptionAdapter(object :
                    PlayerNotificationManager.MediaDescriptionAdapter {
                    override fun getCurrentContentTitle(player: Player): CharSequence {
                        return player.currentMediaItem?.mediaMetadata?.title ?: "Unknown Title"
                    }

                    override fun createCurrentContentIntent(player: Player): PendingIntent? {
                        // 点击通知时启动的 Activity (通常是你的播放器界面)
                        val intent =
                            Intent(this@PlayerService, MainActivity::class.java) //替换为你的MainActivity
                        return PendingIntent.getActivity(
                            this@PlayerService,
                            0,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                    }

                    override fun getCurrentContentText(player: Player): CharSequence? {
                        return player.currentMediaItem?.mediaMetadata?.artist ?: "Unknown Artist"
                    }

                    override fun getCurrentLargeIcon(
                        player: Player,
                        callback: PlayerNotificationManager.BitmapCallback
                    ): android.graphics.Bitmap? {
                        // 返回专辑封面 (你需要自己加载)
                        // 这里只是一个示例，你需要根据你的实际情况加载图片
                        return null //或者从url加载bitmap
                    }
                })
                .setNotificationListener(object : PlayerNotificationManager.NotificationListener {
                    override fun onNotificationPosted(
                        notificationId: Int,
                        notification: android.app.Notification,
                        ongoing: Boolean
                    ) {
                        // 当通知首次显示或更新时调用
                        if (ongoing) {
                            // 确保服务在前台运行
                            startForeground(notificationId, notification)
                        }
                    }

                    override fun onNotificationCancelled(
                        notificationId: Int,
                        dismissedByUser: Boolean
                    ) {
                        // 当通知被取消时调用 (例如，用户手动滑动关闭)
                        stopSelf() // 停止服务
                        stopForeground(true) //移除通知
                    }
                })
                .setSmallIconResourceId(R.drawable.xxz_rabbit)  // 设置通知的小图标
                .build()
        playerNotificationManager.setMediaSessionToken(mediaSession!!.platformToken)

        //添加自定义的Action, 比如添加一个关闭按钮
        val stopServiceIntent = Intent(this, PlayerService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val pendingStopServiceIntent = PendingIntent.getService(
            this,
            0,
            stopServiceIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        playerNotificationManager.setUseStopAction(true)  //使用停止Action
        playerNotificationManager.setUseFastForwardAction(true) //使用快进
        playerNotificationManager.setUseRewindAction(true) //使用快退
        playerNotificationManager.setUseNextAction(true)  //使用下一首
        playerNotificationManager.setUsePreviousAction(true) //使用上一首

        playerNotificationManager.setPlayer(player)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
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
        playerNotificationManager.setPlayer(null)
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (ACTION_STOP_SERVICE == intent?.action) {
            stopSelf()
            stopForeground(true) //移除通知
        }
        return super.onStartCommand(intent, flags, startId)
    }
}