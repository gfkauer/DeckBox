package com.r0adkll.deckbuilder.arch.data

import android.content.SharedPreferences
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.ftinc.kit.kotlin.extensions.Preferences
import com.ftinc.kit.kotlin.extensions.Preferences.*
import com.r0adkll.deckbuilder.arch.domain.features.cards.model.Expansion
import com.r0adkll.deckbuilder.util.extensions.RxPreferences
import com.r0adkll.deckbuilder.util.extensions.RxPreferences.ReactiveBasicEnergySetPreference
import com.r0adkll.deckbuilder.util.extensions.RxPreferences.ReactiveExpansionsPreference
import javax.inject.Inject


class AppPreferences @Inject constructor(
        override val sharedPreferences: SharedPreferences,
        override val rxSharedPreferences: RxSharedPreferences
) : Preferences, RxPreferences{

    companion object {
        const val KEY_ONBOARDING = "pref_onboarding"
        const val KEY_QUICKSTART = "pref_quickstart"
        const val KEY_EXPANSIONS = "pref_expansions"
        const val KEY_EXPANSIONS_TIMESTAMP = "pref_expansions_timestamp"
        const val KEY_DEFAULT_ENERGY_SET = "pref_default_energy_set"
        const val KEY_LAST_VERSION = "pref_last_version"
        const val KEY_DEVICE_ID = "pref_local_offline_device_id"
    }


    var onboarding by BooleanPreference(KEY_ONBOARDING, false)
    var quickStart by BooleanPreference(KEY_QUICKSTART, true)
    var lastVersion by IntPreference(KEY_LAST_VERSION, -1)
    var deviceId by StringPreference(KEY_DEVICE_ID)

    val expansions by ReactiveExpansionsPreference(KEY_EXPANSIONS)
    var expansionsTimestamp by LongPreference(KEY_EXPANSIONS_TIMESTAMP, 0L)

    val basicEnergySet by ReactiveBasicEnergySetPreference(KEY_DEFAULT_ENERGY_SET)


    fun clear() {
        sharedPreferences.edit()
                .clear()
                .apply()
    }
}