package jp.gr.aqua.adice.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.util.set
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import jp.gr.aqua.adice.BR
import jp.gr.aqua.adice.R
import jp.gr.aqua.adice.model.ResultModel
import java.util.*


class ResultView : RecyclerView {

    interface ResultClickListener {
        fun onResultClicked(view: View, position: Int)
        fun onResultLongClicked(view: View, position: Int): Boolean
    }

    private fun init(@Suppress("UNUSED_PARAMETER") context: Context) {
        isFocusable = true
        isFocusableInTouchMode = true
        setBackgroundColor(Color.WHITE)
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        requestFocus()
        return super.onTouchEvent(ev)
    }

    data class ResultViewModel(val position: Int, val listener: ResultClickListener) {
        fun onClick(view: View) = listener.onResultClicked(view , position)
        fun onLongClick(view: View) = listener.onResultLongClicked(view , position)
    }

    class ResultHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)

    class ResultAdapter(private val objects: ArrayList<ResultModel>, private val listener: ResultClickListener) : RecyclerView.Adapter<ResultHolder>() {
        companion object {
            private val LayoutResource = SparseIntArray()

            init {
                LayoutResource[ResultModel.WORD] = R.layout.list_row_word
                LayoutResource[ResultModel.NONE] = R.layout.list_row_none
                LayoutResource[ResultModel.NORESULT] = R.layout.list_row_noresult
                LayoutResource[ResultModel.MORE] = R.layout.list_row_more
                LayoutResource[ResultModel.FOOTER] = R.layout.list_row_footer
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultHolder {
            val binding = DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), LayoutResource[viewType], parent, false)
            return ResultHolder(binding)
        }

        override fun onBindViewHolder(holder: ResultHolder, position: Int) {
            holder.binding.setVariable(BR.result, objects[position])
            holder.binding.setVariable(BR.viewModel, ResultViewModel(position, listener))
            holder.binding.executePendingBindings()
        }

        override fun getItemCount(): Int = objects.size
        override fun getItemViewType(position: Int): Int = objects[position].mode
    }
}
