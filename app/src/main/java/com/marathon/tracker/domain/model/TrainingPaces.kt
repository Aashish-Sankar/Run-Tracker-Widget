package com.marathon.tracker.domain.model

object TrainingPaces {
    val EASY = PaceRange(6 * 60 + 45, 8 * 60 + 0)
    val LONG_RUN = PaceRange(6 * 60 + 45, 7 * 60 + 45)
    val HM_RACE = PaceRange(6 * 60 + 9, 6 * 60 + 23)
    val RACE_25K = PaceRange(6 * 60 + 30, 6 * 60 + 36)
    val MARATHON = PaceRange(6 * 60 + 24, 6 * 60 + 24)
    val TEMPO = PaceRange(5 * 60 + 45, 6 * 60 + 20)
    val SPEED_REPS = PaceRange(5 * 60 + 5, 5 * 60 + 40)
    val STRIDES = PaceRange(4 * 60 + 50, 5 * 60 + 10)
    val RECOVERY = PaceRange(7 * 60 + 0, 8 * 60 + 30)
    val MARATHON_PACE_PROGRESSIVE = PaceRange(6 * 60 + 10, 6 * 60 + 45)
}
