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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var auth: FirebaseAuth
    lateinit var adapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        setUpActionBar()

        val database = Firebase.database // Создания инстаннца
        val myRef = database.getReference("message") // Создание пути для отправки данных в БД

        /**
         * Слушатель нажатий на кнопку "Send"
         */
        binding.bSend.setOnClickListener {
            myRef.child(myRef.push().key ?: "emptyPath").setValue(User(auth.currentUser?.displayName,binding.thisMessage.text.toString())) // При нажатии на кнопку отправляет текст введенный в textField(thisMessage) по пути myRef
        }

        /**
         * Функция прослушивает изменения на пути myRef(message)
         */
        onChangeRouteListener(myRef)
        initRcView()

    }

    private fun initRcView() = with(binding){
        adapter = UserAdapter()
        rcView.layoutManager = LinearLayoutManager(this@MainActivity)
        rcView.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.sign_out){
            auth.signOut()
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Функция принимает путь dRef(узел по которому происходит запись в БД)
     */
    private fun onChangeRouteListener(dRef: DatabaseReference) {

        dRef.addValueEventListener(object : ValueEventListener {

            /**
             * При изменении данных происходит вызов данной функции
             * @param snapshot - Это и есть те данные которые мы прослушиваем
             */
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = ArrayList<User>()
                 for (s in snapshot.children){
                     val user = s.getValue(User::class.java)
                     if (user != null)list.add(user)

                 }

                adapter.submitList(list)
            }


            override fun onCancelled(error: DatabaseError) {}

        })
    }

    private fun setUpActionBar(){
        val ab = supportActionBar
        Thread{
            val bMap = Picasso.get().load(auth.currentUser?.photoUrl).get() // Передаём в параметр картинку
            val dIcon = BitmapDrawable(resources, bMap)
            runOnUiThread {
                ab?.setDisplayHomeAsUpEnabled(true)
                ab?.setHomeAsUpIndicator(dIcon)
                ab?.title = auth.currentUser?.displayName
            }
        }.start()

    }
}