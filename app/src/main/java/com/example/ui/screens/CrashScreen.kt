package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CrashScreen(
  errorTitle: String,
  stackTrace: String,
  onRestart: () -> Unit
) {
  val context = LocalContext.current
  val verticalScroll = rememberScrollState()
  val horizontalScroll = rememberScrollState()

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF0F172A))
      .padding(20.dp)
  ) {
    Column(
      modifier = Modifier.fillMaxSize()
    ) {
      // Error Header
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(Color(0xFFDC2626).copy(alpha = 0.18f))
          .padding(16.dp)
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color(0xFFEF4444))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Application Crash Intercepted",
              color = Color(0xFFEF4444),
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = errorTitle,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // StackTrace console box
      Text(
        text = "Stack Trace Details:",
        color = Color(0xFF94A3B8),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold
      )

      Spacer(modifier = Modifier.height(6.dp))

      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xFF020617))
          .padding(12.dp)
          .verticalScroll(verticalScroll)
          .horizontalScroll(horizontalScroll)
      ) {
        Text(
          text = stackTrace,
          color = Color(0xFF38BDF8),
          fontFamily = FontFamily.Monospace,
          fontSize = 11.sp,
          lineHeight = 16.sp
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Action buttons
      Row(
        modifier = Modifier.fillMaxWidth()
      ) {
        OutlinedButton(
          onClick = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Crash Log", "Error: $errorTitle\n\n$stackTrace")
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Log copied to clipboard", Toast.LENGTH_SHORT).show()
          },
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color(0xFFE2E8F0)
          )
        ) {
          Text("Copy Log", fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.width(10.dp))

        Button(
          onClick = onRestart,
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2563EB)
          )
        ) {
          Text("Restart App", fontSize = 13.sp, color = Color.White)
        }
      }
    }
  }
}
