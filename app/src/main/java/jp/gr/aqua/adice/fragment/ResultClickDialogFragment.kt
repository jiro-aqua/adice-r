package jp.gr.aqua.adice.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import jp.gr.aqua.adice.R

class ResultClickDialogFragment : DialogFragment()
{
    private val args by navArgs<ResultClickDialogFragmentArgs>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = args.title
        val disps = args.disps
        val items = args.items

        return AlertDialog.Builder(requireActivity())
                .setIcon(R.drawable.icon)
                .setTitle(title)
                .setItems(disps){ _, which ->
                    // selected dialog list item
                    setFragmentResult("linkClicked", bundleOf("link" to items[which]))
                }
                .create()
    }
}