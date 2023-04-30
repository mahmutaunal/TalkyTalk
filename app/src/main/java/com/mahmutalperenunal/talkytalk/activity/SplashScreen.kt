package com.mahmutalperenunal.talkytalk.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.mahmutalperenunal.talkytalk.databinding.ActivitySplashScreenBinding
import com.mahmutalperenunal.talkytalk.util.AppUtil

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {

    private var firebaseAuth: FirebaseAuth? = null
    private lateinit var appUtil: AppUtil

    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        appUtil = AppUtil()

        Handler().postDelayed({

            if (firebaseAuth!!.currentUser == null) {

                startActivity(Intent(this, MainActivity::class.java))
                finish()

            } else {

                FirebaseMessaging.getInstance().token
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            if (task.result != null && !TextUtils.isEmpty(task.result)) {
                                val token: String = task.result!!
                                val databaseReference =
                                    FirebaseDatabase.getInstance().getReference("Users")
                                        .child(appUtil.getUID()!!)

                                val map: MutableMap<String, Any> = HashMap()
                                map["token"] = token
                                databaseReference.updateChildren(map)
                            }
                        }
                    }

                startActivity(Intent(this, DashBoard::class.java))
                finish()

            }

        }, 3000)
    }
}