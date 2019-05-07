package com.ustory.asyncthreadsynchandle

import android.util.Log
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class AsyncTaskSyncHandler {
    private val queues: BlockingQueue<AsyncRunnableWrapper> = LinkedBlockingQueue(10)

    @Volatile
    private var isRunState = false

    @Volatile
    private var exeState: ExeState = ExeState.FINISH

    private var handleThread = HandleThread()

    companion object {
        @Volatile
        private var instance: AsyncTaskSyncHandler? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: AsyncTaskSyncHandler().also { instance = it }
            }
    }

    fun enqueue(runnable: AsyncRunnableWrapper) {
        queues.put(runnable)
        if (!isRunState) {
            isRunState = true
            handleThread.start()
        }
    }

    fun stop() {
        isRunState = false
        handleThread.interrupt()
    }

    /**
     * 任务的Size
     */
    fun getSize(): Int {
        return queues.size
    }

    inner class HandleThread : Thread() {
        override fun run() {
            while (!Thread.currentThread().isInterrupted) {
                try {
                    if (exeState == ExeState.FINISH) {
                        var runnable = queues.take()
                        exeState = ExeState.RUNNING
                        runnable.run {
                            exeState = ExeState.FINISH
                        }
                    }
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
            }
            isRunState = false
        }

    }


    enum class ExeState(var value: Int) {
        RUNNING(1),
        FINISH(0);
    }


    class AsyncRunnableWrapper(val realRunnable: (onComplete: () -> Unit) -> Unit) {
        fun run(onComplete: () -> Unit) {
            realRunnable(onComplete)
        }
    }

}
