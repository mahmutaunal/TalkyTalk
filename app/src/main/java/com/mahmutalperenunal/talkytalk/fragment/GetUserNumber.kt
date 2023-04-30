package com.mahmutalperenunal.talkytalk.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.mahmutalperenunal.talkytalk.R
import com.mahmutalperenunal.talkytalk.databinding.FragmentGetUserNumberBinding
import com.mahmutalperenunal.talkytalk.model.UserModel
import java.util.concurrent.TimeUnit

class GetUserNumber : Fragment() {

    private var _binding: FragmentGetUserNumberBinding? = null
    private val binding get() = _binding!!

    private var number: String? = null
    private var code: String? = null

    private var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null

    private var firebaseAuth: FirebaseAuth? = null
    private var databaseReference: DatabaseReference? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGetUserNumberBinding.inflate(inflater, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        binding.btnGenerateOTP.setOnClickListener {
            if (checkNumber()) {
                val phoneNumber = binding.countryCodePicker.selectedCountryCodeWithPlus + number
                sendCode(phoneNumber)
            }
        }

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                firebaseAuth!!.signInWithCredential(credential).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val userModel =
                            UserModel(
                                "", "", "",
                                firebaseAuth!!.currentUser!!.phoneNumber!!,
                                firebaseAuth!!.uid!!
                            )

                        databaseReference!!.child(firebaseAuth!!.uid!!).setValue(userModel)
                        activity?.supportFragmentManager
                            ?.beginTransaction()
                            ?.replace(R.id.main_container, GetUserData())
                            ?.commit()
                    }
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> Toast.makeText(
                        context,
                        "" + e.message,
                        Toast.LENGTH_SHORT
                    ).show()

                    is FirebaseTooManyRequestsException -> Toast.makeText(
                        context,
                        "" + e.message,
                        Toast.LENGTH_SHORT
                    ).show()

                    else -> Toast.makeText(requireContext(), "" + e.message, Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onCodeSent(
                verificationCode: String,
                p1: PhoneAuthProvider.ForceResendingToken
            ) {
                code = verificationCode
                requireActivity().supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.main_container, VerifyNumber.newInstance(code!!))
                    .commit()


            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun sendCode(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,
            60,
            TimeUnit.SECONDS,
            requireActivity(),
            callbacks!!
        )

    }

    private fun checkNumber(): Boolean {
        number = binding.edtNumber.text.toString().trim()

        return if (number!!.isEmpty()) {
            binding.edtNumberLayout.error = "Field is required"
            false
        } else if (number!!.length < 10) {
            binding.edtNumberLayout.error = "Number should be 10 in length"
            false
        } else true
    }
}