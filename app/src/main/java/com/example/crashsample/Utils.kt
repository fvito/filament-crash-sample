package com.example.crashsample

import android.content.Context
import android.net.http.HttpResponseCache
import java.io.File

private const val CACHE_DIR = "ar"
private const val CACHE_SIZE = 100 * 1024 * 1024L // 100MB Cache size

fun enableSharedArCache(context: Context) {
    if (HttpResponseCache.getInstalled() == null) {
        val cacheDir = File(context.cacheDir, CACHE_DIR)
        HttpResponseCache.install(cacheDir, CACHE_SIZE)
    }
}

fun flushSharedArCache() {
    HttpResponseCache.getInstalled()?.flush()
}