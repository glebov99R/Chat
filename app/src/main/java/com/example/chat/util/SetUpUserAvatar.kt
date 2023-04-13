package com.example.chat.util

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.chat.User

fun setupUserAvatar(user: User, imageView: ImageView) {
    Glide.with(APP_ACTIVITY)
        .load(user.avatarUrl)
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .circleCrop()
        .into(imageView)
}

fun setupImageView(user: User, imageView: ImageView){
    Glide.with(APP_ACTIVITY)
        .load(user.photoUrl)
        .transform(CenterCrop(), RoundedCorners(60))
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(imageView)
}