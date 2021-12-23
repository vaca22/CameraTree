package com.vaca.cameratree

import android.graphics.BitmapFactory
import android.util.Log
import com.vaca.cameratree.utils.CRCUtils
import com.vaca.cameratree.utils.toUInt
import kotlinx.coroutines.sync.Mutex
import java.io.ByteArrayInputStream
import kotlin.experimental.inv

class Pfuck(private val port: String) {
    private var pool: ByteArray? = null
    var nx: FuckImg? = null
    fun pp(b: ByteArray) {
        pool = add(pool, b)
        pool = poccessLinkData(pool)
    }

    private fun add(ori: ByteArray?, add: ByteArray): ByteArray {
        if (ori == null) {
            return add
        }

        val new: ByteArray = ByteArray(ori.size + add.size)
        for ((index, value) in ori.withIndex()) {
            new[index] = value
        }

        for ((index, value) in add.withIndex()) {
            new[index + ori.size] = value
        }

        return new
    }

    private fun poccessLinkData(pool: ByteArray?): ByteArray? {
        var bytes = pool
        while (true) {
            if (bytes == null || bytes.size < 11) {
                break
            }
            var con = false

            loop@ for (i in 0 until bytes!!.size - 10) {
                if (bytes[i] != 0xA5.toByte() || bytes[i + 1] != bytes[i + 2].inv()) {
                    continue@loop
                }

                // need content length
                val len = toUInt(bytes.copyOfRange(i + 6, i + 10))
                if (len < 0) {
                    continue@loop
                }
                if (i + 11 + len > bytes.size) {
                    continue@loop
                }

                val temp: ByteArray = bytes.copyOfRange(i, i + 11 + len)
                if (temp.last() == CRCUtils.calCRC8(temp)) {
                    onResponseReceived(Response(temp))
                    val tempBytes: ByteArray? =
                        if (i + 11 + len == bytes.size) null else bytes.copyOfRange(
                            i + 11 + len,
                            bytes.size
                        )

                    bytes = tempBytes
                    con = true
                    break@loop
                }else{

                }
            }
            if (!con) {
                return bytes
            } else {
                con = false
            }

        }
        return null
    }

    private fun onResponseReceived(x: Response) {
        val bb = x.content.clone()
        val fg = BitmapFactory.decodeStream(ByteArrayInputStream(bb))
        if (fg != null) {
            nx?.yes(fg, port)
        }
    }


}