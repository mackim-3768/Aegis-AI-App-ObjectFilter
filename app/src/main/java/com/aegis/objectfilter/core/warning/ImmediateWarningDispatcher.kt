package com.aegis.objectfilter.core.warning

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.aegis.objectfilter.core.driverreadiness.DriverReadinessState
import com.aegis.objectfilter.core.driverreadiness.DriverReadinessStatus

class ImmediateWarningDispatcher : AutoCloseable {

  private var toneGenerator: ToneGenerator? = null

  private var lastCriticalAtMs: Long = 0L
  private var lastUrgentAtMs: Long = 0L
  private var lastWarningAtMs: Long = 0L

  fun dispatch(context: Context, state: DriverReadinessState) {
    val nowMs = SystemClock.uptimeMillis()

    when (state.status) {
      DriverReadinessStatus.NOT_LOOKING -> {
        if (nowMs - lastCriticalAtMs < 1500L) return
        lastCriticalAtMs = nowMs
        vibrate(context, longArrayOf(0, 80, 60, 80))
        beep()
      }

      DriverReadinessStatus.DROWSY -> {
        if (nowMs - lastUrgentAtMs < 5000L) return
        lastUrgentAtMs = nowMs
        vibrate(context, longArrayOf(0, 120))
        beep()
      }

      DriverReadinessStatus.STARING -> {
        if (nowMs - lastWarningAtMs < 5000L) return
        lastWarningAtMs = nowMs
        vibrate(context, longArrayOf(0, 60))
      }

      else -> Unit
    }
  }

  private fun vibrate(context: Context, pattern: LongArray) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
      vm.defaultVibrator
    } else {
      @Suppress("DEPRECATION")
      context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    if (!vibrator.hasVibrator()) return

    val effect = VibrationEffect.createWaveform(pattern, -1)
    vibrator.vibrate(effect)
  }

  private fun beep() {
    ensureToneGenerator()
    toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)
  }

  private fun ensureToneGenerator() {
    if (toneGenerator != null) return
    toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 80)
  }

  override fun close() {
    toneGenerator?.release()
    toneGenerator = null
  }
}
