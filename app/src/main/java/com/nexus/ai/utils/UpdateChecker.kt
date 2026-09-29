package com.nexus.ai.utils

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class UpdateChecker(private val context: Context) {

    data class UpdateInfo(
        val versionCode: Int,
        val versionName: String,
        val apkUrl: String,
        val changelog: String,
        val mandatory: Boolean
    )

    /**
     * Cek update dari URL JSON.
     * @return UpdateInfo kalau ada versi lebih baru, null kalau tidak / gagal.
     */
    suspend fun checkForUpdate(
        jsonUrl: String,
        currentVersionCode: Int
    ): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL(jsonUrl)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
            }

            if (conn.responseCode != 200) return@withContext null

            val json = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()

            val obj = JSONObject(json)
            val latestCode = obj.getInt("versionCode")

            if (latestCode <= currentVersionCode) return@withContext null

            UpdateInfo(
                versionCode = latestCode,
                versionName = obj.optString("versionName", "Unknown"),
                apkUrl = obj.getString("apkUrl"),
                changelog = obj.optString("changelog", ""),
                mandatory = obj.optBoolean("mandatory", false)
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Mulai download APK. Return downloadId untuk tracking.
     */
    fun downloadApk(apkUrl: String, fileName: String = "nexus-ai-update.apk"): Long {
        // Hapus file lama kalau ada
        val old = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )
        if (old.exists()) old.delete()

        val request = DownloadManager.Request(Uri.parse(apkUrl)).apply {
            setTitle("Nexus AI Update")
            setDescription("Mengunduh versi terbaru...")
            setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            setDestinationInExternalFilesDir(
                context,
                Environment.DIRECTORY_DOWNLOADS,
                fileName
            )
            setAllowedOverMetered(true)
            setAllowedOverRoaming(false)
            setMimeType("application/vnd.android.package-archive")
        }

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return dm.enqueue(request)
    }

    /**
     * Query progress download. Return 0-100, atau -1 kalau gagal.
     */
    fun getDownloadProgress(downloadId: Long): Int {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = dm.query(query) ?: return 0

        return cursor.use {
            if (!it.moveToFirst()) return 0

            val status = it.getInt(
                it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
            )
            if (status == DownloadManager.STATUS_FAILED) return -1

            val bytesDownloaded = it.getLong(
                it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            )
            val bytesTotal = it.getLong(
                it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
            )

            if (bytesTotal <= 0) return 0
            ((bytesDownloaded * 100) / bytesTotal).toInt()
        }
    }

    fun isDownloadComplete(downloadId: Long): Boolean {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = dm.query(query) ?: return false

        return cursor.use {
            if (!it.moveToFirst()) return false
            val status = it.getInt(
                it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
            )
            status == DownloadManager.STATUS_SUCCESSFUL
        }
    }

    /**
     * Buka installer Android dengan APK yang sudah diunduh.
     */
    fun installApk(fileName: String = "nexus-ai-update.apk"): Boolean {
        return try {
            val apkFile = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )
            if (!apkFile.exists()) return false

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Listener untuk event download selesai.
     * Return receiver — WAJIB unregister di onDispose/onDestroy.
     */
    fun registerDownloadListener(
        downloadId: Long,
        onComplete: () -> Unit
    ): BroadcastReceiver {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId && isDownloadComplete(downloadId)) {
                    onComplete()
                }
            }
        }

        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                receiver, filter, Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(receiver, filter)
        }

        return receiver
    }

    fun unregisterListener(receiver: BroadcastReceiver) {
        runCatching { context.unregisterReceiver(receiver) }
    }
}