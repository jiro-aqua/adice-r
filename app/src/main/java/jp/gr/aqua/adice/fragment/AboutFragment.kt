package jp.gr.aqua.adice.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import jp.gr.aqua.adice.BuildConfig
import jp.gr.aqua.adice.R
import jp.gr.aqua.adice.databinding.FragmentAboutBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

open class AboutFragment : Fragment(){

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    protected open fun pageUrl() = ABOUT_PAGE

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentAboutBinding.inflate(inflater)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onStart() {
        super.onStart()

        val url = pageUrl()

        binding.webview.apply{
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
                    setFragmentResult("downloadResult", bundleOf("url" to url))
                    // 前の画面に
                    findNavController().popBackStack()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        internal val ABOUT_PAGE = "file:///android_asset/about.html"
    }
}
