package com.ustory.asyncthreadsynchandle

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var handler = Handler()
        AsyncTaskSyncHandler.getInstance().enqueue(AsyncTaskSyncHandler.AsyncRunnableWrapper {
            onComplete ->
            Log.i("qiyue","实际任务运行.....")
            handler.postDelayed({
                onComplete()
            },3000)
        })


        handler.postDelayed({
            AsyncTaskSyncHandler.getInstance().enqueue(AsyncTaskSyncHandler.AsyncRunnableWrapper {
                    onComplete ->
                Log.i("qiyue","实际任务运行.....")
                handler.postDelayed({
                    onComplete()
                },2000)
            })

        },7000)


        handler.postDelayed({
            AsyncTaskSyncHandler.getInstance().stop()
            // 20秒回后停止
        },20000)
    }
}
