package jp.gr.aqua.adice.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import jp.gr.aqua.adice.model.ResultModel
import jp.gr.aqua.adice.model.SearchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch

@InternalCoroutinesApi
class AdiceViewModel(application: Application): AndroidViewModel(application)
{
    private val searchRepository = SearchRepository()

    val searchWord : MutableLiveData<String> = MutableLiveData()
    val resultData : MutableLiveData<ResultData> = MutableLiveData()

    data class ResultData(
            val result: List<ResultModel>,
            val resetScroll : Boolean,
            val loseFocus : Boolean
    )

    init
    {
        viewModelScope.launch(Dispatchers.IO){
            searchRepository.initialize()
        }
    }

    fun startPage(){
        viewModelScope.launch(Dispatchers.IO) {
            searchRepository.startPage().let {
                resultData.postValue(ResultData(it, true, false))
            }
        }
    }

    fun search(text:String, loseFocus : Boolean = false){
        viewModelScope.launch(Dispatchers.IO) {
            searchWord.postValue(text)
            searchRepository.search(text)?.let{
                resultData.postValue(ResultData(it, true, loseFocus))
            }
        }
    }

    fun more(position :Int){
        viewModelScope.launch(Dispatchers.IO) {
            searchRepository.more(resultData.value?.result!!, position)?.let{
                resultData.postValue(ResultData(it, false, false))
            }
        }
    }

    fun onResume() = searchRepository.applySettings()
    fun pushHistory() = searchRepository.pushHistory()
    fun popHistory() : CharSequence? = searchRepository.popHistory()
}