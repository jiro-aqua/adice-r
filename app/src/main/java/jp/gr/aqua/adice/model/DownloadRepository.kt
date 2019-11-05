package jp.gr.aqua.adice.model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.zip.ZipInputStream

class DownloadRepository {

    private val STORAGE: File by lazy { ContextModel.filesDir }

    private fun getName(path: String): String {
        val patharr = path.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return patharr[patharr.size - 1]
    }

    suspend fun downloadDicfile(url: String): String? {
        return withContext(Dispatchers.IO){
            val client = OkHttpClient()
            val request = Request.Builder()
                    .url(url)
                    .build()

            val response = client.newCall(request).execute()
            val body = response.body?.byteStream()

            if (url.endsWith(".dic")) {
                val dst = File(STORAGE.path , getName(url))
                FileOutputStream(dst).use {
                    body?.copyTo(it)
                }
                dst.absolutePath
            } else if (url.endsWith(".zip")) {
                body?.let{
                    extractZip(it)
                }
            }else{
                null
            }
        }
    }

    private fun extractZip(inst: InputStream): String? {
        var ret: String? = null
        val zis: ZipInputStream
        try {
            zis = ZipInputStream(inst)
            while (ret == null ) {
                val ze = zis.nextEntry?:break
                val name = ze.name
                if (name.toLowerCase(locale = Locale.US).endsWith(".dic")) {
                    val nf = File(STORAGE.path , getName(name))
                    FileOutputStream(nf).use{
                        zis.copyTo( it )
                    }
                    ret = nf.path
                }
                zis.closeEntry()
            }
            zis.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ret
    }




}