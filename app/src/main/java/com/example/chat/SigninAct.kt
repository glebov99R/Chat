package com.example.chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.chat.databinding.ActivityMainBinding
import com.example.chat.databinding.ActivitySigninBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SigninAct : AppCompatActivity() {

    lateinit var launcher: ActivityResultLauncher<Intent>
    lateinit var auth: FirebaseAuth
    lateinit var binding: ActivitySigninBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
           val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try {

                val account = task.getResult(ApiException::class.java)

                if (account != null) firebaseAuthWithGoogle(account.idToken!!)

            } catch (e: ApiException){
                Log.d("MyLogAuth","ApiException")
            }
        }

        binding.bSignIn.setOnClickListener {
            signInWithGoogle()
        }
        checkAuthState()

    }

    /**
     * Создание окна для выбора окна для выбора google аккаунта для Авторизации
     */
    private fun getClient(): GoogleSignInClient{
        /**
         * Запрос для получения google аккаунта из телефона
         */
        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(this,gso)
    }

    private fun signInWithGoogle(){
        val signInClient = getClient() // intent который мы хотим отправить по нажатию на кнопку
        launcher.launch(signInClient.signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String){
        val credential = GoogleAuthProvider.getCredential(idToken,null)
        auth.signInWithCredential(credential).addOnCompleteListener{
            if (it.isSuccessful){
                Log.d("MyLogAuth","Google signIn done")
                checkAuthState()
            } else {
                Log.d("MyLogAuth","Google signIn error")
            }
        }
    }

    private fun checkAuthState(){
        if (auth.currentUser != null){
            val i = Intent(this,MainActivity::class.java)
            startActivity(i)
        }
    }
}