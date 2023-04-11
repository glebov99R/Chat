package com.example.chat

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chat.util.CURRENT_UID
import com.example.chat.util.MY_REF
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
        const val VIEW_TYPE_MY_TEXT = 1
        const val VIEW_TYPE_MY_IMAGE = 2

        const val VIEW_TYPE_OTHER_TEXT = 3
        const val VIEW_TYPE_OTHER_IMAGE = 4
    }

    class  ItemMyMessageHolder(item: View): RecyclerView.ViewHolder(item){

        private val messageTextView: TextView = itemView.findViewById(R.id.myMessage)
        private val userNameTextView: TextView = itemView.findViewById(R.id.myName)

        fun bindMyMessage(user: User) {
            messageTextView.text = user.message
            userNameTextView.text = user.name
            itemView.setOnLongClickListener {
                val messageId = user.messageId
                MY_REF.child(messageId!!).removeValue()
                true
            }
        }
    }

    class ItemMyImageHolder(item: View): RecyclerView.ViewHolder(item){

        private val imageView: ImageView = itemView.findViewById(R.id.imageMyMessage)

        fun bindMyImage(user: User) {
            Picasso.get().load(user.photoUrl).into(imageView)
            itemView.setOnLongClickListener {
                val sad = Firebase.storage.getReferenceFromUrl(user.photoUrl!!)
                sad.delete().addOnSuccessListener {
                    MY_REF.child(user.messageId!!).removeValue()
                }
                    true
            }
        }
    }

    class  ItemOtherMessageHolder(item: View): RecyclerView.ViewHolder(item){

        private val messageTextView: TextView = itemView.findViewById(R.id.otherMessage)
        private val userNameTextView: TextView = itemView.findViewById(R.id.otherName)

        fun bindOtherMessage(user: User) {
            messageTextView.text = user.message
            userNameTextView.text = user.name
//            itemView.setOnLongClickListener {
//                val messageId = user.messageId
//                MY_REF.child(messageId!!).removeValue()
//                true
//            }
        }
    }

    class ItemOtherImageHolder(item: View): RecyclerView.ViewHolder(item){

        private val imageView: ImageView = itemView.findViewById(R.id.imageOtherMessage)

        fun bindOtherImage(user: User) {
            Picasso.get().load(user.photoUrl).into(imageView)
//            itemView.setOnLongClickListener {
//                val sad = Firebase.storage.getReferenceFromUrl(user.photoUrl!!)
//                sad.delete().addOnSuccessListener {
//                    MY_REF.child(user.messageId!!).removeValue()
//                }
//                true
//            }
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
                ItemMyMessageHolder(view)
            }
            VIEW_TYPE_MY_IMAGE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_list_my_image_item, parent, false)
                ItemMyImageHolder(view)
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val user = getItem(position)
        when (holder.itemViewType) {
            VIEW_TYPE_MY_TEXT ->  {
                val itemMyMessageHolder = holder as ItemMyMessageHolder
                itemMyMessageHolder.bindMyMessage(user)
//                Log.d("wqeeqwq","VIEW_TYPE_MY_TEXT")
            }
            VIEW_TYPE_MY_IMAGE -> {
                val itemMyImageHolder = holder as ItemMyImageHolder
                itemMyImageHolder.bindMyImage(user)
//                Log.d("wqeeqwq","VIEW_TYPE_MY_IMAGE")
            }
            VIEW_TYPE_OTHER_TEXT -> {
                val itemOtherMessageHolder = holder as ItemOtherMessageHolder
                itemOtherMessageHolder.bindOtherMessage(user)
//                Log.d("wqeeqwq","VIEW_TYPE_OTHER_TEXT")
            }
            VIEW_TYPE_OTHER_IMAGE -> {
                val itemOtherImageHolder = holder as ItemOtherImageHolder
                itemOtherImageHolder.bindOtherImage(user)
//                Log.d("wqeeqwq","VIEW_TYPE_OTHER_IMAGE")
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val user = getItem(position)

         return when ( user.userId == CURRENT_UID){
             true -> {
                 if (user.photoUrl != null ){
                     VIEW_TYPE_MY_IMAGE
                 } else {
                     VIEW_TYPE_MY_TEXT
                 }
             }
             false -> {
                 if (user.photoUrl != null ){
                     VIEW_TYPE_OTHER_IMAGE
                 } else {
                     VIEW_TYPE_OTHER_TEXT
                 }
             }
         }
    }

}

