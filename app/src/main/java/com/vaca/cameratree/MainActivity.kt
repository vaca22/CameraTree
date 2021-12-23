package com.vaca.cameratree

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.vaca.cameratree.databinding.ActivityMainBinding

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding
    val yes=HashMap<String,Pfuck>()
    val yes2=HashMap<String,ImageView>()
    val gu= arrayListOf<ImageView>()
    val gu2= arrayListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        gu.add(binding.img4)
        gu.add(binding.img3)
        gu.add(binding.img2)
        gu.add(binding.img1)
        setContentView(binding.root)
        val nn = NioSocketServerInstance()
        var indexN=0;
        nn.tcpReceiveMsg = object : TcpReceiveMsg {
            override fun receive(port: String, msg: ByteArray) {
                if(!gu2.contains(port)){
                    gu2.add(port)
                    yes2[port] = gu[indexN]
                    indexN++
                    if(indexN>4){
                        indexN=0;
                    }
                    val yesx=Pfuck(port)
                    yesx.nx=object:FuckImg{
                        override fun yes(b: Bitmap,portx:String) {
                           // Log.e("fuckxxx",portx+b.width)
                            MainScope().launch {
                                yes2[portx]?.setImageBitmap(b)
                            }

                        }

                    }
                    yes.put(port,yesx)

                }
                yes[port]?.pp(msg)

            }
        }
        nn.initServer()
    }




}