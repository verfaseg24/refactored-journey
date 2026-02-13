/*
 * Copyright (c) 2024 DuckDuckGo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duckduckgo.app.proxy

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import logcat.logcat
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProxyManager @Inject constructor() {

    companion object {
        private const val PROXY_LIST_URL = "https://github.com/verfaseg24/cuddly-octo-adventure/raw/main/valid_proxies.txt"
        private const val CONNECTION_TIMEOUT = 5000L
        private const val TEST_URL = "https://www.google.com"
    }

    private var currentProxy: Proxy? = null
    private val proxyList = mutableListOf<ProxyInfo>()

    data class ProxyInfo(
        val type: Proxy.Type,
        val host: String,
        val port: Int
    )

    suspend fun initialize() = withContext(Dispatchers.IO) {
        try {
            logcat { "Initializing proxy manager..." }
            loadProxyList()
            selectWorkingProxy()
        } catch (e: Exception) {
            logcat { "Failed to initialize proxy: ${e.message}" }
        }
    }

    private suspend fun loadProxyList() = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()

            val request = Request.Builder()
                .url(PROXY_LIST_URL)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string()?.let { content ->
                        parseProxyList(content)
                    }
                }
            }
        } catch (e: Exception) {
            logcat { "Failed to load proxy list: ${e.message}" }
        }
    }

    private fun parseProxyList(content: String) {
        proxyList.clear()
        content.lines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                parseProxyLine(trimmed)?.let { proxyList.add(it) }
            }
        }
        logcat { "Loaded ${proxyList.size} proxies" }
    }

    private fun parseProxyLine(line: String): ProxyInfo? {
        return try {
            // Format: type://host:port or host:port
            val parts = if (line.contains("://")) {
                val split = line.split("://")
                val type = when (split[0].lowercase()) {
                    "socks", "socks5" -> Proxy.Type.SOCKS
                    "http", "https" -> Proxy.Type.HTTP
                    else -> return null
                }
                val hostPort = split[1].split(":")
                Triple(type, hostPort[0], hostPort[1].toInt())
            } else {
                // Default to HTTP if no type specified
                val hostPort = line.split(":")
                Triple(Proxy.Type.HTTP, hostPort[0], hostPort[1].toInt())
            }

            ProxyInfo(parts.first, parts.second, parts.third)
        } catch (e: Exception) {
            logcat { "Failed to parse proxy line: $line - ${e.message}" }
            null
        }
    }

    private suspend fun selectWorkingProxy() = withContext(Dispatchers.IO) {
        for (proxyInfo in proxyList) {
            if (testProxy(proxyInfo)) {
                currentProxy = Proxy(proxyInfo.type, InetSocketAddress(proxyInfo.host, proxyInfo.port))
                logcat { "Selected working proxy: ${proxyInfo.host}:${proxyInfo.port} (${proxyInfo.type})" }
                return@withContext
            }
        }
        logcat { "No working proxy found, using direct connection" }
        currentProxy = Proxy.NO_PROXY
    }

    private suspend fun testProxy(proxyInfo: ProxyInfo): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val proxy = Proxy(proxyInfo.type, InetSocketAddress(proxyInfo.host, proxyInfo.port))
            val client = OkHttpClient.Builder()
                .proxy(proxy)
                .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                .build()

            val request = Request.Builder()
                .url(TEST_URL)
                .head()
                .build()

            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            logcat { "Proxy test failed for ${proxyInfo.host}:${proxyInfo.port} - ${e.message}" }
            false
        }
    }

    fun getCurrentProxy(): Proxy {
        return currentProxy ?: Proxy.NO_PROXY
    }

    suspend fun refreshProxy() {
        initialize()
    }
}
