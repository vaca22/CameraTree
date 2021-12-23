package com.vaca.cameratree.utils





fun toUInt(bytes: ByteArray): Int {
    var result = 0
    for ((i, v) in bytes.withIndex()) {
        result += v.unsigned().shl(i * 8)
    }
    return result
}


fun Byte.unsigned(): Int = when {
    (toInt() < 0) -> 255 + toInt() + 1
    else -> toInt()
}