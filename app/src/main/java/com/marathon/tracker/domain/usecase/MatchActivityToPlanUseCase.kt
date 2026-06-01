package com.marathon.tracker.domain.usecase

import com.marathon.tracker.domain.model.DayWorkout
import com.marathon.tracker.domain.model.RunType
import com.marathon.tracker.domain.model.StravaActivity
import javax.inject.Inject
import kotlin.math.abs

class MatchActivityToPlanUseCase @Inject constructor() {
    operator fun invoke(activity: StravaActivity, plan: DayWorkout): Boolean {
        if (plan.runType == RunType.REST || plan.runType == RunType.GYM_ONLY) return false
        if (activity.type != "Run") return false
        if (plan.distanceKm == 0.0) return false
        return abs(activity.distanceKm - plan.distanceKm) <= 3.0
    }
}
