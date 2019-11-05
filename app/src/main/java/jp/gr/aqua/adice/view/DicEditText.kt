package jp.gr.aqua.adice.view

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

class DicEditText : EditText {

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context) : super(context) {}

    override fun onFocusChanged(focused: Boolean, direction: Int,
                                previouslyFocusedRect: Rect?) {
        if (!focused) {
            val inputMethodManager = context
                    .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
        }
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
    }
}
