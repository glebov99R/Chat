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

    /**
     * lateinit (отложенная инициализация) означает, что переменная launcher не будет инициализирована в момент ее объявления,
     * а будет проинициализирована позднее в коде, до того момента, когда будет использоваться.
     */
    lateinit var launcher: ActivityResultLauncher<Intent>
    /**
     * lateinit (отложенная инициализация) означает, что переменная auth не будет инициализирована в момент ее объявления,
     * а будет проинициализирована позднее в коде, до того момента, когда будет использоваться.
     */
    lateinit var auth: FirebaseAuth
    /**
     * lateinit (отложенная инициализация) означает, что переменная binding не будет инициализирована в момент ее объявления,
     * а будет проинициализирована позднее в коде, до того момента, когда будет использоваться.
     */
    lateinit var binding: ActivitySigninBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // Это метод жизненного цикла активности, который вызывается при ее создании.

        /**
         * binding = ActivityMainBinding.inflate(layoutInflater) создает экземпляр ActivityMainBinding,
         * который содержит ссылки на все виды, определенные в макете activity_main.xml, и связывает их с соответствующими переменными в коде.
         */
        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root) // Устанавливает корневое представление макета activity_main.xml в качестве основного содержимого этой активности.


        auth = Firebase.auth // Firebase.auth возвращает объект класса FirebaseAuth, который предоставляет API для аутентификации пользователей в приложении Firebase.

        /**
         * Здесь происходит инициализация и регистрация callback-функции для получения результата из активности,
         * которая запускается при вызове метода startActivityForResult().
         *
         * Когда пользователь выбирает аккаунт в окне выбора Google-аккаунта, запускается ActivityForResult.
         * После выбора аккаунта, возвращается Intent объект, который содержит информацию об аккаунте.
         */
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){

            /**
             * Callback-функция получает этот Intent в качестве параметра в it.data и извлекает информацию о пользователе,
             * используя Google Sign-In API. Затем результат передается в firebaseAuthWithGoogle() для аутентификации в Firebase.
             */
           val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)

            /**
             * Это блок кода, который вызывается после того, как пользователь выбирает свой аккаунт Google и завершает процесс аутентификации.
             * Он получает результат из запущенной ранее интент-активности (в данном случае, из интента для выбора аккаунта Google) и извлекает информацию об аккаунте,
             * если выбор был успешным. Если выбор был успешным и информация об аккаунте извлечена успешно, то функция firebaseAuthWithGoogle() вызывается для аутентификации пользователя в Firebase
             * через его Google-аккаунт. Если выбор был неудачным (например, из-за ошибки ApiException), то это зарегистрировано в журнале Logcat.
             */
            try {

                val account = task.getResult(ApiException::class.java)

                if (account != null) firebaseAuthWithGoogle(account.idToken!!)

            } catch (e: ApiException){
                Log.d("MyLogAuth","ApiException")
            }
        }

        /**
         * Слушатель нажатий кнопки Авторизации
         */
        binding.bSignIn.setOnClickListener {
            signInWithGoogle()
        }

        checkAuthState() // Функция, которая проверяет, авторизован ли текущий пользователь в приложении.

    }

    /**
     * Это функция, которая создает клиента Google для входа в систему.
     * Она используется в функции signInWithGoogle() для получения Intent для выбора учетной записи Google.
     * Внутри функции getClient() определяется, какие данные Google должны быть запрошены у пользователя при аутентификации, и затем возвращается GoogleSignInClient.
     */
    private fun getClient(): GoogleSignInClient{
        /**
         *  В этом коде используется метод Builder, чтобы создать объект GoogleSignInOptions и настроить его на получение идентификатора токена запроса и адреса электронной почты пользователя.
         *  Возвращаемый объект GoogleSignInOptions используется в методе GoogleSignIn.getClient() для создания объекта GoogleSignInClient,
         *  который может быть использован для получения авторизационного окна для пользователя.
         */
        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        /**
         *  Функция принимает текущий контекст приложения и объект GoogleSignInOptions,
         *  который определяет тип запрошенных данных при авторизации. Она создает клиента авторизации,
         *  используя переданные параметры и возвращает его.
         */
        return GoogleSignIn.getClient(this,gso)
    }

    /**
     * Это функция signInWithGoogle, которая запускает Intent для аутентификации пользователя через Google.
     * Она использует метод getClient() для получения объекта GoogleSignInClient, через который создается Intent.
     * Затем Intent запускается с помощью launcher.launch(signInClient.signInIntent).
     * После этого пользователь может выбрать учетную запись Google и предоставить приложению разрешение на доступ к ее данным.
     * Результат аутентификации будет обработан в методе onActivityResult в классе активности.
     */
    private fun signInWithGoogle(){
        val signInClient = getClient() // intent который мы хотим отправить по нажатию на кнопку
        launcher.launch(signInClient.signInIntent)
    }

    /**
     * Это функция, которая выполняет аутентификацию пользователя в приложении с использованием учетных данных Google.
     * @param idToken - идентификатор токена, который был получен при аутентификации пользователя в Google.
     */
    private fun firebaseAuthWithGoogle(idToken: String){

        /**
         * Сначала, с помощью GoogleAuthProvider.getCredential() создается учетная запись Google для аутентификации в Firebase.
         */
        val credential = GoogleAuthProvider.getCredential(idToken,null)

        /**
         * Затем, с помощью метода signInWithCredential() объекта auth (FirebaseAuth),
         * учетная запись Google используется для аутентификации пользователя в Firebase.
         *
         * Внутри метода addOnCompleteListener() проверяется успешна ли была аутентификация
         * и в зависимости от результата функция checkAuthState() вызывается для перенаправления пользователя
         * в основную активность приложения или вывода сообщения об ошибке.
         */
        auth.signInWithCredential(credential).addOnCompleteListener{
            if (it.isSuccessful){
                Log.d("MyLogAuth","Google signIn done")
                checkAuthState()
            } else {
                Log.d("MyLogAuth","Google signIn error")
            }
        }
    }

    /**
     *  функция, которая проверяет, авторизован ли текущий пользователь в приложении.
     *  Если пользователь авторизован, то функция запускает основную активность приложения MainActivity.
     */
    private fun checkAuthState(){
        if (auth.currentUser != null){ //  Если текущий пользователь не равен null, значит пользователь уже авторизован и функция запускает MainActivity.
            /**
             * В данном случае, Intent используется для запуска основной активности приложения MainActivity.
             * Он создается с помощью конструктора Intent, в котором передается текущий контекст (this) и класс активности,
             * которую необходимо запустить (MainActivity::class.java). Затем, с помощью метода startActivity(),
             * созданный Intent запускается и переводит пользователя на основную активность приложения
             */
            val i = Intent(this,MainActivity::class.java)
            startActivity(i)
        }
        // Если же текущий пользователь равен null, то пользователь должен авторизоваться, и checkAuthState() завершается без действий.
    }
}