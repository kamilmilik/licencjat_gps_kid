package kamilmilik.licencjat_gps_kid.Utils

import android.util.Log
import java.util.*

/**
 * Created by kamil on 20.02.2018.
 */
object RandomIdGenerator {
    val TAG : String = "RandomIdGenerator"
    /**
     * @param base62chars possible char in uniq key
     */
    private val base62chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"

    private val random = Random()
    /**
     * generate unique key
     * @param length
     */
    fun getBase62(length: Int): String {
        Log.i(TAG,"getBase62: random Id generator")
        val sb = StringBuilder(length)
        for (i in 0 until length)
            sb.append(base62chars[random.nextInt(62)])
        return sb.toString()
    }

    /**
     * generate unique key
     * @param length
     */
    fun getBase36(length: Int): String {
        Log.i(TAG,"getBase36: random Id generator")
        val sb = StringBuilder(length)
        for (i in 0 until length)
            sb.append(base62chars[random.nextInt(36)])
        return sb.toString()
    }
}