package com.example.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chat.databinding.UserListItemBinding

/**
 * UserAdapter это класс адаптера RecyclerView, который отвечает за заполнение элементов списка данными.
 * Он наследуется от класса ListAdapter и использует внутри себя класс ItemHolder для хранения вьюх элементов списка.
 *
 * ListAdapter отслеживает изменения в списке данных и обновляет список, когда данные изменяются.
 * В качестве параметров, ListAdapter принимает два типа: модель данных и объект типа ViewHolder.
 *
 * ItemComparator() передается в качестве параметра в конструктор ListAdapter и используется для сравнения объектов в списке данных.
 */

class UserAdapter: ListAdapter<User, UserAdapter.ItemHolder>(ItemComparator()) {

    /**
     * Этот код относится к созданию ViewHolder для RecyclerView. ViewHolder используется для хранения и управления отображением элемента списка, который будет отображаться на экране.
     *
     * В данном случае, ItemHolder - это пользовательский ViewHolder, который содержит данные о пользователе (User) и отображает их в представлении (View) списка.
     *
     * UserListItemBinding - это автоматически сгенерированный класс, который предоставляет привязки для макета элемента списка,
     * в котором находятся визуальные элементы (TextView) для отображения имени пользователя и его сообщения.
     *
     * Метод create создает новый экземпляр ItemHolder и передает ему View, созданный из макета элемента списка.
     * Когда RecyclerView требует новый ViewHolder для отображения элемента списка, он вызывает этот метод.
     */
    class  ItemHolder(private val binding: UserListItemBinding): RecyclerView.ViewHolder(binding.root){

        /**
         * Это функция-расширение для класса ItemHolder, которая использует экземпляр binding для заполнения пользовательских данных
         * в соответствующих View элементах, которые привязаны к этому binding.
         *
         * В данном случае, эта функция заполняет имя пользователя (name) и сообщение (message) в соответствующие TextView элементы в макете UserListItemBinding.
         * Это происходит в блоке with(binding), который обеспечивает доступ к View элементам из макета через свойства binding.
         */
        fun bind(user: User) = with(binding){
            message.text = user.message
            userName.text = user.name
        }
        /**
         * companion object это объект, который может содержать методы и свойства,
         * аналогичные статическим методам и свойствам в Java. Он создается внутри класса,
         * но относится к самому классу, а не к его экземплярам
         *
         * В данном случае, companion object содержит метод create, который создает экземпляр ItemHolder.
         * Этот метод используется в методе onCreateViewHolder адаптера для создания новых объектов ItemHolder
         */
        companion object{
            /**
             * Эта строка создает новый объект ItemHolder с помощью метода create внутри объекта-компаньона,
             * который использует UserListItemBinding, созданный из разметки с помощью метода inflate из объекта LayoutInflater.
             * В конструкторе ItemHolder используется binding.root для доступа к корневому элементу разметки UserListItemBinding.
             * В общем, этот код создает новый экземпляр ItemHolder, готовый к использованию в RecyclerView. (Создаёт разметку)
             */
            fun create(parent: ViewGroup): ItemHolder{
                return ItemHolder(UserListItemBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false))
            }
        }
    }

    /**
     * Этот код представляет класс ItemComparator, который расширяет класс DiffUtil.ItemCallback<User>(). DiffUtil
     * это вспомогательный класс в Android, который используется для определения различий между двумя списками элементов.
     *
     * В данном случае ItemComparator определяет, каким образом должны сравниваться элементы списка типа User.
     */
    class ItemComparator : DiffUtil.ItemCallback<User>(){
        /**
         * areItemsTheSame - это метод, который используется для проверки,
         * являются ли два элемента списка одинаковыми в терминах их идентификатора (ID).
         * Этот метод принимает два объекта типа User (старый и новый) и сравнивает их.
         * Если эти объекты имеют одинаковый ID, метод возвращает true, в противном случае - false.
         */
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }


        /**
         * Это метод, определяющий, являются ли содержимое элементов старого и нового списка идентичными.
         * Если метод возвращает true, содержимое элементов считается одинаковым,
         * если false - содержимое отличается и, соответственно, нужно обновить соответствующий элемент списка.
         */
        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }

    }

    /**
     * onCreateViewHolder() является методом адаптера и вызывается для создания нового ViewHolder
     * , который будет использоваться для отображения элементов списка.
     *
     * В данном коде этот метод возвращает экземпляр ItemHolder для отображения элемента списка.
     * Для этого используется статический метод create() в классе ItemHolder, который создает новый экземпляр ItemHolder и
     * заполняет его макетом элемента списка. Этот метод передает родительский ViewGroup для ItemHolder, в котором будет размещен элемент списка.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder.create(parent)
    }
    /**
     *  Метод onBindViewHolder вызывается каждый раз, когда необходимо отобразить элемент списка в RecyclerView
     *  @param holder - это объект, который отображает элемент списка на экране.
     *  @param position - позиция элемента.
     *  Метод getItem(position) возвращает данные для элемента списка на определенной позиции,
     *  а затем holder.bind() используется для отображения этих данных в представлении holder.
     */
        override fun onBindViewHolder(holder: ItemHolder, position: Int) {
            holder.bind(getItem(position))
    }
}


/**
 * recyclerView - это компонент пользовательского интерфейса Android, который представляет список элементов с возможностью прокрутки.
 * Он используется для отображения большого количества данных в удобном и компактном формате.
 *
 * RecyclerView - более эффективный аналог ListView, он предоставляет больше гибкости и контроля над отображением элементов списка.
 * Он состоит из LayoutManager, который отвечает за расположение элементов на экране, Adapter, который предоставляет данные для отображения и
 *
 * ViewHolder,который содержит ссылки на визуальные элементы, представляющие каждый элемент в списке.
 */