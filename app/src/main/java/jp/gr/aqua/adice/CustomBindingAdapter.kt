package jp.gr.aqua.adice

import android.graphics.Typeface
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import jp.gr.aqua.adice.model.ResultModel

object CustomBindingAdapter {
    //xmlに定義する際のBindingAdapter

    private fun TextView.setItem(str: CharSequence?, tf: Typeface?, size: Int) {
        if (str.isNullOrEmpty()) {
            visibility = View.GONE
        } else {
            visibility = View.VISIBLE
            text = str
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, size.toFloat())
            typeface = tf
        }
    }

    @BindingAdapter("indexResult")
    @JvmStatic
    fun indexResult(view: TextView, result: ResultModel) {
        view.setItem(result.Index, result.IndexFont, result.IndexSize)
    }

    @BindingAdapter("phoneResult")
    @JvmStatic
    fun phoneResult(view: TextView, result: ResultModel) {
        view.setItem(result.Phone, result.PhoneFont, result.PhoneSize)
    }

    @BindingAdapter("transResult")
    @JvmStatic
    fun transResult(view: TextView, result: ResultModel) {
        view.setItem(result.Trans, result.TransFont, result.TransSize)
    }

    @BindingAdapter("sampleResult")
    @JvmStatic
    fun sampleResult(view: TextView, result: ResultModel) {
        view.setItem(result.Sample, result.SampleFont, result.SampleSize)
    }

}
