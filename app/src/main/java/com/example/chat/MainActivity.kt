package com.example.chat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.example.chat.databinding.ActivityMainBinding
import com.example.chat.util.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), UserAdapter.OnMessageLongClickListener {

    lateinit var binding: ActivityMainBinding
    lateinit var adapter: UserAdapter

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        URL_AVATAR = defaultUrlAvatar

        AUTH = Firebase.auth // Firebase.auth возвращает объект класса FirebaseAuth, который предоставляет API для аутентификации пользователей в приложении Firebase.

        DATABASE = Firebase.database // Создания инстаннца

        MY_REF = DATABASE.getReference(NODE_MESSAGE) // Создание пути для отправки данных в БД

        REF_STORAGE_ROOT = FirebaseStorage.getInstance().reference.child("images") // Создаем ссылку на папку в Firebase Storage, в которую будем загружать изображение

        AVATAR_USER = FirebaseStorage.getInstance().reference.child("avatar") // Создаем ссылку на папку в Firebase Storage, в которую будем загружать аватар пользователя

        BACKGROUND_CHAT = FirebaseStorage.getInstance().reference.child("background_chat")

        APP_ACTIVITY = this // Ссылка на наше Activity

        CURRENT_UID = AUTH.currentUser?.uid.toString() // Уникальный идентификатор пользователя

        setUpActionBar() // Устанавливаем topBar

        getUrlAvatar() // Получение url текущей аввтарки пользователля

        initRcView(this) // Инизиализируем метод initRcView

        onChangeRouteListener(MY_REF) // Функция прослушивает изменения на пути myRef(message)

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
            chooseImage()
        }

        binding.bACamera.setOnClickListener {
            dispatchTakePictureIntent()
        }

        binding.bSend.setOnClickListener {
            lifecycleScope.launch {
                sendMessage(
                    message = binding.thisMessage.text.toString(),
                    photoUrl = null
                )
                binding.thisMessage.setText("")
            }
        }

        addEndlessScrollListener(recyclerView = binding.rcView, adapter = adapter) {
            binding.rcView.postDelayed({
                binding.rcView.scrollToPosition(adapter.itemCount - 1)
            }, 100)
        }

        setBackgroundChat(
            context = this,
            imageName = "backgroundLayout.jpg",
            view = binding.clMainActivity
        )

    }


    fun addEndlessScrollListener(
        recyclerView: RecyclerView,
        adapter: RecyclerView.Adapter<*>,
        action: () -> Unit
    ) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // Список достиг конца(заполнение списка завершено), выполните ваш код здесь
                    action.invoke()
                }
            }
        })
    }

    /**
     * Функция которая инициализирует RecyclerView и устанавливает адаптер для него.
     */
    private fun initRcView(listener: UserAdapter.OnMessageLongClickListener) = with(binding){
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

    suspend fun sendMessage(
         message: String? = null,
         photoUrl: String? = null,
    ){
        withContext(Dispatchers.IO){
            val key = MY_REF.push().key ?: "emptyPath"
            MY_REF.child(key).setValue(
                User(
                    name = AUTH.currentUser?.displayName,
                    message = message,
                    messageId = key,
                    photoUrl = photoUrl,
                    userId = CURRENT_UID,
                    timeMessage = getCurrentTimeFormatted(),
                    avatarUrl = URL_AVATAR
                )
            )
            binding.rcView.postDelayed({
                binding.rcView.scrollToPosition(adapter.itemCount - 1)
            }, 100)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Создаём фаил изображения
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private lateinit var currentPhotoPath: String
    private val takePicture = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val file = File(currentPhotoPath)
            val imageUri = Uri.fromFile(file)
            uploadImageToFirebaseStorage(imageUri)
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                null
            }
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "${applicationContext.packageName}.provider",
                    it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                takePicture.launch(takePictureIntent)
            }
        }
    }

    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        pickImageLauncher.launch(Intent.createChooser(intent, "Select Image"))
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            // Обработка выбранного изображения
            val selectedImageUri = result.data?.data
            // Вызов функции загрузки изображения в Firebase Storage
            uploadImageToFirebaseStorage(selectedImageUri)
        }
    }

    private fun uploadImageToFirebaseStorage(imageUri: Uri?) {
        // Проверяем, что URI изображения не является null
        imageUri?.let { uri ->
            // Создаем уникальное имя файла на основе текущего времени
            val fileName = "image_${System.currentTimeMillis()}.jpg"

            // Создаем ссылку на файл в Firebase Storage
            val imageRef = REF_STORAGE_ROOT.child(fileName)

            // Загружаем изображение в Firebase Storage
            imageRef.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    // Получаем ссылку на загруженное изображение
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                        lifecycleScope.launch {
                            sendMessage(
                                photoUrl = uri.toString()
                            )
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    // Обработка ошибки загрузки изображения
                    Log.e("23", "Error uploading image to Firebase Storage: ${exception.message}")
                }
        }
    }


    private fun chooseAvatar() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        pickAvatar.launch(Intent.createChooser(intent, "Select Image"))
    }

    private val pickAvatar = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val selectedImageUri = result.data?.data
            uploadAvatarToFirebaseStorage(selectedImageUri)
        }
    }

    private fun uploadAvatarToFirebaseStorage(imageUri: Uri?) {
        imageUri?.let { uri ->
            val fileName = "${AUTH.currentUser?.uid}.jpg"
            val imageRef = AVATAR_USER.child(fileName)
            imageRef.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                        URL_AVATAR = uri.toString()
                        setUpActionBar()
                    }
                }
        }
    }

    private fun chooseBackgroundChat(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        pickBackground.launch(Intent.createChooser(intent, "Select Image"))
    }

    private val pickBackground = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val selectedImageUri = result.data?.data
            uploadBackgroundChat(selectedImageUri, APP_ACTIVITY,binding.clMainActivity)
        }
    }

    private fun uploadBackgroundChat(imageUri: Uri?,context: Context,view: View,) {
        imageUri?.let { uri ->

            val imageName = "backgroundLayout.jpg"
            val imageRef = BACKGROUND_CHAT.child("background")

            imageRef.putFile(uri)

            /**
             * Загружаем картику в директорию приложения
             */
            Glide.with(context)
                .asBitmap()
                .load(uri)
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            val outputStream = context.openFileOutput(imageName, Context.MODE_PRIVATE)
                            resource.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                            outputStream.close()
                    }
                })

            /**
             * Сетаем картинку в view
             */
            Glide.with(context)
                .load(uri)
                .centerCrop()
                .into(object : CustomViewTarget<View, Drawable>(view) {
                    override fun onLoadFailed(errorDrawable: Drawable?) { view.background = errorDrawable }
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) { view.background = resource }
                    override fun onResourceCleared(placeholder: Drawable?) { view.background = placeholder }
                })
        }
    }

    /**
     * Функция нужна для получение url ссылки на аватарку пользователя
     */
    @RequiresApi(Build.VERSION_CODES.O_MR1)
    private fun getUrlAvatar() {
        val storageRef = FirebaseStorage.getInstance().reference.child("avatar")
        storageRef.listAll().addOnSuccessListener { listResult ->
            if (listResult.items.isNotEmpty()) {
                for (item in listResult.items) {
                    val itemRefName = item.name
                    if (itemRefName == AUTH.currentUser?.uid + ".jpg") {
                        val pathReference = storageRef.child(itemRefName)
                        pathReference.downloadUrl.addOnSuccessListener { url ->
                            URL_AVATAR = url.toString()
                        }.addOnSuccessListener {
                            setUpActionBar()
                            binding.rcView.postDelayed({
                                binding.rcView.scrollToPosition(adapter.itemCount - 1)
                            }, 100)
                        }
                    }
                }
            }
        }
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
        if (item.itemId == R.id.sign_out) {
            AUTH.signOut()
            finish()
        }
        if (item.itemId == R.id.download_avatar) {
            chooseAvatar()
        }
        if (item.itemId == R.id.change_background_chat){
            chooseBackgroundChat()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Функция принимает узел dRef который мы хотим прослушивать на изменения.
     */
    private fun onChangeRouteListener(dRef: DatabaseReference) {
        dRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = ArrayList<User>()
                for (s in snapshot.children) {
                    val user = s.getValue(User::class.java)
                    if (user != null) {
                        list.add(user)
                    }
                }
                adapter.submitList(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onMessageLongClick(messageId: String) {
        MY_REF.child(messageId).removeValue()
    }

    override fun onImageLongClick(photoUrl: String, messageId: String) {
        val sad = Firebase.storage.getReferenceFromUrl(photoUrl)
        sad.delete().addOnSuccessListener {
            MY_REF.child(messageId).removeValue()
        }
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
            val bMap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                Picasso
                    .get()
                    .load(URL_AVATAR)
                    .resize(100,130)
                    .transform(CircleTransform())
                    .get()
            } else {
                TODO("VERSION.SDK_INT < O_MR1")
            }

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
                ab?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.black)))

            }
        }.start() // создание и запуск нового потока, Когда поток запускается с помощью метода start(), операции загрузки изображения будут выполнены параллельно с главным потоком.

    }



}