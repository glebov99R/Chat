package com.example.chat.util

import com.example.chat.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.core.Context
import com.google.firebase.storage.StorageReference

lateinit var APP_ACTIVITY: MainActivity

lateinit var REF_STORAGE_ROOT: StorageReference // REF_STORAGE_ROOT

lateinit var MY_REF: DatabaseReference

lateinit var DATABASE: FirebaseDatabase

lateinit var AUTH: FirebaseAuth

lateinit var CURRENT_UID: String

const val NODE_MESSAGE = "message"

lateinit var AVATAR_USER : StorageReference

lateinit var URL_AVATAR: String


