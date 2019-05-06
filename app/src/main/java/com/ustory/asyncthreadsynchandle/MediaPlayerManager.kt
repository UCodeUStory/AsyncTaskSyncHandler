package com.ustory.asyncthreadsynchandle

import android.media.MediaPlayer


/**
 * 实现同时播放多个任务，我希望每一个播放完后再播放下一个，实现顺序播放
 *
 *
 */
class MediaPlayerManager(var mediaPlayer: MediaPlayer) {

    private var atshandler = AsyncTaskSyncHandler.getInstance()

    companion object {
        @Volatile
        private var instance: MediaPlayerManager? = null

        fun getInstance(mediaPlayer: MediaPlayer) =
            instance ?: synchronized(this) {
                instance ?: MediaPlayerManager(mediaPlayer).also { instance = it }
            }
    }


    fun play(voiceRunnable: VoiceRunnable) {

        var asyncRunnableWrapper =
            AsyncTaskSyncHandler.AsyncRunnableWrapper { onComplete ->
                playUnit(voiceRunnable, onComplete)
            }

        atshandler.enqueue(asyncRunnableWrapper)
    }

    /**
     * 根据具体的业务场景来编写
     */
    private fun playUnit(
        voiceRunnable: VoiceRunnable,
        onComplete: () -> Unit
    ) {
        var count = voiceRunnable.count
        mediaPlayer?.setOnCompletionListener {
            if (voiceRunnable.isLoop) {
                //判断有没有待处理的任务，有就结束当前的，没有继续执行循环
                if (atshandler.getSize() != 0) {
                    onComplete
                } else {
                    playUnit(voiceRunnable, onComplete)
                }
            } else {
                count--
                if (count == 0) {
                    onComplete
                } else {
                    playUnit(voiceRunnable, onComplete)
                }
            }
        }
        mediaPlayer?.start()

    }


    class VoiceRunnable(var count: Int, var type: String, var isLoop: Boolean)

}