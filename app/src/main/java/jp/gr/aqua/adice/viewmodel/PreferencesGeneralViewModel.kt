package jp.gr.aqua.adice.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import jp.gr.aqua.adice.model.DictionaryRepository
import jp.gr.aqua.adice.model.DownloadRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PreferencesGeneralViewModel(application: Application): AndroidViewModel(application)
{
    private val downloadRepository = DownloadRepository()
    private val dictionaryRepository = DictionaryRepository()

    val downloadInProgress : MutableLiveData<Boolean> = MutableLiveData()
    val completed : MutableLiveData<Pair<Boolean,String>> = MutableLiveData()

    init{
        downloadInProgress.postValue(false)
        completed.postValue(null)
    }

    fun download(site: String, english:Boolean, defname : String)
    {
        viewModelScope.launch(Dispatchers.IO){
            downloadInProgress.postValue(true)
            val dicname = downloadRepository.downloadDicfile("http://$site")
            val result = dictionaryRepository.addDictionary(dicname, english, defname)
            downloadInProgress.postValue(false)
            completed.postValue(result)
        }
    }

    fun openDictionary(uri: Uri){
        viewModelScope.launch(Dispatchers.IO){
            downloadInProgress.postValue(true)
            val result = dictionaryRepository.openDictionary(uri)
            downloadInProgress.postValue(false)
            completed.postValue(result)
        }
    }

}
