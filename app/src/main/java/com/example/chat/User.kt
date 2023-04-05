package com.example.chat

/**
 * Класс для записи в его параметры Сообщения пользователя и его Имя
 * @param name - Имя пользователя которое мы получаем из Google аккаунта
 * @param message - Сообщение пользователя
 */
data class User(
    val name: String? = null,
    val message: String? = null,
    val id: String? = null
)
