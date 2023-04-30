package com.mahmutalperenunal.talkytalk.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mahmutalperenunal.talkytalk.databinding.ActivityUserInfoBinding
import com.mahmutalperenunal.talkytalk.model.UserModel
import com.mahmutalperenunal.talkytalk.util.AppUtil

class UserInfo : AppCompatActivity() {

    private lateinit var binding: ActivityUserInfoBinding

    private var userId: String? = null
    private lateinit var appUtil: AppUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appUtil = AppUtil()

        supportActionBar!!.title = "User Info"
        userId = intent.getStringExtra("userId")
        getUserData(userId)
    }

    private fun getUserData(userId: String?) {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId!!)
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userModel = snapshot.getValue(UserModel::class.java)
                    binding.userModel = userModel

                    if (userModel!!.name.contains(" ")) {
                        val split = userModel.name.split(" ")
                        binding.txtProfileFName.text = split[0]
                        binding.txtProfileLName.text = split[1]
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) { }
        })
    }

    override fun onPause() {
        super.onPause()
        appUtil.updateOnlineStatus("offline")
    }

    override fun onResume() {
        super.onResume()
        appUtil.updateOnlineStatus("online")
    }
}