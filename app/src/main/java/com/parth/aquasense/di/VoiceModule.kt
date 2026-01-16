package com.parth.aquasense.di

import android.content.Context
import com.parth.aquasense.util.AudioPlaybackManager
import com.parth.aquasense.util.SpeechRecognitionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VoiceModule {

    @Provides
    @Singleton
    fun provideSpeechRecognitionManager(
        @ApplicationContext context: Context
    ): SpeechRecognitionManager {
        return SpeechRecognitionManager(context)
    }

    @Provides
    @Singleton
    fun provideAudioPlaybackManager(
        @ApplicationContext context: Context
    ): AudioPlaybackManager {
        return AudioPlaybackManager(context)
    }
}
