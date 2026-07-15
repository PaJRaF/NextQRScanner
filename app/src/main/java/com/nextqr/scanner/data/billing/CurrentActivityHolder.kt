package com.nextqr.scanner.data.billing

import android.app.Activity
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds a weak reference to the currently-resumed Activity. Play Billing and
 * AdMob interstitial/rewarded flows require an Activity but our repositories are
 * process-scoped singletons — this bridges the two without leaking the Activity.
 */
@Singleton
class CurrentActivityHolder @Inject constructor() {
    private var ref: WeakReference<Activity> = WeakReference(null)

    fun set(activity: Activity?) {
        ref = WeakReference(activity)
    }

    val activity: Activity? get() = ref.get()
}
