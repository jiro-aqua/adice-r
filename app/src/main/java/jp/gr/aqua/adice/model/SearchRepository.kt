package jp.gr.aqua.adice.model

import android.graphics.Typeface
import android.text.Html
import android.util.Log
import jp.gr.aqua.adice.BuildConfig
import jp.gr.aqua.adice.R
import jp.sblo.pandora.dice.DiceFactory
import jp.sblo.pandora.dice.IdicInfo
import jp.sblo.pandora.dice.IdicResult
import kotlinx.coroutines.*
import kotlinx.coroutines.NonCancellable.isActive
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set
import kotlin.coroutines.CoroutineContext

@InternalCoroutinesApi
class SearchRepository {
    private var mInitialized = false

    private val mDice = DiceFactory.getInstance()

    private var mNormalize = true
    private var mLast: String? = ""

    private val mSearchHistory: ArrayList<CharSequence> = ArrayList()

    private var diceJob: Job? = null
    private val diceContext: CoroutineContext
        get() {
            val job = Job()
            diceJob = job
            return Dispatchers.IO + job
        }

    private lateinit var phoneticFont: Typeface
    private lateinit var mStartPage: String
    private lateinit var mFooter: String
    private lateinit var mDescription: String
    private lateinit var cacheDir: File

    fun initialize() {
        loadResources()
        loadIrreg()
        initDice()
        Log.i(TAG, "aDice Initialized")
        mInitialized = true
    }

    suspend fun startPage(): List<ResultModel> {
        return withContext(Dispatchers.IO) {
            val result = ArrayList<ResultModel>()
            generateDisp(DISP_MODE_START, 0, null, result, -1)
            result
        }
    }

    suspend fun search(text: String): List<ResultModel>? {
        if (text.isEmpty()) return null
        while(!mInitialized) { delay(100) }
        diceJob?.cancelAndJoin()
        return withContext(diceContext) {
            val converted = if (mNormalize) DiceFactory.convert(text) else text
            if (converted.isNotEmpty() && mLast != converted) {
                val result = searchProc(converted, 10)
                if ( result != null ) mLast = converted
                result
            } else {
                null
            }
        }
    }

    suspend fun more(currentResult: List<ResultModel>, position: Int): List<ResultModel>? {
        while(!mInitialized) { delay(100) }

        diceJob?.cancelAndJoin()
        return withContext(diceContext) {
            val result: ArrayList<ResultModel> = currentResult.toCollection(ArrayList())
            result.removeAt(position)
            val dic = result[position].dic
            val pr = mDice.getMoreResult(dic)
            generateDisp(DISP_MODE_RESULT, dic, pr, result, position)
            result
        }
    }

    fun pushHistory() {
        val last = mLast ?: ""
        if (last.isNotEmpty() && (mSearchHistory.isEmpty() || last != mSearchHistory[0])) {     // todo linkedHashMapで置き換え
            mSearchHistory.add(0, last)
        }
    }

    fun popHistory(): CharSequence? {
        return if (mSearchHistory.isNotEmpty()) {
            val cs = mSearchHistory[0]
            mSearchHistory.removeAt(0)
            cs
        } else {
            null
        }
    }

