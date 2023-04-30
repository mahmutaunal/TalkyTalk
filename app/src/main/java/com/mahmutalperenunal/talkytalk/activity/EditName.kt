package com.mahmutalperenunal.talkytalk.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.mahmutalperenunal.talkytalk.R
import com.mahmutalperenunal.talkytalk.databinding.ActivityEditNameBinding
import com.mahmutalperenunal.talkytalk.util.AppUtil

class EditName : AppCompatActivity() {

    private lateinit var binding: ActivityEditNameBinding
    private lateinit var fName: String
    private lateinit var lName: String
    private lateinit var appUtil: AppUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditNameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setContentView(R.layout.activity_edit_name)

        appUtil = AppUtil()

        if (intent.hasExtra("name")) {
            val name = intent.getStringExtra("name")
            if (name!!.contains(" ")) {
                val split = name.split(" ")
                binding.edtFName.setText(split[0])
                binding.edtLName.setText(split[1])

            }
        }

        binding.btnEditName.setOnClickListener {
            if (areFieldEmpty()) {
                val intent = Intent()

                intent.putExtra("name", "$fName $lName")
                setResult(100, intent)
                finish()
            }
        }
    }

    private fun areFieldEmpty(): Boolean {
        fName = binding.edtFName.text.toString()
        lName = binding.edtLName.text.toString()
        var required = false
        var view: View? = null

        if (fName.isEmpty()) {
            binding.edtFNameLayout.error = "Field is required"
            required = true
            view = binding.edtFName

        } else if (lName.isEmpty()) {
            binding.edtLNameLayout.error = "Field is required"
            required = true
            view = binding.edtLName

        }

        return if (required) {
            view?.requestFocus()
            false
        } else true
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