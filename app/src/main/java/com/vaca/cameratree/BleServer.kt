package com.vaca.cameratree

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

object BleServer {
    val dataScope = CoroutineScope(Dispatchers.IO)
}