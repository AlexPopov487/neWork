package com.example.netologydiploma.api

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Instant
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApiServiceModule {

    companion object {
        const val BASE_URL = "https://net-diploma.herokuapp.com/api/"
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(authPrefs: SharedPreferences): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .addInterceptor { chain ->
            authPrefs.getString("token", null)?.let { token ->
                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", token)
                    .build()
                return@addInterceptor chain.proceed(newRequest)
            }
            chain.proceed(chain.request())
        }
        .build()


    // gson Date converter as suggested by
    // https://stackoverflow.com/a/46726037/13924310
    // see also https://stackoverflow.com/a/40157532/13924310
    @Singleton
    @Provides
    fun provideGson(): Gson = GsonBuilder()
        .registerTypeAdapter(Instant::class.java, object : TypeAdapter<Instant>() {
            override fun write(out: JsonWriter?, value: Instant?) {
                out?.value(value.toString())
            }

            override fun read(`in`: JsonReader?): Instant {
                return Instant.parse(`in`?.nextString())
            }
        })
        .enableComplexMapKeySerialization()
        .create()


    @Singleton
    @Provides
    fun provideRetrofit(okhttp: OkHttpClient, gson: Gson): Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create(gson))
        .baseUrl(BASE_URL)
        .client(okhttp)
        .build()

    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)


}