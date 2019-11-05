package jp.gr.aqua.adice.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import jp.gr.aqua.adice.R

class WelcomeDialogFragment : DialogFragment()
{
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireActivity())
                .setTitle(R.string.welcome_title)
                .setMessage(R.string.welcome_message)
                .setNegativeButton(R.string.label_close,null)
                .setPositiveButton(R.string.label_download) { _, _->
                    // 設定画面呼び出し
                    val action =
                            WelcomeDialogFragmentDirections.actionWelcomeDialogToPreferenceGeneral(true)
                    findNavController().navigate(action)
                }
                .create()
    }
}