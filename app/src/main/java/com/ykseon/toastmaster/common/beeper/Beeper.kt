package com.ykseon.toastmaster.common.beeper

import android.content.Context
import android.media.MediaPlayer

class Beeper constructor(context: Context, id: Int){

    private var player: MediaPlayer

    init {
        player = MediaPlayer.create(context, id)
    }

    fun play() {
        player.seekTo(0)
        player.start()
    }
}