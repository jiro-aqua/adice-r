package jp.gr.aqua.adice.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import jp.gr.aqua.adice.R
import jp.gr.aqua.adice.databinding.FragmentAdiceBinding
import jp.gr.aqua.adice.model.PreferenceRepository
import jp.gr.aqua.adice.model.ResultModel
import jp.gr.aqua.adice.view.ResultView
import jp.gr.aqua.adice.viewmodel.AdiceViewModel
import kotlinx.android.synthetic.main.fragment_adice.*
import kotlinx.coroutines.InternalCoroutinesApi
import java.util.*

@InternalCoroutinesApi
class AdiceFragment : Fragment()
{
    private val viewModel by lazy { ViewModelProvider(requireActivity()).get(AdiceViewModel::class.java) }
    private val resultData = ArrayList<ResultModel>()
    private val args by navArgs<AdiceFragmentArgs>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        val binding = DataBindingUtil.inflate<FragmentAdiceBinding>( inflater, R.layout.fragment_adice, container, false)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        val dicAdapter = ResultView.ResultAdapter(resultData, resultClickListener)

        viewModel.searchWord.observe(this , Observer{
            if ( editSearchWord.text.toString() != it ) {
                editSearchWord.setText(it)
            }
        })
        viewModel.resultData.observe(this, Observer {
            (results,resetScroll)->
            resultData.clear()
            results.mapTo(resultData){ it }
            dicAdapter.notifyDataSetChanged()
            if ( resetScroll ){
                dicView.scrollToPosition(0)
            }
            dicView.requestFocus()
        })
        setFragmentResultListener("linkClicked") { _, bundle ->
            val link = bundle.getString("link")
            link?.let{
                searchForward(link)
            }
        }

        dicView.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = dicAdapter
        }

        editSearchWord.apply{
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(editable: Editable) { }
                override fun beforeTextChanged(charsequence: CharSequence, i: Int, j: Int, k: Int) { }
                override fun onTextChanged(charsequence: CharSequence, i: Int, j: Int, k: Int) {
                    viewModel.search(charsequence.toString())
                }
            })
            setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                    val text = editSearchWord.text.toString()
                    viewModel.search(text)
                }
                false
            }
        }

        buttonClear.setOnClickListener {
            viewModel.pushHistory()
            editSearchWord.setText("")
            editSearchWord.requestFocus()
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, object:OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                Log.d("====>", "Backpressed!")
                var cs : CharSequence?
                do{
                    cs = viewModel.popHistory()
                }while( cs != null && editSearchWord.text.toString() == cs.toString() )
                cs?.let { editSearchWord.setText(it) } ?: finish()
            }
        })

        args.text.let{
            if (it.isNotEmpty()) {
                editSearchWord.setText(it)
            }
        }

        if (PreferenceRepository().isVersionUp()) {
            findNavController().navigate(R.id.action_main_to_welcome_dialog)
        }
        val text = editSearchWord.text.toString()
        if ( text.isEmpty() ){
            viewModel.startPage()
            editSearchWord.requestFocus()
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.pushHistory()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
        val text = editSearchWord.text.toString()
        viewModel.search(text)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.mainmenu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.preferences) {
            // 設定画面呼び出し
            val action =
                    AdiceFragmentDirections.actionMainToPreferenceGeneral(false)
            findNavController().navigate(action)
            return true
        }
        if (id == R.id.help) {
            // About画面呼び出し
            findNavController().navigate(R.id.action_main_to_about)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun searchForward(word: String)
    {
        if ( editSearchWord.text.toString() != word ) {
            viewModel.pushHistory()
            editSearchWord.setText(word)
        }
    }

    private val resultClickListener = object : ResultView.ResultClickListener {
        override fun onResultClicked(view:View, position: Int) {
            val data = resultData[position]
            when (data.mode) {
                ResultModel.Mode.MORE -> {
                    viewModel.more(position)
                }
                ResultModel.Mode.WORD -> {
                    val (disps,items) = data.links()
                    if (disps.size == 1) {
                        searchForward(items[0])
                    } else if (disps.size > 1) {
                        val title : String = data.index!!.toString()
                        val action =
                                AdiceFragmentDirections.actionMainToResultclickDialog(title, disps, items)
                        try {
                            findNavController().navigate(action)
                        }catch (e:Exception){}
                    }
                }
                else->{}
            }
        }

        override fun onResultLongClicked(view:View, position: Int): Boolean {
            val data = resultData[position]
            when (data.mode) {
                ResultModel.Mode.WORD -> {
                    // selected dialog list item
                    val all = data.allText()
                    val title : String = data.index!!.toString()
                    val action =
                            AdiceFragmentDirections.actionMainToLongclickDialog(title, all)
                    findNavController().navigate(action)
                }
                else->{}
            }
            return false
        }
    }

    private fun finish()
    {
        requireActivity().finish()
    }
}