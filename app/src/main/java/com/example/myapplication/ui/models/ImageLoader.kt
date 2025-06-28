package com.example.myapplication.ui.models

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.myapplication.R

object ImageLoader {
    fun loadImage(imageView: ImageView, url: String) {
        Glide.with(imageView.context)
            .load(url)
            .placeholder(R.drawable.default_advice_image)
            .error(R.drawable.default_advice_image)
            .into(imageView)
    }
}
