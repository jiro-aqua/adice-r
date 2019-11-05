package jp.gr.aqua.adice.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class AboutViewModel(application: Application) : AndroidViewModel(application) {
    val downloadResult = MutableLiveData<String>()

    fun notify(result : String) {
        downloadResult.postValue(result)
    }
}
