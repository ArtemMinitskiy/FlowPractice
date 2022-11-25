package com.example.flowpractice

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.flowpractice.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

//https://medium.com/@myofficework000/kotlin-flows-for-beginners-69cacb712324
//https://medium.com/mobile-app-development-publication/kotlins-flow-channelflow-and-callbackflow-made-easy-5e82ce2e27c0

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var flow: Flow<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        setUpFlows()
//        setUpClickEvent()
//        main()
//        main2()
//        main3()

        CoroutineScope(Dispatchers.Main).launch {
//            my123Flow.collect { Log.i("mLog", "Emitting : $it") }
//            myAbcFlow.collect { Log.i("mLog", "Emitting : $it") }
            myAbcFlow.channelMerge(my123Flow).collect()
        }
    }

    private fun main3(): Unit = runBlocking {
        var sendData: (data: Int) -> Unit = { } // Not suspending
        var closeChannel: () -> Unit = { }

        launch {
            channelFlow {
                for (i in 1..5) trySend(i)
                sendData = { data -> trySend(data) }
                closeChannel = { close() }
                awaitClose {
                    sendData = {}
                    closeChannel = {}
                }
            }.collect { Log.i("mLog", "$it") }
        }

        delay(10)
        Log.i("mLog", "Sending 6")
        sendData(6)
        closeChannel()
        sendData(7)
    }

    private fun main2(): Unit = runBlocking {
        var sendData: suspend (data: Int) -> Unit = { }
        var closeChannel: () -> Unit = { }

        launch {
            channelFlow {
                for (i in 1..5) send(i)
                sendData = { data -> send(data) }
                closeChannel = { close() }
                awaitClose {
                    sendData = {}
                    closeChannel = {}
                }
            }.collect { Log.i("mLog", "$it") }
        }

        delay(10)
        Log.i("mLog", "Sending 6")
        sendData(6)
        closeChannel()

    }

    private fun main() = runBlocking {
        flow {
            Log.i("mLog", "Flow started")
            for (i in 1..5) {
                Log.i("mLog", "Emitting : $i")
                emit(i)
            }
        }.collect { value ->
            delay(100)
            Log.i("mLog", "Consuming $value")
        }
    }

    //MergeFlow
    private val myAbcFlow = flow {
        ('A'..'E').forEach {
            delay(50)
            emit(it)
        }
    }

    private val my123Flow = flow {
        (1..5).forEach {
            delay(50)
            emit(it)
        }
    }

    private fun <T> Flow<T>.channelMerge(other: Flow<T>): Flow<T> = channelFlow {
        Log.i("mLog", "channelMerge")
        launch {
            collect {
                Log.i("mLog", "Emitting : $it")
                send(it)
            }
        }
        other.collect {
            Log.i("mLog", "Emitting : $it")
            send(it)
        }
    }

    /*
This method is producer of stream data in flow*/
    private fun setUpFlows() {
        flow = flow {
            Log.i("mLog", "Flow started")
            (0..15).forEach {
                // Emit items with 300 milliseconds delay
                delay(1500)
                Log.i("mLog", "Flow emitting: $it")
                emit(it)
            }
        }.flowOn(Dispatchers.Default)
    }

    /*
This method is responsible for consuming of data in
 Flow of stream*/
    private fun setUpClickEvent() {
        binding.button.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                flow.collect {
                    Log.d("mLog", it.toString())
                }
            }
        }
    }
}