package com.example.coroutinesplayground.coroutines

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*

class StateFlowRunner {
    fun launchStateFlowRunner() {
        runBlocking {
            launchStateFlow()
        }
    }

    private suspend fun launchSharedFlow() {
        val sharedFlow = MutableSharedFlow<Int>(
            //replay = 3,
            onBufferOverflow = BufferOverflow.DROP_LATEST,
            extraBufferCapacity = 1
        )
        runBlocking {
            println("SharedFlows runBlocking")
            launch {
                for (i in 1..30) {
                    delay(30)
                    println("SharedFlows Emitting $i")
                    sharedFlow.emit(i)
                }
            }
            delay(200)
            sharedFlow
                .onEach {
                    println("SharedFlows Received in first $it")
                    delay(200)
                }
                .launchIn(this)

            sharedFlow
                .collect() {
                    println("SharedFlows Received in second $it")
                    delay(300)
                }
        }

    }

    private suspend fun launchStateFlow() {
        val stateFlow = MutableStateFlow(StateData(1, 4))
        var stateData = StateData(2,3)

        runBlocking {
            stateFlow
                .onEach {
                    println("SharedFlows collected state: ${stateFlow.value}")
                }
                .launchIn(this)

            delay(100)

            stateFlow.value = stateData
            delay(1)
            stateData.size = 20
            stateFlow.value = stateData
            delay(1)
            stateFlow.value = StateData(1,1)
//            for(i in 1..100) {
//                delay(1)
//                stateFlow.value = StateData(3,i)
//            }
        }


    }

    private data class StateData(
        var size: Int,
        val weight: Int
    )
}
