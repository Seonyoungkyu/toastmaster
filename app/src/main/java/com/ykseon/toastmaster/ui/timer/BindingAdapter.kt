package com.ykseon.toastmaster.ui.timer

import android.widget.ImageButton
import androidx.databinding.BindingAdapter
import com.ykseon.toastmaster.R

@BindingAdapter("startButtonImage")
fun bindStartButtonImage(imageView: ImageButton, state: TimerState) {
    if (state is TimerState.Initialized) imageView.setImageResource(R.drawable.ic_action_play)
    else if (state.paused) imageView.setImageResource(R.drawable.ic_action_play)
    else imageView.setImageResource(R.drawable.ic_action_pause)
}
