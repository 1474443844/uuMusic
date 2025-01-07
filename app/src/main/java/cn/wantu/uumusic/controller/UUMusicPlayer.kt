package cn.wantu.uumusic.controller

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.offline.DefaultDownloadIndex
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadIndex
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import cn.wantu.uumusic.controller.SettingConfig.downloadDir
import java.util.concurrent.Executors

@UnstableApi
class UUMusicPlayer(private val context: Context) {
    private lateinit var player: ExoPlayer
    private lateinit var downloadManager: DownloadManager
    private lateinit var downloadNotificationHelper: DownloadNotificationHelper
    private lateinit var downloadIndex: DownloadIndex
    private lateinit var downloadCache: Cache

    init {
        initPlayerAndDownloadManager()
    }

    private fun initPlayerAndDownloadManager() {

        // 1. 创建 DownloadCache
        val databaseProvider = StandaloneDatabaseProvider(context)
        downloadCache = SimpleCache(downloadDir, NoOpCacheEvictor(), databaseProvider)

        // 2. 创建 DownloadIndex
        downloadIndex = DefaultDownloadIndex(databaseProvider)

        // 3. 创建 DataSourceFactory
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
        val cacheReadDataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)

        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(downloadCache)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setCacheReadDataSourceFactory(cacheReadDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        // 注意：这里的 MediaSourceFactory 将用于播放和下载
        val mediaSourceFactory = DefaultMediaSourceFactory(cacheDataSourceFactory)

        // 4. 创建 DownloadManager
        downloadManager = DownloadManager(
            context,
            databaseProvider,
            downloadCache,
            httpDataSourceFactory,
            Executors.newFixedThreadPool(6) // 线程池大小可以根据需要调整
        )

        downloadManager.addListener(object : DownloadManager.Listener {
            override fun onDownloadChanged(
                downloadManager: DownloadManager,
                download: Download,
                finalException: Exception?
            ) {
                // 监听下载状态的变化
                when (download.state) {
                    Download.STATE_DOWNLOADING -> {
                        // 下载中
                        println("Download state: Downloading")
                    }

                    Download.STATE_COMPLETED -> {
                        // 下载完成
                        println("Download state: Completed")
                    }

                    Download.STATE_FAILED -> {
                        // 下载失败
                        println("Download state: Failed")
                    }

                    Download.STATE_STOPPED -> {
                        // 下载停止
                        println("Download state: Stopped")
                    }

                    Download.STATE_REMOVING -> {
                        // 下载删除中
                        println("Download state: Removing")
                    }

                    Download.STATE_RESTARTING -> {
                        // 下载重新开始
                        println("Download state: Restarting")
                    }

                    Download.STATE_QUEUED -> {
                        // 下载排队中
                        println("Download state: Queued")
                    }
                }
            }
        })
        // 5. 初始化 ExoPlayer
        player = ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
    }

    fun releasePlayer() {
        player.release()
        downloadManager.release()
    }

    private fun createMediaItem(url: String, customCacheKey: String): MediaItem {
        return MediaItem.Builder()
            .setUri(url)
            .setCustomCacheKey(customCacheKey) // 使用自定义的缓存键
            .build()
    }

    private fun createDownloadRequest(url: String, customCacheKey: String): DownloadRequest {
        return DownloadRequest.Builder(customCacheKey, Uri.parse(url)) // ID 设置为 customCacheKey
            .setCustomCacheKey(customCacheKey)
            .build()
    }

    fun playAndDownload(url: String) {
        // 使用 URL 作为自定义缓存键（也可以是其他唯一标识符）
        val customCacheKey = url

        val mediaItem = createMediaItem(url, customCacheKey)
        val downloadRequest = createDownloadRequest(url, customCacheKey)

        // 播放
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
        // 下载
        downloadManager.addDownload(downloadRequest)
    }

    fun isDownloaded(url: String): Boolean {
        val download = downloadIndex.getDownload(url)
        return download != null && download.state == Download.STATE_COMPLETED
    }

    fun removeDownload(url: String) {
        downloadManager.removeDownload(url)
    }
}