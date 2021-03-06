package com.r0adkll.deckbuilder.arch.data.features.marketplace.api

import android.os.Build
import com.r0adkll.deckbuilder.BuildConfig
import com.r0adkll.deckbuilder.internal.di.scopes.AppScope
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

@Module
class TcgReplayerModule {

    @Provides @AppScope
    fun provideTcgReplayer(): TcgReplayer {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) Level.BASIC else Level.NONE
            })
            .addInterceptor {
                val userAgent = "DeckBox/${BuildConfig.VERSION_NAME} (${BuildConfig.GIT_SHA}, " +
                    "${Build.MANUFACTURER}, ${Build.MODEL}, ${Build.PRODUCT}, ${Build.VERSION.SDK_INT})"
                val request = it.request().newBuilder()
                    .header("User-Agent", userAgent)
                    .build()
                it.proceed(request)
            }
            .build()

        val retrofit = Retrofit.Builder()
            .client(client)
            .baseUrl(BuildConfig.MARKETPLACE_URL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(TcgReplayer::class.java)
    }
}