    fun applySettings() {
        mLast = ""

        val settings = PreferenceRepository().readGeneralSettings()
        mNormalize = settings.normalize

        for (i in 0 until mDice.dicNum) {
            val dicinfo = mDice.getDicInfo(i)
            val name = dicinfo.GetFilename()
            applyDictionarySettings(dicinfo, PreferenceRepository().readDictionarySettings(name))
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    private fun initDice() {
        val dicss = PreferenceRepository().getDics()
        val dics = dicss.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }

        // 外部辞書読込
        dics.forEach { name ->
            if (name.isEmpty()) {
                return@forEach
            }
            val dicinfo = mDice.open(name)
            if (dicinfo != null) {
                Log.i(TAG, "Open OK:$name")

                // インデクス作成
                if (!dicinfo.readIndexBlock(DictionaryRepository().indexCacheAccessor(name))) {
                    mDice.close(dicinfo)
                } else {
                    applyDictionarySettings(dicinfo, PreferenceRepository().readDictionarySettings(name))
                }
            } else {
                Log.i(TAG, "Open NG:$name")
            }
        }
    }

    private fun applyDictionarySettings(dicinfo: IdicInfo, settings: PreferenceRepository.DictionarySettings) {
        dicinfo.SetDicName(settings.dicname)
        dicinfo.SetEnglish(settings.english)
        dicinfo.SetNotuse(!settings.use)
        dicinfo.SetSearchMax(settings.resultNum)

        dicinfo.SetIndexSize(20)
        dicinfo.SetPhoneticSize(16)
        dicinfo.SetTransSize(16)
        dicinfo.SetSampleSize(16)
    }

    // 英語向けIRREG読込
    private fun loadIrreg() {
        val name = "IrregDic.txt"
        val irreg = HashMap<String, String>()
        ContextModel.assets.open(name).reader(charset = Charsets.UTF_8).use{
            it.readLines().forEach{
                line->
                val s = line.split('\t')
                irreg[s[0]] = s[1]
            }
            Log.i(TAG, "Open OK:$name")
        }
        mDice.setIrreg(irreg)
    }

    private suspend fun searchProc(text: String, timer: Int): List<ResultModel>? {

        val result = ArrayList<ResultModel>()
        // Log.i("search thread ", "sleeping...");
        delay(timer.toLong())
        // Log.i("search thread ", "got up");
        val dicnum = mDice.dicNum
        for (dic in 0 until dicnum) {
            if (!isActive)
                return null

            if (!mDice.isEnable(dic)) {
                continue
            }

            if (!isActive)
                return null

            mDice.search(dic, text)

            val pr = mDice.getResult(dic)

            if (!isActive)
                return null
            if (pr.count > 0) {
                generateDisp(DISP_MODE_RESULT, dic, pr, result, -1)
                generateDisp(DISP_MODE_FOOTER, dic, null, result, -1)
            }

            if (!isActive)
                return null
        }

        if (result.size == 0) {
            generateDisp(DISP_MODE_NORESULT, -1, null, result, -1)
        }
        if (!isActive)
            return null

        return result
    }

    private fun generateDisp(mode: Int, dic: Int, pr: IdicResult?, result: ArrayList<ResultModel>, _pos: Int) {
        var pos = _pos
        synchronized(this) {
            when (mode) {
                DISP_MODE_RESULT -> {
                    // 表示させる内容を生成
                    for (i in 0 until pr!!.count) {
                        val idx = pr.getDisp(i)
                        val index = if (idx.isNullOrEmpty()) {
                            pr.getIndex(i)
                        }else{
                            idx
                        }
                        val info = mDice.getDicInfo(dic)

                        val data = ResultModel(mode=ResultModel.Mode.WORD, dic=dic,
                                index = index,
                                phone = pr.getPhone(i),
                                trans = pr.getTrans(i),
                                sample = pr.getSample(i),

                                indexSize = info.GetIndexSize(),
                                phoneSize = info.GetPhoneticSize(),
                                transSize = info.GetTransSize(),
                                sampleSize = info.GetSampleSize(),

                                indexFont = null,
                                phoneFont = phoneticFont,
                                transFont = null,
                                sampleFont = null
                                )

                        if (pos == -1) {
                            result.add(data)
                        } else {
                            result.add(pos++, data)
                        }
                    }

                    // 結果がまだあるようならmoreボタンを表示
                    if (mDice.hasMoreResult(dic)) {
                        val data = ResultModel(mode=ResultModel.Mode.MORE, dic=dic)

                        if (pos == -1) {
                            result.add(data)
                        } else {
                            result.add(pos++, data)
                        }
                    }
                }
                DISP_MODE_FOOTER -> {
                    var dicname: String? = mDice.getDicInfo(dic).GetDicName()
                    if (dicname.isNullOrEmpty()) {
                        dicname = mDice.getDicInfo(dic).GetFilename()
                    }
                    val data = ResultModel(mode=ResultModel.Mode.FOOTER, dic=dic,
                            index = String.format(mFooter, dicname!!),
                            indexSize = 16)

                    if (pos == -1) {
                        result.add(data)
                    } else {
                        result.add(pos++, data)
                    }
                }
                DISP_MODE_NORESULT -> {
                    val data = ResultModel(mode=ResultModel.Mode.NONE, dic=0,
                            indexSize = 16)
                    result.add(data)
                }
                DISP_MODE_START -> {
                    val versionName = BuildConfig.VERSION_NAME
                    val versionCode = BuildConfig.VERSION_CODE
                    val version = "Ver. " + String.format("%s (%d)", versionName, versionCode)
                    val description = mDescription

                    @Suppress("DEPRECATION")
                    val index = Html.fromHtml(mStartPage.replace("\$version$", version).replace("\$description$", description))
                    val data = ResultModel(mode=ResultModel.Mode.NONE, dic=0,
                            index = index,
                            indexSize = 16)
                    result.add(data)
                }
            }
            Unit
        }
    }

    private fun loadResources() {
        mFooter = ContextModel.resources.getString(R.string.resulttitlehtml)
        mDescription = ContextModel.resources.getString(R.string.description)
        mStartPage = ContextModel.assets.open("start.html").bufferedReader(charset = Charsets.UTF_8).readText()
        phoneticFont = Typeface.createFromAsset(ContextModel.assets, "DoulosSILR.ttf")
        cacheDir = ContextModel.cacheDir
    }

    companion object {
        private const val TAG = "aDiceVM"

        private const val DISP_MODE_RESULT = 0
        private const val DISP_MODE_MORE = 1
        private const val DISP_MODE_FOOTER = 2
        private const val DISP_MODE_NORESULT = 3
        private const val DISP_MODE_START = 4
    }


}
