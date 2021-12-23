package com.vaca.cameratree

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.vaca.cameratree.BleServer.dataScope
import kotlinx.coroutines.launch
import java.net.ServerSocket

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val nn=NioSocketServerInstance()
        nn.tcpReceiveMsg=object :TcpReceiveMsg{
            override fun receive(port: Int, msg: ByteArray) {
                Log.e("fuck","$port"+"         "+ String(msg))
            }
        }
        nn.initServer()
    }
}