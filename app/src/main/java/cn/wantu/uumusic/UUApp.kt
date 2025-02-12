package cn.wantu.uumusic

import android.app.Application
import android.content.ComponentName
import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import cn.wantu.uumusic.controller.MusicPlayerController
import cn.wantu.uumusic.service.MusicService
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class UUApp : Application() {

    companion object {
        @get:Synchronized
        lateinit var instance: UUApp
            private set
        lateinit var client: OkHttpClient
            private set

        val context: Context
            get() = instance.applicationContext
        val json = Json { ignoreUnknownKeys = true }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        instance = this
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        client = OkHttpClient.Builder()
            .addInterceptor(logging).build()
        val sessionToken =
            SessionToken(this, ComponentName(this, MusicService::class.java))
        val mediaControllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        mediaControllerFuture.addListener({
            val player = mediaControllerFuture.get()
            MusicPlayerController.setController(player)
        }, MoreExecutors.directExecutor())
    }

}
