package jp.gr.aqua.adice.fragment

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import jp.gr.aqua.adice.R
import jp.gr.aqua.adice.model.DictionaryRepository
import jp.gr.aqua.adice.model.PreferenceRepository
import jp.gr.aqua.adice.model.PreferenceRepository.Companion.KEY_FILENAME
import jp.gr.aqua.adice.model.PreferenceRepository.Companion.KEY_MOVE_DOWN
import jp.gr.aqua.adice.model.PreferenceRepository.Companion.KEY_MOVE_UP
import jp.gr.aqua.adice.model.PreferenceRepository.Companion.KEY_REMOVE
import jp.gr.aqua.adice.viewmodel.PreferencesDictionaryViewModel
import kotlin.properties.Delegates

class PreferencesDictionaryFragment : PreferenceFragmentCompat()
{
    private val viewModel by lazy { ViewModelProviders.of(requireActivity()).get(PreferencesDictionaryViewModel::class.java) }

    private var filename by Delegates.notNull<String>()
    private var index by Delegates.notNull<Int>()

    private val args by navArgs<PreferencesDictionaryFragmentArgs>()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        filename = args.filename
        index = args.index
        Log.d("====>", "$filename/$index")

        preferenceManager.sharedPreferencesName = PreferenceRepository().dictionaryPreferenceName(filename)

        // res/xml/preferences_dictionary.xml ファイルに従って設定画面を構成
        setPreferencesFromResource(R.xml.preferences_dictionary, rootKey)

        // ファイル名を設定
        findPreference<Preference>(KEY_FILENAME)?.apply{
            summary = filename
        }

        // 上に移動ボタン
        findPreference<Preference>(KEY_MOVE_UP)?.apply{
            if ( index == 0 ){
                this.isVisible = false
            }else{
                onPreferenceClickListener = SwapDic(up = true)
            }
        }
        // 下に移動ボタン
        findPreference<Preference>(KEY_MOVE_DOWN)?.apply{
            if ( index == DictionaryRepository().getDicList().size - 1){
                this.isVisible = false
            }else{
                onPreferenceClickListener = SwapDic(up = false)
            }
        }
        // 削除ボタン
        findPreference<Preference>(KEY_REMOVE)?.apply{
            onPreferenceClickListener = RemoveDictionary()
        }
    }

    private inner class SwapDic(val up : Boolean) : Preference.OnPreferenceClickListener {
        override fun onPreferenceClick(preference: Preference): Boolean {
            // 該当する辞書を入れ替える
            viewModel.swap(filename , up)
            // 前の画面に
            findNavController().popBackStack()
            return true
        }
    }

    private inner class RemoveDictionary : Preference.OnPreferenceClickListener {
        override fun onPreferenceClick(preference: Preference): Boolean {
            // 辞書削除
            viewModel.remove(filename)

            Toast.makeText(requireActivity(),
                    resources.getString(R.string.toastremoved, filename),
                    Toast.LENGTH_LONG).show()

            // 前の画面に
            findNavController().popBackStack()
            return true
        }
    }
}