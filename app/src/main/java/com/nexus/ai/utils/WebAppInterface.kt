package com.nexus.ai.utils

import android.util.Log
import android.webkit.JavascriptInterface

/**
 * JavaScript bridge untuk WebView peta.
 * Dipanggil dari JS: `Android.setPosition('(lat, lng)')`
 */
class WebAppInterface(
    private val onPositionPicked: (lat: Double, lng: Double) -> Unit,
    private val onZoomChanged: (zoom: Double) -> Unit = {}
) {

    @JavascriptInterface
    fun setPosition(str: String) {
        try {
            // Format: "(lat, lng)"
            val start = str.indexOf('(') + 1
            val comma = str.indexOf(',')
            val end = str.indexOf(')')

            if (start <= 0 || comma <= 0 || end <= 0) {
                Log.e(TAG, "Format koordinat salah: $str")
                return
            }

            val lat = str.substring(start, comma).trim().toDouble()
            val lng = str.substring(comma + 1, end).trim().toDouble()
            onPositionPicked(lat, lng)
        } catch (t: Throwable) {
            Log.e(TAG, "Gagal parsing koordinat: $str", t)
        }
    }

    @JavascriptInterface
    fun setZoom(str: String) {
        try {
            onZoomChanged(str.toDouble())
        } catch (t: Throwable) {
            Log.e(TAG, "Gagal parsing zoom: $str", t)
        }
    }

    companion object {
        const val JS_NAME = "Android"
        private val TAG = WebAppInterface::class.java.simpleName
    }
}