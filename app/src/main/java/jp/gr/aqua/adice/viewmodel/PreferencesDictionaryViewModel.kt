package jp.gr.aqua.adice.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import jp.gr.aqua.adice.model.DictionaryRepository

class PreferencesDictionaryViewModel(application: Application): AndroidViewModel(application)
{
    private val dictionaryRepository = DictionaryRepository()

    fun swap(name:String, up:Boolean) = dictionaryRepository.swap(name, up)
    fun remove(name:String) = dictionaryRepository.remove(name)
}
