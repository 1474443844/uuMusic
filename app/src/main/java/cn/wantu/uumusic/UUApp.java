package cn.wantu.uumusic;

import android.app.Application;
import android.content.Context;

import cn.wantu.uumusic.model.MusicController;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class UUApp extends Application {

    private static UUApp instance;
    private static OkHttpClient client;
    private static MusicController mediaPlay;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        client = new OkHttpClient.Builder()
                .addInterceptor(logging).build();
        mediaPlay = new MusicController();
    }

    public synchronized static UUApp getInstance() {
        return instance;
    }
    public synchronized static MusicController getMediaPlayer(){
        return mediaPlay;
    }
    public static Context getContext(){
        return instance.getApplicationContext();
    }
    public static String getStr(int resid){
        return instance.getString(resid);
    }
    public static OkHttpClient getClient(){
        return client;
    }

}