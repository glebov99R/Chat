package com.example.chat.util

import com.example.chat.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.StorageReference

lateinit var APP_ACTIVITY: MainActivity

lateinit var REF_STORAGE_ROOT: StorageReference

lateinit var MY_REF: DatabaseReference

lateinit var DATABASE: FirebaseDatabase

lateinit var AUTH: FirebaseAuth
