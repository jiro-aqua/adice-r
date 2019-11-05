package jp.gr.aqua.adice.fragment

import android.app.Dialog
import android.content.*
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import jp.gr.aqua.adice.R
import jp.gr.aqua.adice.model.ContextModel

class ResultLongClickDialogFragment : DialogFragment()
{
    private val clipboardManager by lazy { requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
    private val args by navArgs<ResultLongClickDialogFragmentArgs>()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = args.title
        val all = args.all

        val items: Array<CharSequence> = arrayOf(
                ContextModel.resources.getString(R.string.menu_share),
                ContextModel.resources.getString(R.string.menu_copy_index),
                ContextModel.resources.getString(R.string.menu_copy_all))

        val listener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                0 -> {
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "text/plain"
                    intent.putExtra(Intent.EXTRA_TEXT, all)
                    try {
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                    }
                }
                1 -> {// copy index
                    val clip: ClipData = ClipData.newPlainText("text", title)
                    clipboardManager.setPrimaryClip(clip)
                }

                2 -> {// copy all
                    val clip: ClipData = ClipData.newPlainText("text", all)
                    clipboardManager.setPrimaryClip(clip)
                }
            }
        }

        return AlertDialog.Builder(requireActivity())
                .setIcon(R.drawable.icon)
                .setTitle(title)
                .setItems(items,listener)
                .create()
    }
}