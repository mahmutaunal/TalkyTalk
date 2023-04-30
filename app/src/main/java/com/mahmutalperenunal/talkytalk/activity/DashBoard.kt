package com.mahmutalperenunal.talkytalk.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.mahmutalperenunal.talkytalk.R
import com.mahmutalperenunal.talkytalk.databinding.ActivityDashBoardBinding
import com.mahmutalperenunal.talkytalk.fragment.Chat
import com.mahmutalperenunal.talkytalk.fragment.Contact
import com.mahmutalperenunal.talkytalk.fragment.Profile
import com.mahmutalperenunal.talkytalk.util.AppUtil

class DashBoard : AppCompatActivity() {

    private var fragment: Fragment? = null
    private lateinit var appUtil: AppUtil

    private lateinit var binding: ActivityDashBoardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appUtil= AppUtil()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.dashboardContainer, Chat()).commit()
            binding.bottomNavigation.selectedItemId = R.id.btnChat
        }
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.btnChat -> {
                    fragment = Chat()

                    fragment!!.let {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.dashboardContainer, fragment!!)
                            .commit()
                    }

                    true
                }

                R.id.btnProfile -> {
                    fragment = Profile()

                    fragment!!.let {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.dashboardContainer, fragment!!)
                            .commit()
                    }

                    true
                }

                R.id.btnContact -> {
                    fragment = Contact()

                    fragment!!.let {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.dashboardContainer, fragment!!)
                            .commit()
                    }

                    true
                }

                else -> false
            }
        }
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