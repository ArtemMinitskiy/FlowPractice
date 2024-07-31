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
//https://metanit.com/kotlin/tutorial/9.1.php
//https://medium.com/@mortitech/sharedflow-vs-stateflow-a-comprehensive-guide-to-kotlin-flows-503576b4de31
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var flow: Flow<Int>
    private val scope = CoroutineScope(Job() + Dispatchers.Main)
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
//            myAbcFlow.channelMerge(my123Flow).collect()

//            sharedFlow()
//            stateFlow()
//            errorHandling()
        }

        binding.apply {
            btnSimpleFlow.setOnClickListener {
                main()
            }
            btnFlowOf.setOnClickListener {
                scope.launch {
                    mainFlowOf()
                }
            }
            btnAsFlow.setOnClickListener {
                scope.launch {
                    mainAsFlow()
                }
            }
            btnCount.setOnClickListener {
                scope.launch {
                    mainCount()
                }
            }
            btnTake.setOnClickListener {
                scope.launch {
                    mainTake()
                }
            }
            btnDrop.setOnClickListener {
                scope.launch {
                    mainDrop()
                }
            }
            btnMap.setOnClickListener {
                scope.launch {
                    mainMap()
                }
            }
            btnTransform.setOnClickListener {
                scope.launch {
                    mainTransform()
                }
            }
            btnFilter.setOnClickListener {
                scope.launch {
                    mainFilter()
                }
            }
            btnTakeWhile.setOnClickListener {
                scope.launch {
                    mainTakeWhile()
                }
            }
            btnDropWhile.setOnClickListener {
                scope.launch {
                    mainDropWhile()
                }
            }
            btnReduce.setOnClickListener {
                scope.launch {
                    mainReduce()
                }
            }
            btnFold.setOnClickListener {
                scope.launch {
                    mainFold()
                }
            }
            btnZip.setOnClickListener {
                scope.launch {
                    mainZip()
                }
            }
        }


    }


    //Handle errors properly:
    //
    //When using flows, ensure that you handle exceptions correctly.
    //Use the catch operator to handle exceptions within the flow pipeline,
    //and the onCompletion operator to perform cleanup operations or react to the completion of the flow.
    private suspend fun errorHandling() {
        val flow = flow {
            emit(1)
            throw RuntimeException("Error occurred")
            emit(2)
        }.catch { e ->
            // Handle the exception and emit a default value
            Log.e("mLog", "Exception: $e")
            emit(-1)
        }.onCompletion {
            Log.i("mLog", "onCompletion")
        }
        CoroutineScope(Dispatchers.Main).launch {
            launch {
                flow.collect { value ->
                    Log.i("mLog", "Received: $value")
                }
            }
        }
    }

    //StateFlow:
    //
    //A StateFlow is a hot flow that represents a state, holding a single value at a time. It is also a conflated flow, meaning that when a new value is emitted, the most recent value is retained and immediately emitted to new collectors.
    //It is useful when you need to maintain a single source of truth for a state and automatically update all the collectors with the latest state.
    //It always has an initial value and only stores the latest emitted value.
    private suspend fun stateFlow() {
        val mutableStateFlow = MutableStateFlow(0)
        val stateFlow: StateFlow<Int> = mutableStateFlow
        CoroutineScope(Dispatchers.Main).launch {
            // Collect values from stateFlow
            launch {
                stateFlow.collect { value ->
                    Log.i("mLog", "Collector 1 received: $value")
                }
            }

            // Collect values from stateFlow
            launch {
                stateFlow.collect { value ->
                    Log.i("mLog", "Collector 2 received: $value")
                }
            }

            // Update the state
            launch {
                repeat(3) { i ->
                    Log.i("mLog", "i: $i")
//                    delay(1000)
                    mutableStateFlow.value = i
                }
            }
        }
    }

    //SharedFlow:
    //
    //A SharedFlow is a hot flow that can have multiple collectors. It can emit values independently of the collectors, and multiple collectors can collect the same values from the flow.
    //It’s useful when you need to broadcast a value to multiple collectors or when you want to have multiple subscribers to the same stream of data.
    //It does not have an initial value, and you can configure its replay cache to store a certain number of previously emitted values for new collectors.
    private suspend fun sharedFlow() {
        val sharedFlow = MutableSharedFlow<Int>()
        CoroutineScope(Dispatchers.Main).launch {
            // Collect values from sharedFlow
            launch {
                sharedFlow.collect { value ->
                    Log.i("mLog", "Collector 1 received: $value")
                }
            }

            // Collect values from sharedFlow
            launch {
                sharedFlow.collect { value ->
                    Log.i("mLog", "Collector 2 received: $value")
                }
            }

            // Emit values to sharedFlow
            launch {
                repeat(3) { i ->
                    sharedFlow.emit(i)
                }
            }
        }
    }

    //zip принимает два параметра. Первый параметр - поток данных, с которым надо выполнить объединение.
    //Второй параметр - собственно функция объединения.
    //Она принимает соответствующие элементы обоих потоков в качестве параметров и возвращает результат их объединения.
    private suspend fun mainZip() {
        val names = listOf("Tom", "Bob", "Sam").asFlow()
        val ages = listOf(37, 41, 25).asFlow()
        names.zip(ages) { name, age -> Person(name, age) }.collect { person -> println("Name: ${person.name}   Age: ${person.age}") }
    }

    //fold также сводит все элементы потока в один.
    //Но в отличие от оператора reduce оператор fold в качестве первого параметра принимает начальное значение
    private suspend fun mainFold() {
        val userFlow = listOf("Tom", "Bob", "Kate", "Sam", "Alice").asFlow()
        val foldedValue = userFlow.fold("Users:") { a, b -> "$a $b" }
        println(foldedValue)   // Users: Tom Bob Kate Sam Alice
    }

    //reduce принимает функцию, которая имеет два параметра.
    //Первый параметр при первом запуске представляет первый объект потока, а при последующих запусках - результат функции над предыдущими объектами.
    //А второй параметр функции - следующий объект.
    private suspend fun mainReduce() {
        val numberFlow = listOf(1, 2, 3, 4, 5).asFlow()
        val reducedValue = numberFlow.reduce { a, b -> a + b }
        println(reducedValue)   // 15
    }

    //takeWhile выбирает из потока элементы, пока будет истино некоторое условие
    private suspend fun mainTakeWhile() {
        val peopleFlow = listOf(
            Person("Tom", 37),
            Person("Alice", 32),
            Person("Bill", 5),
            Person("Sam", 14),
            Person("Bob", 25),
        ).asFlow()

        peopleFlow.takeWhile { person -> person.age > 17 }.collect { person -> println("name: ${person.name}   age:  ${person.age} ") }
    }

    //dropWhile удаляет из потока элементы, пока они не начнут соответствовать некоторому условию
    private suspend fun mainDropWhile() {
        val peopleFlow = listOf(
            Person("Tom", 37),
            Person("Alice", 32),
            Person("Bill", 5),
            Person("Sam", 14),
            Person("Bob", 25),
        ).asFlow()

        peopleFlow.dropWhile { person -> person.age > 17 }.collect { person -> println("name: ${person.name}   age:  ${person.age} ") }
    }

    //filter выполняет фильтрацию объектов в потоке.
    //В качестве параметра он принимает функцию-условие, которая получает объект потока и возвращает true (если объект проходит фильтрацию) и false (если не проходит)
    private suspend fun mainFilter() {
        val peopleFlow = listOf(
            Person("Tom", 37),
            Person("Bill", 5),
            Person("Sam", 14),
            Person("Bob", 21),
        ).asFlow()

        peopleFlow.filter { person -> person.age > 17 }.collect { person -> println("name: ${person.name}   age:  ${person.age} ") }
    }

    //transform также позволяет выполнять преобразование объектов в потоке.
    //В отличие от map она позволяет использовать функцию emit(), чтобы передавать в поток произвольные объекты.
    private suspend fun mainTransform() {
        val peopleFlow = listOf(
            Person("Tom", 37),
            Person("Bill", 5),
            Person("Sam", 14),
            Person("Bob", 21),
        ).asFlow()

        peopleFlow.transform { person ->
            if (person.age > 17) {
                emit(person.name)
            }
        }.collect { personName -> println(personName) }
    }

    //map() преобразует данные потока.
    //В качестве параметра он принимает функцию преобразования. Функция преобразования принимает в качестве единственного параметра объект из потока и возвращает преобразованные данные.
    private suspend fun mainMap() {
        val peopleFlow = listOf(Person("Tom", 37), Person("Sam", 41), Person("Bob", 21)).asFlow()
        peopleFlow.map { person -> person.name }.collect { personName -> println(personName) }
    }

    data class Person(val name: String, val age: Int)

    //drop удаляет из потока определенное количество элементов
    private suspend fun mainDrop() {
        val userFlow = listOf("Tom", "Bob", "Kate", "Sam", "Alice").asFlow()
        userFlow.drop(3).collect { user -> println(user) }
    }

    //take ограничивает количество элементов в потоке
    private suspend fun mainTake() {
        val userFlow = listOf("Tom", "Bob", "Kate", "Sam", "Alice").asFlow()
        userFlow.take(3).collect { user -> println(user) }
    }

    //count получает количество объектов в потоке
    private suspend fun mainCount() {
        val userFlow = listOf("Tom", "Bob", "Sam").asFlow()
        println("Count: ${userFlow.count()}")       // Count: 3
    }

    private suspend fun mainAsFlow() {
        // преобразование последовательности в поток
        val numberFlow: Flow<Int> = (1..5).asFlow()
        numberFlow.collect { n -> println(n) }

        // преобразование коллекции List<String> в поток
        val userFlow = listOf("Tom", "Sam", "Bob").asFlow()
        userFlow.collect({ user -> println(user) })
    }

    private suspend fun mainFlowOf() {
        val numberFlow: Flow<Int> = flowOf(1, 2, 3, 5, 8)
        numberFlow.collect { n -> println(n) }
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
//        binding.button.setOnClickListener {
        CoroutineScope(Dispatchers.Main).launch {
            flow.collect {
                Log.d("mLog", it.toString())
            }
        }
//        }
    }
}