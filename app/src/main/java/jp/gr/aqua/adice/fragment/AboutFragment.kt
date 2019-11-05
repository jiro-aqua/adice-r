package jp.gr.aqua.adice.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import jp.gr.aqua.adice.BuildConfig
import jp.gr.aqua.adice.R
import jp.gr.aqua.adice.viewmodel.AboutViewModel
import kotlinx.android.synthetic.main.fragment_about.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

open class AboutFragment : Fragment(){

    private val viewModel by lazy { ViewModelProviders.of(requireActivity()).get(AboutViewModel::class.java) }

    protected open fun pageUrl() = ABOUT_PAGE

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_about, container , false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onStart() {
        super.onStart()

        val url = pageUrl()

        webview.apply{
            loadUrl(url)
            addJavascriptInterface(JsCallbackObj(), "jscallback")
            settings.javaScriptEnabled = true
            isFocusable = true
            isFocusableInTouchMode = true
        }
    }

    internal inner class JsCallbackObj {

        @JavascriptInterface
        fun getAboutStrings(key: String): String {
            if (key == "version") {
                val versionName = BuildConfig.VERSION_NAME
                val versionCode = BuildConfig.VERSION_CODE
                return "Ver. $versionName ($versionCode)"
            } else return if (key == "description") {
                resources.getString(R.string.description)
            } else if (key == "manual") {
                resources.getString(R.string.manual)
            } else {
                ""
            }
        }

        @JavascriptInterface
        fun throwIntentByUrl(url: String?, @Suppress("UNUSED_PARAMETER") requestcode: Int) {
            if (url != null && url.isNotEmpty()) {
                GlobalScope.launch(Dispatchers.Main){
                    viewModel.notify(url)
                    // 前の画面に
                    findNavController().popBackStack()
                }
            }
        }
    }

    companion object {
        internal val ABOUT_PAGE = "file:///android_asset/about.html"
    }
}
