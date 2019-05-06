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
            Log.i("qiyue", "程序开始执行》》》》")
            while (!Thread.currentThread().isInterrupted) {
//            while (isStart) {//可以记录程序是否运行
                try {
                    if (exeState == ExeState.FINISH) {
                        Log.i("qiyue", "开始运行")
                        //没有任务会阻塞在这里
                        var runnable = queues.take()
                        Log.i("qiyue", "取出任务")
                        exeState = ExeState.RUNNING
                        runnable.run {
                            Log.i("qiyue", "任务结束")
                            exeState = ExeState.FINISH
                        }
                    }
                } catch (e: InterruptedException) {
                    Log.i("qiyue", "程序被打断")
                    // 当程序本打断后打断状态会被重置，所以要重新设置一下，否则会死循环
                    // Thread.interrupted() 会被重置
                    Thread.currentThread().interrupt()
                }
            }
            isRunState = false
            Log.i("qiyue", "程序退出》》》》》》")
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
