package jp.gr.aqua.adice.fragment

import jp.gr.aqua.adice.R
import jp.gr.aqua.adice.model.ContextModel

class InstallFragment : AboutFragment(){
    override fun pageUrl() = ContextModel.resources.getString(R.string.install_url)
}
