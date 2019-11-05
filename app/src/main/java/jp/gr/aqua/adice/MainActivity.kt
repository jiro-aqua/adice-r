package jp.gr.aqua.adice

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import jp.gr.aqua.adice.model.ContextModel
import kotlinx.coroutines.InternalCoroutinesApi

@InternalCoroutinesApi
class MainActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ContextModel.initialize(this)
        setContentView(R.layout.activity_main)

        val navController = findNavController(R.id.nav_host_fragment)
        setupActionBarWithNavController(navController)

        if ( savedInstanceState == null ) {
            val text = getWordFromIntent()
            val args = Bundle().apply { putString("text", text) }
            navController.navigate(R.id.adiceFragment, args)
        }
    }

    override fun onSupportNavigateUp() = findNavController(R.id.nav_host_fragment).navigateUp()

    private fun getWordFromIntent() : String
    {
        // intentからのデータ取得
        intent?.let{
            intent->
            // intentからのデータ取得
            when( intent.action ){
                Intent.ACTION_SEND->intent.extras?.getString(Intent.EXTRA_TEXT)
                Intent.ACTION_SEARCH->intent.extras?.getString(SearchManager.QUERY)
                else->null
            }?.let{
                val pos = it.indexOf("\n")
                return if (pos > 0) {
                    it.substring(0, pos)
                }else{
                    it
                }
            }
        }
        return ""
    }
}
