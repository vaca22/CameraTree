package com.vaca.cameratree

interface TcpReceiveMsg {
    fun receive(port:String,msg:ByteArray)
}