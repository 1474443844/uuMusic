package cn.wantu.uumusic.service

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import cn.wantu.uumusic.MainActivity
import cn.wantu.uumusic.R
import cn.wantu.uumusic.UUApp

@UnstableApi
class MusicService : MediaSessionService() {
    private lateinit var player: Player
    private lateinit var session: MediaSession
    private lateinit var playerNotificationManager: PlayerNotificationManager

    companion object {
        const val NOTIFICATION_ID = 20050307
        const val CHANNEL_ID = "uuMusic_playback_channel"

        //        const val ACTION_STOP_SERVICE = "com.example.myapp.action.STOP_SERVICE" //自定义action
        val EXTRA_NOTIFICATION = "${UUApp.instance.packageName}.notification"
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true) // 允许跨协议重定向（https -> http）
        val dataSourceFactory: DataSource.Factory =
            DefaultDataSource.Factory(applicationContext, httpDataSourceFactory)
        player = ExoPlayer.Builder(applicationContext)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .build()

        session = MediaSession.Builder(this, player)
            .setCallback(object : MediaSession.Callback {

            })
            /*.setSessionActivity(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java).apply {
                        putExtra(EXTRA_NOTIFICATION, true)
                        action = Intent.ACTION_VIEW
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )*/
            .build()
        /*
                setMediaNotificationProvider(
                    DefaultMediaNotificationProvider.Builder(applicationContext).build().apply {
                        setSmallIcon(R.drawable.baseline_add_music_24)
                    }
                )*/
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
                            Intent(this@MusicService, MainActivity::class.java) //替换为你的MainActivity
                        return PendingIntent.getActivity(
                            this@MusicService,
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

    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return session
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        player.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
        session.release()
    }


}