package cn.wantu.uumusic.activity

import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable


class OauthActivity : DefaultActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = getIntent()
        var action = intent.action
        val data = intent.data

        if (Intent.ACTION_VIEW == action && data != null) {
            val scheme = data.scheme // "your-app-scheme"
            val host = data.host // "your-app-host"
            val path = data.path // "/your-app-path"

            // 根据 scheme, host, path 处理不同的逻辑
            val params = data.pathSegments
            if (params.isNotEmpty()) {
                // 处理path中的参数
                val param = params[0]
                println(param)
            }

            val queryParameter = data.getQueryParameter("param1")
            if (queryParameter != null) {
                // 处理query parameter
                println(queryParameter)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // 如果你的Activity是 singleTask 或 singleTop 模式，新的 Intent 会通过这个方法传递
        // 在这里处理 Intent 的逻辑
    }

    @Composable
    override fun SetupUI() {
        Column {

        }
    }

    override fun doBeforeUI() {
    }
}