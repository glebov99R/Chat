package com.example.chat.util

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.example.chat.User
import com.bumptech.glide.request.target.Target

fun setupImageView(user: User, imageView: ImageView, progressBar: ProgressBar){

    val listener = object : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            progressBar.visibility = View.GONE
            return false
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            progressBar.visibility = View.GONE
            return false
        }
    }

    Glide.with(APP_ACTIVITY)
        .load(user.photoUrl)
        .transform(CenterCrop(), RoundedCorners(60))
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .listener(listener)
        .into(imageView)
}

fun setupUserAvatar(user: User, imageView: ImageView) {
    Glide.with(APP_ACTIVITY)
        .load(user.avatarUrl)
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .circleCrop()
        .into(imageView)
}

