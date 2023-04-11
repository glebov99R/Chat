package com.example.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chat.databinding.ActivityMainBinding.bind
import com.example.chat.databinding.UserListItemBinding
import com.example.chat.util.MY_REF
import com.example.chat.util.REF_STORAGE_ROOT
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso


/**
 * UserAdapter это класс адаптера RecyclerView, который отвечает за заполнение элементов списка данными.
 * Он наследуется от класса ListAdapter и использует внутри себя класс ItemHolder для хранения вьюх элементов списка.
 *
 * ListAdapter отслеживает изменения в списке данных и обновляет список, когда данные изменяются.
 * В качестве параметров, ListAdapter принимает два типа: модель данных и объект типа ViewHolder.
 *
 * ItemComparator() передается в качестве параметра в конструктор ListAdapter и используется для сравнения объектов в списке данных.
 */

class UserAdapter: ListAdapter<User, RecyclerView.ViewHolder>(ItemComparator()) {

    companion object {
        const val VIEW_TYPE_TEXT = 1
        const val VIEW_TYPE_IMAGE = 2
    }

    class  ItemHolder(item: View): RecyclerView.ViewHolder(item){
        private val messageTextView: TextView = itemView.findViewById(R.id.message)
        private val userNameTextView: TextView = itemView.findViewById(R.id.userName)

        fun bindMessage(user: User) {
            messageTextView.text = user.message
            userNameTextView.text = user.name
            itemView.setOnLongClickListener {
                val messageId = user.messageId
                MY_REF.child(messageId!!).removeValue()
                true
            }
        }

    }

    class ImageHolder(item: View): RecyclerView.ViewHolder(item){

        private val imageView: ImageView = itemView.findViewById(R.id.imageMessage)

        fun bindImage(user: User, position: Int) {
            Picasso
               .get()
               .load(user.photoUrl)
               .into(imageView)

            itemView.setOnLongClickListener {
                val sad = Firebase.storage.getReferenceFromUrl(user.photoUrl!!)
                sad.delete().addOnSuccessListener {
                    MY_REF.child(user.messageId!!).removeValue()
                }
                    true

            }
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
            VIEW_TYPE_TEXT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.user_list_item, parent, false)
                ItemHolder(view)
            }
            VIEW_TYPE_IMAGE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.user_list_image_item, parent, false)
                ImageHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")

        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val user = getItem(position)
        when (holder.itemViewType) {
             VIEW_TYPE_TEXT ->  {
                val messageWithHolder = holder as ItemHolder
                messageWithHolder.bindMessage(user)
            }
             VIEW_TYPE_IMAGE -> {
                val imageWithHolder = holder as ImageHolder
                imageWithHolder.bindImage(user,position)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val user = getItem(position)
         return if (user.photoUrl != null) {
             VIEW_TYPE_IMAGE
         } else {
             VIEW_TYPE_TEXT
         }
    }

}

