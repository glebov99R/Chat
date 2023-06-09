package com.example.chat

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chat.util.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class UserAdapter(private val onMessageLongClickListener: OnMessageLongClickListener): ListAdapter<User, RecyclerView.ViewHolder>(ItemComparator()) {

    companion object {
        const val VIEW_TYPE_MY_TEXT = 1
        const val VIEW_TYPE_MY_IMAGE = 2

        const val VIEW_TYPE_OTHER_TEXT = 3
        const val VIEW_TYPE_OTHER_IMAGE = 4
    }

    class ItemMyMessageHolder(item: View,private val onMessageLongClickListener: OnMessageLongClickListener) : RecyclerView.ViewHolder(item) {

        private val imagePhotoUser: ImageView = itemView.findViewById(R.id.userPhoto)
        private val messageTextView: TextView = itemView.findViewById(R.id.myMessage)
        private val userNameTextView: TextView = itemView.findViewById(R.id.myName)

        @RequiresApi(Build.VERSION_CODES.O_MR1)
        fun bindMyMessage(user: User) {

            messageTextView.setupMessageTextView(itemView)
            messageTextView.text = user.message
            setupUserAvatar(user, imagePhotoUser)
            userNameTextView.text = user.timeMessage

            itemView.setOnLongClickListener {
                onMessageLongClickListener.onMessageLongClick(user.messageId!!)
                true
            }
        }
    }

    class ItemMyImageHolder(item: View, private val onMessageLongClickListener: OnMessageLongClickListener): RecyclerView.ViewHolder(item){

        private val imageView: ImageView = itemView.findViewById(R.id.imageMyMessage)
        private val imagePhotoUser: ImageView = itemView.findViewById(R.id.userPhotoImage)
        private val progress: ProgressBar = itemView.findViewById(R.id.progressBar)

        fun bindMyImage(user: User) {
            progress.visibility = View.VISIBLE
            setupImageView(user,imageView,progress)
            setupUserAvatar(user, imagePhotoUser)

            itemView.setOnLongClickListener {
                onMessageLongClickListener.onImageLongClick(user.photoUrl!!, user.messageId!!)
                true
            }
        }
    }

    class  ItemOtherMessageHolder(item: View): RecyclerView.ViewHolder(item){

        private val imagePhotoUser: ImageView = itemView.findViewById(R.id.otherPhotoUser)
        private val messageTextView: TextView = itemView.findViewById(R.id.otherMessage)
        private val userNameTextView: TextView = itemView.findViewById(R.id.otherName)

        fun bindOtherMessage(user: User) {
            messageTextView.setupMessageTextView(itemView)
            messageTextView.text = user.message
            userNameTextView.text = user.timeMessage
            setupUserAvatar(user, imagePhotoUser)

        }
    }

    class ItemOtherImageHolder(item: View): RecyclerView.ViewHolder(item){

        private val imageView: ImageView = itemView.findViewById(R.id.imageOtherMessage)
        private val imagePhotoUser: ImageView = itemView.findViewById(R.id.otherUserPhotoImage)
        private val progress: ProgressBar = itemView.findViewById(R.id.progressBar2)

        fun bindOtherImage(user: User) {
            progress.visibility = View.VISIBLE
            setupUserAvatar(user, imagePhotoUser)
            setupImageView(user,imageView, progress)
        }
    }

    class ItemComparator : DiffUtil.ItemCallback<User>(){
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType){
            VIEW_TYPE_MY_TEXT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_list_my_message_item, parent, false)
                ItemMyMessageHolder(view, onMessageLongClickListener)
            }
            VIEW_TYPE_MY_IMAGE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_list_my_image_item, parent, false)
                ItemMyImageHolder(view, onMessageLongClickListener)
            }
            VIEW_TYPE_OTHER_TEXT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_list_other_message_item, parent, false)
                ItemOtherMessageHolder(view)
            }
            VIEW_TYPE_OTHER_IMAGE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_list_other_image_item, parent, false)
                ItemOtherImageHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val user = getItem(position)
        when (holder.itemViewType) {
            VIEW_TYPE_MY_TEXT ->  {
                val itemMyMessageHolder = holder as ItemMyMessageHolder
                itemMyMessageHolder.bindMyMessage(user)
            }
            VIEW_TYPE_MY_IMAGE -> {
                val itemMyImageHolder = holder as ItemMyImageHolder
                itemMyImageHolder.bindMyImage(user)
            }
            VIEW_TYPE_OTHER_TEXT -> {
                val itemOtherMessageHolder = holder as ItemOtherMessageHolder
                itemOtherMessageHolder.bindOtherMessage(user)
            }
            VIEW_TYPE_OTHER_IMAGE -> {
                val itemOtherImageHolder = holder as ItemOtherImageHolder
                itemOtherImageHolder.bindOtherImage(user)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val user = getItem(position)
        return when (user.userId == CURRENT_UID) {
            true -> if (user.photoUrl != null) VIEW_TYPE_MY_IMAGE else VIEW_TYPE_MY_TEXT
            false -> if (user.photoUrl != null) VIEW_TYPE_OTHER_IMAGE else VIEW_TYPE_OTHER_TEXT
        }
    }

    interface OnMessageLongClickListener {
        fun onMessageLongClick(messageId: String)
        fun onImageLongClick(photoUrl: String, messageId: String)
    }
}

