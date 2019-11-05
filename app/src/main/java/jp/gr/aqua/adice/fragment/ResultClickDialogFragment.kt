package jp.gr.aqua.adice.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import jp.gr.aqua.adice.R
import jp.gr.aqua.adice.viewmodel.ResultClickDialogViewModel

class ResultClickDialogFragment : DialogFragment()
{
    private val args by navArgs<ResultClickDialogFragmentArgs>()
    private val resultClickDialogViewModel by lazy { ViewModelProviders.of(requireActivity()).get(ResultClickDialogViewModel::class.java) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = args.title
        val disps = args.disps
        val items = args.items

        return AlertDialog.Builder(requireActivity())
                .setIcon(R.drawable.icon)
                .setTitle(title)
                .setItems(disps){ _, which ->
                    // selected dialog list item
                    resultClickDialogViewModel.linkClicked.postValue(items[which])
                }
                .create()
    }
}