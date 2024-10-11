package com.minimalisttodolist.pleasebethelastrecyclerview.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator


fun vibrate(context: Context, strength: Int) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val effect = when (strength) {
            1 -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
            2 -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
            3 -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
            4 -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
            else -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
        }
        vibrator.vibrate(effect)
    } else {
        val duration = when (strength) {
            1 -> 50L
            2 -> 100L
            3 -> 150L
            4 -> 200L
            else -> 100L
        }
        vibrator.vibrate(duration)
    }
}

