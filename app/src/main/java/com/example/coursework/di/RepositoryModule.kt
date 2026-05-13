package com.example.coursework.di

import com.example.coursework.data.repository.RunRepositoryImpl
import com.example.coursework.data.repository.RunTypeRepositoryImpl
import com.example.coursework.data.repository.UserPreferencesRepositoryImpl
import com.example.coursework.data.repository.WeatherRepositoryImpl
import com.example.coursework.domain.repository.RunRepository
import com.example.coursework.domain.repository.RunTypeRepository
import com.example.coursework.domain.repository.UserPreferencesRepository
import com.example.coursework.domain.repository.WeatherRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Tells Hilt how to satisfy requests for repository interfaces by mapping each one to its
 * concrete implementation. ViewModels then depend only on the interfaces in the domain layer and
 * never reach for the data layer directly, which keeps the architecture testable and lets
 * implementations be swapped out without touching the rest of the app.
 *
 * The class is abstract because @Binds methods have no body — Hilt generates the wiring at
 * compile time, so there's nothing for us to implement.
 *
 * @InstallIn(SingletonComponent::class) attaches these bindings to the application-wide component,
 * meaning they exist for as long as the app process is alive rather than being tied to a single
 * Activity or screen.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // @Binds is preferred over @Provides here because we're just telling Hilt "when something asks
    // for this interface, hand it that implementation." There's no construction logic to write —
    // the implementation's own @Inject constructor already tells Hilt how to build it,
    // so @Binds is the lighter option and generates less code than a @Provides method would.
    //
    // @Singleton means Hilt creates a single instance per app process and reuses it everywhere it's injected.
    // We want every ViewModel talking to the same instance rather than each getting its own copy.
    @Binds
    @Singleton
    abstract fun bindRunTypeRepository(impl: RunTypeRepositoryImpl): RunTypeRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindRunRepository(impl: RunRepositoryImpl): RunRepository

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(impl: WeatherRepositoryImpl): WeatherRepository
}
