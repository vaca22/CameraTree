package com.vaca.cameratree

interface TcpReceiveMsg {
    fun receive(port:Int,msg:ByteArray)
}