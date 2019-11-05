package jp.gr.aqua.adice.model

import android.content.ContentResolver
import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import java.io.File

class ContextModel{
    companion object{
        lateinit var resources : Resources
        lateinit var cacheDir : File
        lateinit var filesDir : File
        lateinit var assets : AssetManager
        lateinit var contentResolver : ContentResolver

        fun initialize(context : Context) {
            resources = context.resources
            cacheDir = context.cacheDir
            filesDir = context.filesDir
            assets = context.assets
            contentResolver = context.contentResolver
        }
    }
}