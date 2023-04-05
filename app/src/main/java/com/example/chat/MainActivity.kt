package com.example.chat

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chat.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity(), UserAdapter.Listener {

    /**
     * lateinit (отложенная инициализация) означает, что переменная binding не будет инициализирована в момент ее объявления,
     * а будет проинициализирована позднее в коде, до того момента, когда будет использоваться.
     */
    lateinit var binding: ActivityMainBinding

    /**
     * lateinit (отложенная инициализация) означает, что переменная auth не будет инициализирована в момент ее объявления,
     * а будет проинициализирована позднее в коде, до того момента, когда будет использоваться.
     */
    lateinit var auth: FirebaseAuth


    /**
     * lateinit (отложенная инициализация) означает, что переменная adapter не будет инициализирована в момент ее объявления,
     * а будет проинициализирована позднее в коде, до того момента, когда будет использоваться.
     */
    lateinit var adapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // Это метод жизненного цикла активности, который вызывается при ее создании.
        /**
         * binding = ActivityMainBinding.inflate(layoutInflater) создает экземпляр ActivityMainBinding,
         * который содержит ссылки на все виды, определенные в макете activity_main.xml, и связывает их с соответствующими переменными в коде.
         */
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root) // Устанавливает корневое представление макета activity_main.xml в качестве основного содержимого этой активности.

        auth = Firebase.auth // Firebase.auth возвращает объект класса FirebaseAuth, который предоставляет API для аутентификации пользователей в приложении Firebase.

        setUpActionBar()

        val database = Firebase.database // Создания инстаннца

        val myRef = database.getReference("message") // Создание пути для отправки данных в БД

        /**
         * Этот код устанавливает обработчик нажатия на кнопку bSend и выполняет запись в базу данных Firebase Realtime Database.
         * При нажатии на кнопку bSend вызывается лямбда-выражение, которое выполняет следующие действия:
         *
         * 1) Создает новый узел в базе данных Firebase Realtime Database с помощью метода child объекта myRef,
         * указывая в качестве имени ключ, полученный с помощью метода push() (если ключ не получен, то будет использоваться строка "emptyPath").
         *
         * 2) Устанавливает значение узла в виде объекта User, содержащего имя пользователя и текст сообщения, полученный из EditText с идентификатором thisMessage объекта binding.
         *
         * В данном случае метод setValue записывает в базу данных Firebase Realtime Database объект User, содержащий имя пользователя и текст сообщения.
         * Этот объект будет записан в узел, имя которого задано методом child объекта myRef. Если указанный узел не существует, то он будет создан автоматически.
         *
         * !!! Этот код отвечает за отправку сообщения в базу данных Firebase Realtime Database. При нажатии на кнопку "bSend", код генерирует уникальный идентификатор сообщения с помощью метода push().key
         * и сохраняет новый экземпляр класса User в базу данных. Затем текстовое поле thisMessage очищается для ввода следующего сообщения.
         */
        binding.bSend.setOnClickListener {

            val messageId = myRef.push().key ?: "emptyPath"

            myRef.child(messageId).setValue(
                User(
                    name = auth.currentUser?.displayName,
                    message = binding.thisMessage.text.toString(),
                    messageId = messageId
                )
            )
            binding.thisMessage.setText("")
        }

