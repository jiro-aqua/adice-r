package jp.gr.aqua.adice.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class ResultClickDialogViewModel(application: Application) : AndroidViewModel(application) {
    val linkClicked = MutableLiveData<String>()

    fun click(link : String) {
        linkClicked.postValue(link)
    }
}
