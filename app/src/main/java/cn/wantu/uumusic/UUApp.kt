package cn.wantu.uumusic

import android.app.Application
import android.content.Context
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
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        client = OkHttpClient.Builder()
            .addInterceptor(logging).build()
    }
}