//        adapter.setOnItemLongClickListener(object : UserAdapter.OnItemLongClickListener {
//            override fun onItemLongClick(user: User) {
//                // Получаем идентификатор сообщения
//                val messageId = user.messageId
//                // Удаляем сообщение из базы данных по его идентификатору
//                myRef.child(messageId!!).removeValue()
//            }
//        })




        /**
         * Функция прослушивает изменения на пути myRef(message)
         */
        onChangeRouteListener(myRef)

        initRcView(this) // Инизиализируем метод initRcView

    }



    /**
     * Функция которая инициализирует RecyclerView и устанавливает адаптер для него.
     */
    private fun initRcView(listener : UserAdapter.Listener) = with(binding){
        adapter = UserAdapter(listener) // Инизиализируем адаптер


        /**
         * мы устанавливаем для rcView (RecyclerView) менеджер компоновки LinearLayoutManager,
         * который позволяет отображать элементы списка в виде вертикального списка.
         */
        rcView.layoutManager = LinearLayoutManager(this@MainActivity)
        /**
         * Далее мы устанавливаем адаптер для rcView с помощью метода setAdapter.
         * Это связывает наш адаптер UserAdapter с rcView и позволяет отображать данные из списка в RecyclerView.
         */
        rcView.adapter = adapter
    }

    /**
     * Функция используется для создания меню при запуске активности.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Функция проверяет, был ли выбран элемент меню с идентификатором R.id.sign_out.
     *  Если да, то выполняются команды auth.signOut(), которая отключает пользователя от системы аутентификации,
     *  и finish(), которая завершает текущую активность.
     *
     *  Затем функция возвращает значение true, чтобы указать, что обработка нажатия на элемент меню была успешно завершена.
     *
     *  Таким образом, если пользователь выбирает пункт меню, то сначала выполняются определенные действия, а затем управление передается в базовый класс для дополнительной обработки.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.sign_out){
            auth.signOut()
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Функция принимает узел dRef который мы хотим прослушивать на изменения.
     */
    private fun onChangeRouteListener(dRef: DatabaseReference) {

        /**
         * Внутри функции мы добавляем слушатель addValueEventListener к этому узлу.
         * Этот слушатель срабатывает каждый раз, когда происходят изменения в узле, на который он был установлен.
         */
        dRef.addValueEventListener(object : ValueEventListener {

            /**
             * При изменении в узле происходит вызов функции onDataChange
             * Внутри функции onDataChange мы считываем данные, которые пришли в snapshot в список list.
             * В данном случае, snapshot содержит дочерние узлы узла, на который установлен слушатель,
             * и мы читаем данные из каждого дочернего узла и преобразуем их в объект класса User.
             * Для этого мы используем метод getValue, который принимает класс объекта, в который необходимо преобразовать данные.
             */
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = ArrayList<User>()
                 for (s in snapshot.children){
                     val user = s.getValue(User::class.java)
                     if (user != null)list.add(user) // Мы проверяем, что полученный объект не равен null и добавляем его в список list.

                 }

                adapter.submitList(list) // Мы передаем этот список list в адаптер списка adapter с помощью метода submitList, который обновляет данные в списке и вызывает перерисовку списка.
            }

            /**
             * Функция используется для обработки ошибок в случае, если операция чтения или записи в базу данных была отменена.
             */
            override fun onCancelled(error: DatabaseError) {}

        })
    }

    /**
     * Функция используется для настройки ActionBar
     */
    private fun setUpActionBar(){
        val ab = supportActionBar // Метод supportActionBar используется для получения объекта ActionBar для текущей активности.
        Thread{
            /**
             * В данном случае, код использует объект auth класса FirebaseAuth для получения URL-адреса изображения профиля текущего пользователя
             * и передает его в качестве параметра в метод Picasso.get().load(). Метод load() загружает изображение по указанному URL-адресу.
             * Затем метод get() вызывается для получения объекта Bitmap, который представляет загруженное изображение.
             * Наконец, объект Bitmap сохраняется в переменной bMap для использования в дальнейшем, например, для создания объекта BitmapDrawable для установки значка кнопки "Домой" ActionBar
             */
            val bMap = Picasso.get().load(auth.currentUser?.photoUrl).get()

            /**
             * Код создает новый объект BitmapDrawable, используя ресурсы приложения и объект bMap,
             * содержащий загруженное изображение профиля пользователя.
             * Объект dIcon будет использоваться позже для установки значка кнопки "Домой" ActionBar.
             *
             * Параметр resources является ссылкой на объект Resources, который используется для доступа к ресурсам приложения, таким как изображения, строки и другие ресурсы.
             */
            val dIcon = BitmapDrawable(resources, bMap)

            /**
             * Метод runOnUiThread позволяет запустить переданный в него код в главном потоке пользовательского интерфейса,
             * что является необходимым для обновления пользовательского интерфейса из других потоков, таких как поток загрузки изображения.
             */
            runOnUiThread {
                ab?.setDisplayHomeAsUpEnabled(true) // Метод setDisplayHomeAsUpEnabled для отображения кнопки "Домой" ActionBar
                ab?.setHomeAsUpIndicator(dIcon) // Метод setHomeAsUpIndicator для установки значка кнопки "Домой", и метод title для установки заголовка ActionBar.
                ab?.title = auth.currentUser?.displayName // Устанавливаем в заголово ActionBar имя текущего пользователя прошедшего Авторизацию
            }
        }.start() // создание и запуск нового потока, Когда поток запускается с помощью метода start(), операции загрузки изображения будут выполнены параллельно с главным потоком.

    }


    override fun onLongClick(user: User) {
        val messageId = user.messageId
        val database = Firebase.database
        val myRef = database.getReference("message")
        myRef.child(messageId!!).removeValue()
    }

}