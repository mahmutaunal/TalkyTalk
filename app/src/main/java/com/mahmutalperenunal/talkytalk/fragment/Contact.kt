package com.mahmutalperenunal.talkytalk.fragment

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mahmutalperenunal.talkytalk.adapter.ContactAdapter
import com.mahmutalperenunal.talkytalk.constants.AppConstants
import com.mahmutalperenunal.talkytalk.databinding.FragmentContactBinding
import com.mahmutalperenunal.talkytalk.model.UserModel
import com.mahmutalperenunal.talkytalk.permission.AppPermission

class Contact : Fragment() {

    private var _binding: FragmentContactBinding? = null
    private val binding get() = _binding!!

    private lateinit var appPermission: AppPermission

    private lateinit var mobileContacts: ArrayList<UserModel>
    private lateinit var appContacts: ArrayList<UserModel>

    private var contactAdapter: ContactAdapter? = null

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var phoneNumber: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactBinding.inflate(inflater, container, false)

        appPermission = AppPermission()
        firebaseAuth = FirebaseAuth.getInstance()
        phoneNumber = firebaseAuth.currentUser?.displayName!!

        if (appPermission.isContactOk(requireContext())) {
            getMobileContact()
        } else appPermission.requestContactPermission(requireActivity())

        binding.contactSearchView.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (contactAdapter != null)
                    contactAdapter!!.filter.filter(newText)
                return false
            }
        })

        return binding.root
    }

    @SuppressLint("Range")
    private fun getMobileContact() {
        val projection = arrayOf(
            ContactsContract.Data.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val contentResolver = requireContext().contentResolver
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        if (cursor != null) {
            mobileContacts = ArrayList()

            while (cursor.moveToNext()) {

                val name =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                var number =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                number = number.replace("\\s".toRegex(), "")
                val num = number.elementAt(0).toString()
                if (num == "0")
                    number = number.replaceFirst("0+".toRegex(), "+92")
                val userModel = UserModel()
                userModel.name = name
                userModel.number = number
                mobileContacts.add(userModel)
            }

            cursor.close()
            getAppContact(mobileContacts)
        }
    }

    private fun getAppContact(mobileContact: ArrayList<UserModel>) {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        val query = databaseReference.orderByChild("number")
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    appContacts = ArrayList()
                    for (data in snapshot.children) {
                        val number = data.child("number").value.toString()
                        for (mobileModel in mobileContact) {
                            if (mobileModel.number == number && number != phoneNumber) {
                                val userModel = data.getValue(UserModel::class.java)
                                appContacts.add(userModel!!)
                            }
                        }
                    }

                    binding.recyclerViewContact.apply {
                        layoutManager = LinearLayoutManager(context)
                        setHasFixedSize(true)
                        contactAdapter = ContactAdapter(appContacts)
                        adapter = contactAdapter
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            AppConstants.CONTACT_PERMISSION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    getMobileContact()
                else Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}