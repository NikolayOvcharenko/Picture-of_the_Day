package com.example.pictureoftheday.ui.main

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import coil.api.load
import com.example.pictureoftheday.R
import kotlinx.android.synthetic.main.main_fragment.*

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        val observer = Observer<PictureOfTheDayData> { renderData(it) }
        viewModel.getData().observe(viewLifecycleOwner, observer)
    }

    private fun renderData(data: PictureOfTheDayData) {
        when (data) {
            is PictureOfTheDayData.Success -> {
                val serverResponseData: PODServerResponseData = data.serverResponseData
                val url: String? = serverResponseData.url
                if (url.isNullOrEmpty()) {
                    // показать диалог ошибка, пустой ответ сервера
                } else {
                    image_view.load(url)
                    message.text = serverResponseData.explanation
                }
            }
            is PictureOfTheDayData.Loading -> { /* Показать загрузку */ }
            is PictureOfTheDayData.Error -> { /*Показать ошибку */ }
        }
    }

}
