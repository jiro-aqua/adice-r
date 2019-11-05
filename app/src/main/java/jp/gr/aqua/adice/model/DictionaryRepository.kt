package jp.gr.aqua.adice.model

import android.net.Uri
import android.provider.OpenableColumns
import jp.sblo.pandora.dice.DiceFactory
import jp.sblo.pandora.dice.IIndexCacheFile
import jp.sblo.pandora.dice.IdicInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream

class DictionaryRepository {
    private val mDice = DiceFactory.getInstance()

    private suspend fun createIndex(dicname: String, english: Boolean, defname: String) : Boolean
    {
        return withContext(Dispatchers.IO){
            // 辞書追加
            var failed = true
            val dicinfo = mDice.open(dicname)
            if (dicinfo != null) {
                // 登録成功ならば
                // インデクスキャッシュファイル名取得
                val cachename = dicname.replace("/", ".") + ".idx"

                // インデクス作成
                if (!dicinfo.readIndexBlock(object : IIndexCacheFile {
                            val path = "${ContextModel.cacheDir}/$cachename"

                            @Throws(FileNotFoundException::class)
                            override fun getInput(): FileInputStream {
                                return FileInputStream(path)
                            }

                            @Throws(FileNotFoundException::class)
                            override fun getOutput(): FileOutputStream {
                                return FileOutputStream(path)
                            }
                        })) {
                    mDice!!.close(dicinfo)
                } else {
                    failed = false
                    PreferenceRepository().setDefaultSettings(dicname, defname, english)
                }
            }
            !failed
        }
    }


    suspend fun openDictionary(uri : Uri) : Pair<Boolean,String>
    {
        return withContext(Dispatchers.IO){
            val cr = ContextModel.contentResolver
            val cursor = cr.query(uri, null, null, null, null, null)
            var displayName = ""
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
                cursor!!.close()

                val dicFile = File(ContextModel.filesDir, displayName)
                cr.openInputStream(uri)?.copyTo(FileOutputStream(dicFile))
                cursor.close()
                addDictionary(dicFile.absolutePath, false, dicFile.nameWithoutExtension)
            } catch (e: Throwable) {
                e.printStackTrace()
                false to ""
            }
        }
    }

    suspend fun addDictionary(dicname: String?, english: Boolean, defname: String) : Pair<Boolean,String>
    {
        if (dicname != null) {
            val result = createIndex(dicname, english, defname)
            if (result) {
                writeDictionary()
                return true to dicname
            }
            return false to dicname
        }
        return false to ""
    }

    private fun writeDictionary() {
        val dics = List<String>(mDice.dicNum) { mDice.getDicInfo(it).GetFilename() }
        PreferenceRepository().writeDics(dics)
    }
    fun swap(name : String , up : Boolean) {
        val dir = if ( up ) -1 else 1
        mDice.swap(mDice.getDicInfo(name), dir)
        writeDictionary()
    }

    private fun close(name : String ) = mDice.close(mDice.getDicInfo(name))

    fun remove( name : String ){
        // 該当する辞書を閉じる
        close(name)
        // 一覧を更新
        writeDictionary()
        // インデクスファイル削除
        indexCacheFilename(name).delete()
        // 辞書削除
        File(name).delete()
        // プレファレンスから削除
        PreferenceRepository().removeDic(name)
    }

    // 辞書一覧取得
    fun getDicList()  = List<IdicInfo>(mDice.dicNum) { mDice.getDicInfo(it) }

    private fun indexCacheFilename(name:String) = File( ContextModel.cacheDir.toString() ,  name.replace("/", ".") + ".idx" )

    fun indexCacheAccessor(name : String) : IIndexCacheFile {
        return object : IIndexCacheFile {
            private val file = indexCacheFilename(name)

            @Throws(FileNotFoundException::class)
            override fun getInput(): FileInputStream {
                return file.inputStream()
            }

            @Throws(FileNotFoundException::class)
            override fun getOutput(): FileOutputStream {
                return file.outputStream()
            }
        }
    }

}

