package com.example

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.ui.screens.CrashScreen
import com.example.ui.screens.OSSimulatorScreen
import com.example.ui.theme.MyApplicationTheme
import java.io.PrintWriter
import java.io.StringWriter

class MainActivity : ComponentActivity() {

  companion object {
    var globalCrashTitle by mutableStateOf<String?>(null)
    var globalStackTrace by mutableStateOf<String?>(null)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Global Uncaught Exception Handler
    val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
      Log.e("ZterOSCrash", "Uncaught exception in thread ${thread.name}", throwable)
      val sw = StringWriter()
      val pw = PrintWriter(sw)
      throwable.printStackTrace(pw)
      val stackTraceString = sw.toString()
      val title = "${throwable.javaClass.simpleName}: ${throwable.message ?: "No message"}"

      runOnUiThread {
        globalCrashTitle = title
        globalStackTrace = stackTraceString
      }
    }

    setContent {
      MyApplicationTheme(darkTheme = true) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = Color(0xFF141419)
        ) {
          val errTitle = globalCrashTitle
          val errTrace = globalStackTrace
          if (errTitle != null && errTrace != null) {
            CrashScreen(
              errorTitle = errTitle,
              stackTrace = errTrace,
              onRestart = {
                globalCrashTitle = null
                globalStackTrace = null
                val intent = Intent(this@MainActivity, MainActivity::class.java).apply {
                  addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(intent)
                Runtime.getRuntime().exit(0)
              }
            )
          } else {
            OSSimulatorScreen()
          }
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun OSSimulatorPreview() {
  MyApplicationTheme(darkTheme = true) {
    OSSimulatorScreen()
  }
}
