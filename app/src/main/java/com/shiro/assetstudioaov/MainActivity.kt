package com.shiro.assetstudioaov

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.graphics.Typeface
import java.io.BufferedInputStream

class MainActivity : Activity() {
    private lateinit var result: TextView
    private val openFile = 4101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(23, 21, 31)
        window.navigationBarColor = android.graphics.Color.rgb(23, 21, 31)

        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(28), dp(22), dp(22))
            setBackgroundColor(android.graphics.Color.rgb(18, 18, 24))
        }
        val title = TextView(this).apply {
            text = "AssetStudio AOV"
            textSize = 27f
            setTypeface(null, Typeface.BOLD)
            setTextColor(android.graphics.Color.WHITE)
        }
        val subtitle = TextView(this).apply {
            text = "Android preview build · Unity bundle inspector"
            textSize = 14f
            setTextColor(0xFFBDB8C9.toInt())
            setPadding(0, dp(6), 0, dp(22))
        }
        val choose = Button(this).apply {
            text = "Choose game file"
            isAllCaps = false
            setOnClickListener { launchPicker() }
        }
        result = TextView(this).apply {
            text = "Select a game asset or bundle to inspect it.\n\nThis preview build checks the file header and basic metadata. Full AOV decryption, asset extraction, textures and 3D model export are not implemented yet."
            textSize = 15f
            setTextColor(0xFFE8E4EF.toInt())
            setPadding(dp(16), dp(18), dp(16), dp(18))
            setBackgroundColor(0xFF25232D.toInt())
        }
        val note = TextView(this).apply {
            text = "No broad storage permission is needed. Android's file picker grants access only to the file you select."
            textSize = 12f
            setTextColor(0xFFAAA5B4.toInt())
            setPadding(0, dp(18), 0, 0)
        }
        page.addView(title)
        page.addView(subtitle)
        page.addView(choose, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)))
        page.addView(result, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(18) })
        page.addView(note)
        val scroll = ScrollView(this).apply { addView(page) }
        setContentView(scroll)
    }

    private fun launchPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, openFile)
    }

    @Deprecated("Deprecated by Android API; retained for broad device compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != openFile || resultCode != Activity.RESULT_OK) return
        val uri = data?.data ?: return
        try {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: Exception) { }
        inspect(uri)
    }

    private fun inspect(uri: Uri) {
        try {
            val name = queryName(uri)
            val size = contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
            val header = contentResolver.openInputStream(uri)?.use { input ->
                BufferedInputStream(input).use { stream ->
                    val bytes = ByteArray(64)
                    val count = stream.read(bytes)
                    if (count <= 0) ByteArray(0) else bytes.copyOf(count)
                }
            } ?: ByteArray(0)
            val ascii = header.take(16).map { b ->
                val c = b.toInt() and 0xff
                if (c in 32..126) c.toChar() else '.'
            }.joinToString("")
            val hex = header.take(32).joinToString(" ") { "%02X".format(it.toInt() and 0xff) }
            val signature = when {
                ascii.startsWith("UnityFS") -> "UnityFS bundle signature detected"
                ascii.startsWith("UnityRaw") -> "UnityRaw signature detected"
                ascii.startsWith("UnityWeb") -> "UnityWeb signature detected"
                ascii.startsWith("PK") -> "ZIP archive signature detected"
                else -> "No common Unity bundle signature at byte zero"
            }
            val sizeText = if (size >= 0) "${size} bytes" else "Unknown"
            result.text = "FILE INSPECTION\n\nName: $name\nSize: $sizeText\n\nResult: $signature\n\nHeader (text): $ascii\n\nHeader (hex): $hex\n\nThis is only a first-pass signature check. A missing signature does not prove the file is unsupported; some game files may be wrapped, compressed or encrypted."
        } catch (e: Exception) {
            result.text = "Could not inspect this file.\n\n${e.message ?: "Unknown error"}"
        }
    }

    private fun queryName(uri: Uri): String {
        contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) return c.getString(0) ?: uri.lastPathSegment ?: "Unknown"
        }
        return uri.lastPathSegment ?: "Unknown"
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
