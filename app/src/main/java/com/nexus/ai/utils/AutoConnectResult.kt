package com.nexus.ai.utils

sealed class AutoConnectResult {
    data class ConnectedInternetOk(val ssid: String) : AutoConnectResult()
    data class ConnectedCaptivePortal(val ssid: String) : AutoConnectResult()
    object NoOpenNetwork : AutoConnectResult()
    object NoNetworks : AutoConnectResult()
    object SuggestionFailed : AutoConnectResult()
}