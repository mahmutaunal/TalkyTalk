package com.mahmutalperenunal.talkytalk.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mahmutalperenunal.talkytalk.R
import com.mahmutalperenunal.talkytalk.databinding.ActivityMainBinding
import com.mahmutalperenunal.talkytalk.fragment.GetUserNumber

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .add(R.id.main_container, GetUserNumber())
            .commit()
    }
}