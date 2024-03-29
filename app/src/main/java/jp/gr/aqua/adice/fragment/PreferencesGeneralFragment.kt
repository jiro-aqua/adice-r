package jp.gr.aqua.adice.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.CallSuper
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import jp.gr.aqua.adice.R
import jp.gr.aqua.adice.model.ContextModel
import jp.gr.aqua.adice.model.DictionaryRepository
import jp.gr.aqua.adice.model.PreferenceRepository
import jp.gr.aqua.adice.viewmodel.PreferencesGeneralViewModel

class PreferencesGeneralFragment : PreferenceFragmentCompat() {

    private val preferencesGeneralViewModel by lazy { ViewModelProvider(requireActivity())[PreferencesGeneralViewModel::class.java] }
    private val args by navArgs<PreferencesGeneralFragmentArgs>()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        // res/xml/preferences_general.xml ファイルに従って設定画面を構成
        setPreferencesFromResource(R.xml.preferences_general, rootKey)

        val openDocument = registerForActivityResult(OpenDocument()) {
            uri: Uri? ->
                uri?.let{
                    preferencesGeneralViewModel.openDictionary(uri)
                }
        }

        val adddic = findPreference<Preference>(PreferenceRepository.KEY_ADD_DICTIONARY)
        adddic?.setOnPreferenceClickListener {
            openDocument.launch(arrayOf("*/*"))
            true
        }

        val dldic = findPreference<Preference>(PreferenceRepository.KEY_DOWNLOAD_DICTIONARY)
        dldic?.setOnPreferenceClickListener {
            startDownload()
            true
        }

        if ( args.downloadnow ) {
            startDownload()
        }

        showDownloadProgress(false)
    }

    override fun onStart() {
        super.onStart()
        setFragmentResultListener("downloadResult") { _, bundle ->
            val url = bundle.getString("url")
            url?.let{
                val uri = Uri.parse(url)
                if ( uri.scheme == "adicer" && uri.host == "install" ){
                    val site = uri.getQueryParameter("site")
                    val english = uri.getQueryParameter("english") == "true"
                    val defname = uri.getQueryParameter("name")?:""
                    if ( site!= null && site.isNotEmpty()) {
                        preferencesGeneralViewModel.download(site, english, defname)
                    }
                }
            }
        }
        preferencesGeneralViewModel.downloadInProgress.observe(this, {
            it?.let{
                showDownloadProgress(it)
            }
        })
        preferencesGeneralViewModel.completed.observe(this, {
            it?.let{
                (result,dicname)->
                preferencesGeneralViewModel.completed.value = null

                if ( result ){
                    // 設定画面更新
                    createDictionaryPreference()
                    Toast.makeText(requireActivity(),
                            ContextModel.resources.getString(R.string.toastadded, dicname),
                            Toast.LENGTH_LONG).show()

                }else{
                    Toast.makeText(requireActivity(),
                            ContextModel.resources.getString(R.string.toasterror, dicname),
                            Toast.LENGTH_LONG).show()

                }
            }
        })

    }
    override fun onResume() {
        super.onResume()
        createDictionaryPreference()
    }

    private fun startDownload() = findNavController().navigate(R.id.action_preference_general_to_install)

    private fun showDownloadProgress(visible:Boolean)
    {
        findPreference<Preference>(PreferenceRepository.KEY_ADD_DICTIONARY)?.isVisible = !visible
        findPreference<Preference>(PreferenceRepository.KEY_DOWNLOAD_DICTIONARY)?.isVisible = !visible
        findPreference<Preference>(PreferenceRepository.KEY_PROGRESS)?.isVisible = visible
    }

    private fun createDictionaryPreference()
    {
        // 辞書一覧取得
        val diclist = DictionaryRepository().getDicList()
        val catdic = findPreference<PreferenceCategory>("managementcategory")
        catdic!!.removeAll()

        diclist.forEachIndexed {
            idx, dicinfo ->
            val name = dicinfo.GetFilename()

            if (name.isNotEmpty()) {
                // 辞書
                val psdic = Preference(requireActivity())
                psdic.key = name

                val dicname = PreferenceRepository().getDicName(name)
                psdic.title = dicname
                psdic.isIconSpaceReserved = false

                psdic.setOnPreferenceClickListener {
                    // 設定用の Fragment を表示
                    val action = PreferencesGeneralFragmentDirections.actionPreferenceGeneralToPreferenceDictionary(name, idx)
                    findNavController().navigate(action)
                    true
                }
                // preferences_general をカテゴリに追加
                catdic.addPreference(psdic)

                // TODO: 辞書削除時のy/n確認
            }
        }
    }

    private class OpenDocument : ActivityResultContract<Array<String>, Uri>() {

        @CallSuper
        override fun createIntent(context: Context, input: Array<String>): Intent {
            return Intent(Intent.ACTION_OPEN_DOCUMENT)
                    .setType("*/*")
        }

        override fun getSynchronousResult(context: Context,
                                          input: Array<String>): SynchronousResult<Uri>? {
            return null
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return if (intent == null || resultCode != Activity.RESULT_OK) null else intent.data
        }
    }

}