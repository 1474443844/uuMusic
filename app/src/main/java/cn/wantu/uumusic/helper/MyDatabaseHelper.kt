package cn.wantu.uumusic.helper

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MyDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "UUMusic.db"  // 数据库名称
        private const val DATABASE_VERSION = 1             // 数据库版本号
        private const val TABLE_NAME = "songInfo"              // 表名称
        private const val COLUMN_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_SINGER = "singer"
        private const val COLUMN_COVER = "cover"
        private const val COLUMN_ALBUM = "album"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE $TABLE_NAME ("
                + "$COLUMN_ID INTEGER, "
                + "$COLUMN_TITLE TEXT, "
                + "$COLUMN_SINGER TEXT, "
                + "$COLUMN_COVER TEXT, "
                + "$COLUMN_ALBUM TEXT)")
        db.execSQL(createTable)  // 创建表
    }

    fun addSongInfo(id: Long, title: String, singer: String, cover: String, album: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID, id)
            put(COLUMN_TITLE, title)
            put(COLUMN_SINGER, singer)
            put(COLUMN_COVER, cover)
            put(COLUMN_ALBUM, album)
        }
        db.insert(TABLE_NAME, null, values)  // 插入数据
        db.close()
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 数据库升级时的操作
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }
}
