package com.example.chat.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
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
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.target.SimpleTarget
import com.example.chat.User
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

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


fun saveImageFromUrlToDrawable(context: Context, imageUrl: String, imageName: String) {
    Glide.with(context)
        .asBitmap()

        .load(imageUrl)
        .into(object : SimpleTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
//                val drawable = BitmapDrawable(context.resources, resource)
                try {
                    val outputStream = context.openFileOutput(imageName, Context.MODE_PRIVATE)
                    resource.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    outputStream.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
}

fun setDrawableBackgroundFromUrl(
    context: Context,
    imageUrl: String,
    imageName: String,
    view: View,
) {
    val file = File(context.filesDir,imageName)

    if (file.isFile){

        Glide.with(context)
            .load(File(context.filesDir, imageName))
            .centerCrop()
            .into(object : CustomViewTarget<View, Drawable>(view) {
                override fun onLoadFailed(errorDrawable: Drawable?) { view.background = errorDrawable }
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) { view.background = resource }
                override fun onResourceCleared(placeholder: Drawable?) { view.background = placeholder }
            })
    } else {

        saveImageFromUrlToDrawable(context, imageUrl, imageName)

        Glide.with(context)
            .load(File(context.filesDir, imageName))
            .centerCrop()
            .into(object : CustomViewTarget<View, Drawable>(view) {
                override fun onLoadFailed(errorDrawable: Drawable?) { view.background = errorDrawable }
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) { view.background = resource }
                override fun onResourceCleared(placeholder: Drawable?) { view.background = placeholder }
            })
    }



//    val backgroundImageResourceId = context.resources.getIdentifier(imageName, "drawable", context.packageName)


}

