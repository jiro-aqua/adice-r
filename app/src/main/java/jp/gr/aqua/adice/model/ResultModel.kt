package jp.gr.aqua.adice.model

import android.graphics.Typeface
import java.lang.StringBuilder
import java.util.ArrayList
import java.util.regex.Pattern

class ResultModel(val mode: Int, val dic: Int) {       //todo data class
    var Index: CharSequence? = null
    var Phone: CharSequence? = null
    var Trans: CharSequence? = null
    var Sample: CharSequence? = null

    var IndexFont: Typeface? = null
    var PhoneFont: Typeface? = null
    var TransFont: Typeface? = null
    var SampleFont: Typeface? = null
    var IndexSize: Int = 0
    var PhoneSize: Int = 0
    var TransSize: Int = 0
    var SampleSize: Int = 0

    fun allText() : String{
        val all = StringBuilder()
        all.append(Index!!.toString())
        Trans?.let{
            all.append("\n")
            all.append(it)
        }
        Sample?.let{
            all.append("\n")
            all.append(it)
        }
        all.append("\n")
        return all.toString()
    }

    fun links(): Pair<Array<String>,Array<String>>
    {
        val items = ArrayList<String>()
        val disps = ArrayList<String>()

        Trans?.let{
            trans->
            // <→リンク> 英辞郎形式
            run {
                val p = Pattern.compile("<(→(.+?))>")
                val m = p.matcher(trans)

                while (m.find()) {
                    disps.add(m.group(1)!!)
                    items.add(m.group(2)!!)
                }
            }
            // "→　" 和英辞郎形式
            run {
                val p = Pattern.compile("(→　(.+))")
                val m = p.matcher(trans)

                while (m.find()) {
                    disps.add(m.group(1)!!)
                    items.add(m.group(2)!!)
                }
            }

            // "＝リンク●" 略辞郎形式
            run {
                val p = Pattern.compile("(＝(.+))●")
                val m = p.matcher(trans)

                while (m.find()) {
                    disps.add(m.group(1)!!)
                    val item = m.group(2)!!
                    if ( item.contains(";") ){
                        val split = item.split(";")
                        items.add(split[0])
                    }else{
                        items.add(item)
                    }
                }
            }
        }
        return disps.toTypedArray() to items.toTypedArray()
    }


    companion object {
        val WORD = 0
        val MORE = 1
        val NONE = 2
        val NORESULT = 3
        val FOOTER = 4
    }
}
