package com.example.chat

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chat.databinding.ActivityMainBinding
import com.example.chat.util.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import java.io.File

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var adapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AUTH = Firebase.auth // Firebase.auth возвращает объект класса FirebaseAuth, который предоставляет API для аутентификации пользователей в приложении Firebase.

        DATABASE = Firebase.database // Создания инстаннца

        MY_REF = DATABASE.getReference(NODE_MESSAGE) // Создание пути для отправки данных в БД

        REF_STORAGE_ROOT = FirebaseStorage.getInstance().reference.child("images") // Создаем ссылку на папку в Firebase Storage, в которую будем загружать изображение

        APP_ACTIVITY = this // Ссылка на наше Activity



        setUpActionBar()

        binding.thisMessage.addTextChangedListener {

            if (binding.thisMessage.text.isEmpty()){
                binding.bSend.visibility = View.GONE
                binding.bAttach.visibility = View.VISIBLE
            } else {
                binding.bSend.visibility = View.VISIBLE
                binding.bAttach.visibility = View.GONE
            }

        }

        binding.bAttach.setOnClickListener {
//            attachFile()
            chooseImage()
        }

        binding.bSend.setOnClickListener {

            val messageId = MY_REF.push().key ?: "emptyPath"

            MY_REF.child(messageId).setValue(
                    User(
                        name = AUTH.currentUser?.displayName,
                        message = binding.thisMessage.text.toString(),
                        messageId = messageId
                    )
                )
                binding.thisMessage.setText("")
        }

        /**
         * Функция прослушивает изменения на пути myRef(message)
         */
        onChangeRouteListener(MY_REF)


        initRcView() // Инизиализируем метод initRcView


    }

/////////////////////////////////////////////////////////////////////////////////////////////////////

    private fun onChangeRouteImageListener(dtStoreRef: StorageReference){

        val storage = FirebaseStorage.getInstance()
        val rootRef = storage.reference
        val islandRef = rootRef.child("images/island.jpg")



        val localFile = File.createTempFile("images", "jpg")

        rootRef.getFile(localFile).addOnSuccessListener { taskSnapshot ->

        }.addOnFailureListener { exception ->

        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            // Обработка выбранного изображения
            val selectedImageUri = result.data?.data
            // Вызов функции загрузки изображения в Firebase Storage
            uploadImageToFirebaseStorage(selectedImageUri)
        }
    }

    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        pickImageLauncher.launch(Intent.createChooser(intent, "Select Image"))
    }

    private fun uploadImageToFirebaseStorage(imageUri: Uri?) {
        // Проверяем, что URI изображения не является null
        imageUri?.let { uri ->
            // Создаем ссылку на папку в Firebase Storage, в которую будем загружать изображение
            val storageReference = FirebaseStorage.getInstance().reference.child("images")

            // Создаем уникальное имя файла на основе текущего времени
            val fileName = "image_${System.currentTimeMillis()}.jpg"

            // Создаем ссылку на файл в Firebase Storage
            val imageRef = storageReference.child(fileName)

            // Загружаем изображение в Firebase Storage
            imageRef.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    // Получаем ссылку на загруженное изображение
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()  // Ваш код обработки ссылки на изображение

                        val image = MY_REF.push().key ?: "emptyImage"

                        MY_REF.child(image).setValue(
                            User(
                                name = AUTH.currentUser?.displayName,
                                messageId = image,
                                photoUrl = imageUrl
                            )
                        )
                    }
                }
                .addOnFailureListener { exception ->
                    // Обработка ошибки загрузки изображения
                    Log.e("23", "Error uploading image to Firebase Storage: ${exception.message}")
                }
        }
    }

    /**
     * Функция которая инициализирует RecyclerView и устанавливает адаптер для него.
     */
    private fun initRcView() = with(binding){
        adapter = UserAdapter() // Инизиализируем адаптер
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
            AUTH.signOut()
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
                     if (user != null){
                         list.add(user)
                     } // Мы проверяем, что полученный объект не равен null и добавляем его в список list.
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
            val bMap = Picasso.get().load(AUTH.currentUser?.photoUrl).get()

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
                ab?.title = AUTH.currentUser?.displayName // Устанавливаем в заголово ActionBar имя текущего пользователя прошедшего Авторизацию
            }
        }.start() // создание и запуск нового потока, Когда поток запускается с помощью метода start(), операции загрузки изображения будут выполнены параллельно с главным потоком.

    }


}