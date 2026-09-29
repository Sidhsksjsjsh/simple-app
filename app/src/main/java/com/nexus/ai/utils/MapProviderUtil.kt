package com.nexus.ai.utils

import java.util.Locale

object MapProviderUtil {

    @JvmStatic
    fun getDefaultMapProvider(locale: Locale): String {
        return when (locale.language) {
            "de" -> "OpenStreetMap.DE"
            "fr" -> "OpenStreetMap.France"
            else -> "OpenStreetMap"
        }
    }
}