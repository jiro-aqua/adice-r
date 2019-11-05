package jp.gr.aqua.adice.model

import android.content.Context
import androidx.preference.PreferenceManager
import jp.gr.aqua.adice.AdiceApplication
import jp.gr.aqua.adice.BuildConfig
import jp.gr.aqua.adice.R
import java.util.regex.Pattern

class PreferenceRepository {

    private val context = AdiceApplication.appContext
    private val sp = PreferenceManager.getDefaultSharedPreferences(context)

    fun dictionaryPreferenceName(name:String) = name.replace("/","_")
    private fun dictionaryPreference(name:String) = context.getSharedPreferences(dictionaryPreferenceName(name), Context.MODE_PRIVATE)


    fun readDictionarySettings(name:String) : DictionarySettings
    {
        val dicsp = dictionaryPreference(name)

        return DictionarySettings(
                dicname = dicsp.getString(KEY_DICNAME, "")!!,
                english = dicsp.getBoolean(KEY_ENGLISH, false),
                use = dicsp.getBoolean(KEY_USE, false),
                resultNum = Integer.parseInt(dicsp.getString(KEY_RESULTNUM, "30")!!)
        )
    }

    fun setDefaultSettings(name : String, defname: String, english: Boolean) {

        val dicsp = dictionaryPreference(name)

        // 名称未設定の時はデフォルトに戻す
        if (dicsp.getString(KEY_DICNAME, "")!!.isEmpty()) {
            dicsp.edit().apply{
                putString(KEY_DICNAME, defname)
                putBoolean(KEY_ENGLISH, english)
                putBoolean(KEY_USE, true)
                putString(KEY_RESULTNUM, "30")

                // 辞書名自動判定
                for (i in DICNTEMPLATE.indices) {
                    val p = Pattern.compile(DICNTEMPLATE[i].pattern, Pattern.CASE_INSENSITIVE)
                    val m = p.matcher(name)
                    if (m.find()) {
                        val dicname = if (m.groupCount() > 0) {
                            val edt = m.group(1)
                            ContextModel.resources.getString(DICNTEMPLATE[i].resourceDicname, edt)
                        } else {
                            ContextModel.resources.getString(DICNTEMPLATE[i].resourceDicname)
                        }
                        putString(KEY_DICNAME, dicname)
                        putBoolean(KEY_ENGLISH, DICNTEMPLATE[i].englishFlag)
                    }
                }
                apply()
            }
        }
    }

    fun readGeneralSettings(): Settings {
        return Settings(
                normalize = sp.getBoolean(KEY_NORMALIZE_SEARCH, true)
        )
    }

    fun getDics(): String {
        return sp.getString(KEY_DICS, "")!!
    }

    fun writeDics(filenames: List<String>) {
        val dics = filenames.fold(StringBuilder()) { acc, s -> acc.append("$s|") }
        sp.edit().putString(KEY_DICS, dics.toString()).apply()
    }

    fun getDicName(name:String): String? {
        val dicsp = dictionaryPreference(name)
        return dicsp.getString(KEY_DICNAME, name)
    }

    fun removeDic(name : String){
        val dicsp = dictionaryPreference(name)
        dicsp.edit().clear().apply()
    }

    fun isVersionUp(): Boolean {
        val lastVersion= sp.getInt(KEY_LASTVERSION, 0)
        val versioncode= BuildConfig.VERSION_CODE
        if ( lastVersion == 0 ){
            sp.edit()
                    .putBoolean(KEY_NORMALIZE_SEARCH, true)
                    .apply()
        }

        if (lastVersion != versioncode) {
            sp.edit()
                    .putInt(KEY_LASTVERSION, versioncode)
                    .apply()
            return true
        }
        return false
    }

    data class Settings(
            var normalize: Boolean = false
    )

    data class DictionarySettings(
            val dicname: String,
            val english : Boolean,
            val use : Boolean,
            val resultNum : Int
    )

    companion object {
        const val KEY_DICS = "dics"

        const val KEY_ADD_DICTIONARY = "AddDictionary"
        const val KEY_DOWNLOAD_DICTIONARY = "DownloadDictionary"
        const val KEY_PROGRESS = "progress"
        private const val KEY_NORMALIZE_SEARCH = "normalizesearch"
        private const val KEY_LASTVERSION = "LastVersion"

        const val KEY_DICNAME = "|dicname"
        const val KEY_FILENAME = "|filename"
        const val KEY_USE = "|use"
        const val KEY_ENGLISH = "|english"
        const val KEY_RESULTNUM = "|resultnum"
        const val KEY_MOVE_UP = "|MoveUp"
        const val KEY_MOVE_DOWN = "|MoveDown"
        const val KEY_REMOVE = "|Remove"


        internal class DicTemplate(var pattern: String, var resourceDicname: Int, var englishFlag: Boolean)

        private val DICNTEMPLATE = arrayOf(
                DicTemplate("/EIJI[a-zA-Z]*-([0-9]+)U?.*\\.DIC", R.string.dicname_eijiro, true),
                DicTemplate("/WAEI-([0-9]+)U?.*\\.DIC", R.string.dicname_waeijiro, false),
                DicTemplate("/REIJI([0-9]+)U?.*\\.DIC", R.string.dicname_reijiro, false),
                DicTemplate("/RYAKU([0-9]+)U?.*\\.DIC", R.string.dicname_ryakujiro, false),
                DicTemplate("/PDEJ2005U?.dic", R.string.dicname_pdej, true),
                DicTemplate("/PDEDICTU?.dic", R.string.dicname_edict, false),
                DicTemplate("/PDWD1913U?.dic", R.string.dicname_webster, true),
                DicTemplate("/f2jdic.dic", R.string.dicname_ichirofj, false)
        )
    }
}