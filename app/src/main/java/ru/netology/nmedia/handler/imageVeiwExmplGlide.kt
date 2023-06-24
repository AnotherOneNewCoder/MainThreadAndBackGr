package ru.netology.nmedia.handler


import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import ru.netology.nmedia.R

fun ImageView.load(url: String) {
    Glide.with(this)
        .load(url)
        .placeholder(R.drawable.ic_loading_100dp)
        .error(R.drawable.ic_error_100dp)
        .transition(DrawableTransitionOptions.withCrossFade())
        .transform(CircleCrop())
        .timeout(10_000)
        .into(this)
}