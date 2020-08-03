package com.example.pictureoftheday.ui.main


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.GsonBuilder
import retrofit2.Callback
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException

data class PODServerResponseData(
    val data: String?,
    val explanation: String?,
    val hdurl: String?,
    val media_type: String?,
    val service_version: String?,
    val title: String?,
    val url: String?
)

sealed class PictureOfTheDayData {
    data class Success(val serverResponseData: PODServerResponseData) : PictureOfTheDayData()
    data class Error(val error: Throwable) : PictureOfTheDayData()
    data class Loading(val process: Int?) : PictureOfTheDayData()
}

interface PictureOfTheDayApi {
    @GET("planetary/apod")
    fun getPictureOfTheDay(
        @Query("api_key") apiKey: String)
            : Call<PODServerResponseData> //PODServerRespanseData>
}

class PODRetrofitImpl {
    private val baseURL = "https://api.nasa.gov/"

    fun getPODRetrofit(): PictureOfTheDayApi {
        val podRetrofit: Retrofit = Retrofit.Builder() // берем библиотеку retrofit для выхода в интернет
            .baseUrl(baseURL)       // передаем базовую ссылку
            .addConverterFactory(   // добавляем фабрику GSON
                GsonConverterFactory.create(
                    GsonBuilder().setLenient().create()
                )
            )
            .client(createOkHttpClient(PODInterceptor()))
            .build()
        return podRetrofit.create(PictureOfTheDayApi::class.java)
    }

    class PODInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            return chain.proceed(chain.request())
        }
    }

    private fun createOkHttpClient(interceptor: Interceptor): OkHttpClient {
        val httpClilent = OkHttpClient.Builder()
        httpClilent.addInterceptor(interceptor)
        httpClilent.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        return httpClilent.build()
    }
}

class MainViewModel(
    private val liveDataForViewToObserve: MutableLiveData<PictureOfTheDayData> = MutableLiveData(),
    private val retrofitImpl: PODRetrofitImpl = PODRetrofitImpl()): ViewModel() {

    fun getData(): LiveData<PictureOfTheDayData> {
        sendServerRequest()
        return liveDataForViewToObserve
    }

    private fun sendServerRequest() {
        liveDataForViewToObserve.value = PictureOfTheDayData.Loading(null)
        val apiKey = "DEMO_KEY"
        if (apiKey.isBlank()) {
            PictureOfTheDayData.Error(Throwable("You need API key"))
        } else {
            retrofitImpl.getPODRetrofit().getPictureOfTheDay(apiKey).enqueue(object:
         // retrofitImpl.getRetrofitImpl().getPictureOfTheDay(apiKey).enqueue(object:
            Callback<PODServerResponseData> {
                override fun onResponse(
                    call: Call<PODServerResponseData>,
                    response: Response<PODServerResponseData>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        liveDataForViewToObserve.value =
                            PictureOfTheDayData.Success(response.body()!!)
                    } else {
                        liveDataForViewToObserve.value =
                            PictureOfTheDayData.Error(Throwable("Unidentified error"))
                    }
                }
                override fun onFailure(call: Call<PODServerResponseData>, t: Throwable) {
                    liveDataForViewToObserve.value = PictureOfTheDayData.Error(t)
                }
            })
        }
    }

}
