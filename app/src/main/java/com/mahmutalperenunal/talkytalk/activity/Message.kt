package com.mahmutalperenunal.talkytalk.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.fxn.pix.Options
import com.fxn.pix.Pix
import com.fxn.utility.PermUtil
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mahmutalperenunal.talkytalk.BR
import com.mahmutalperenunal.talkytalk.constants.AppConstants
import com.mahmutalperenunal.talkytalk.databinding.ActivityMessageBinding
import com.mahmutalperenunal.talkytalk.databinding.LeftItemLayoutBinding
import com.mahmutalperenunal.talkytalk.databinding.RightItemLayoutBinding
import com.mahmutalperenunal.talkytalk.model.ChatListModel
import com.mahmutalperenunal.talkytalk.model.MessageModel
import com.mahmutalperenunal.talkytalk.model.UserModel
import com.mahmutalperenunal.talkytalk.permission.AppPermission
import com.mahmutalperenunal.talkytalk.services.SendMediaService
import com.mahmutalperenunal.talkytalk.util.AppUtil
import org.json.JSONObject

class Message : AppCompatActivity() {

    private lateinit var binding: ActivityMessageBinding

    private var hisId: String? = null
    private var hisImage: String? = null
    private var chatId: String? = null
    private var myName: String? = null

    private lateinit var myImage: String
    private lateinit var myId: String

    private lateinit var appUtil: AppUtil
    private var firebaseRecyclerAdapter: FirebaseRecyclerAdapter<MessageModel, ViewHolder>? = null
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var appPermission: AppPermission

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appUtil = AppUtil()
        myId = appUtil.getUID()!!
        sharedPreferences = getSharedPreferences("userData", MODE_PRIVATE)
        myImage = sharedPreferences.getString("myImage", "").toString()
        myName = sharedPreferences.getString("myName", "").toString()
        appPermission = AppPermission()

        binding.activity = this

        if (intent.hasExtra("chatId")) {

            chatId = intent.getStringExtra("chatId")
            hisId = intent.getStringExtra("hisId")
            hisImage = intent.getStringExtra("hisImage")
            readMessages(chatId!!)

        } else {
            hisId = intent.getStringExtra("hisId")
            hisImage = intent.getStringExtra("hisImage")
        }

        binding.hisImage = hisImage

        binding.btnSend.setOnClickListener {
            val message = binding.msgText.text.toString()
            if (message.isEmpty())
                Toast.makeText(this, "Enter Message", Toast.LENGTH_SHORT).show()
            else {
                sendMessage(message)
                getToken(message)
            }
        }

        binding.msgText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isEmpty())
                    typingStatus("false")
                else
                    typingStatus(hisId!!)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnDataSend.setOnClickListener {
            pickImage()
        }

        if (chatId == null)
            checkChat(hisId!!)

        checkOnlineStatus()
    }

    private fun checkChat(hisId: String) {

        val databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(myId)
        val query = databaseReference.orderByChild("member").equalTo(hisId)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (ds in snapshot.children) {
                        val member = ds.child("member").value.toString()
                        if (member == hisId) {
                            chatId = ds.key
                            readMessages(chatId!!)
                            break
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun createChat(message: String) {

        var databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(myId)
        chatId = databaseReference.push().key

        val chatListMode =
            ChatListModel(chatId!!, message, System.currentTimeMillis().toString(), hisId!!)

        databaseReference.child(chatId!!).setValue(chatListMode)

        databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(hisId!!)

        val chatList =
            ChatListModel(chatId!!, message, System.currentTimeMillis().toString(), myId)

        databaseReference.child(chatId!!).setValue(chatList)

        databaseReference = FirebaseDatabase.getInstance().getReference("Chat").child(chatId!!)

        val messageModel = MessageModel(myId, hisId!!, message, type = "text")
        databaseReference.push().setValue(messageModel)


    }

    private fun sendMessage(message: String) {

        if (chatId == null)
            createChat(message)
        else {

            var databaseReference =
                FirebaseDatabase.getInstance().getReference("Chat").child(chatId!!)

            val messageModel =
                MessageModel(myId, hisId!!, message, System.currentTimeMillis().toString(), "text")

            databaseReference.push().setValue(messageModel)

            val map: MutableMap<String, Any> = HashMap()

            map["lastMessage"] = message
            map["date"] = System.currentTimeMillis().toString()

            databaseReference =
                FirebaseDatabase.getInstance().getReference("ChatList").child(myId).child(chatId!!)

            databaseReference.updateChildren(map)

            databaseReference =
                FirebaseDatabase.getInstance().getReference("ChatList").child(hisId!!)

                    .child(chatId!!)
            databaseReference.updateChildren(map)
        }
    }

    private fun readMessages(chatId: String) {

        val query = FirebaseDatabase.getInstance().getReference("Chat").child(chatId)

        val firebaseRecyclerOptions = FirebaseRecyclerOptions.Builder<MessageModel>()
            .setLifecycleOwner(this)
            .setQuery(query, MessageModel::class.java)
            .build()
        query.keepSynced(true)

        firebaseRecyclerAdapter =
            object : FirebaseRecyclerAdapter<MessageModel, ViewHolder>(firebaseRecyclerOptions) {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

                    var viewDataBinding: ViewDataBinding? = null

                    if (viewType == 0)
                        viewDataBinding = RightItemLayoutBinding.inflate(
                            LayoutInflater.from(parent.context),
                            parent,
                            false
                        )

                    if (viewType == 1)

                        viewDataBinding = LeftItemLayoutBinding.inflate(
                            LayoutInflater.from(parent.context),
                            parent,
                            false
                        )

                    return ViewHolder(viewDataBinding!!)

                }

                override fun onBindViewHolder(
                    holder: ViewHolder,
                    position: Int,
                    messageModel: MessageModel
                ) {
                    if (getItemViewType(position) == 0) {
                        holder.viewDataBinding.setVariable(BR.message, messageModel)
                        holder.viewDataBinding.setVariable(BR.messageImage, myImage)
                    }

                    if (getItemViewType(position) == 1) {

                        holder.viewDataBinding.setVariable(BR.message, messageModel)
                        holder.viewDataBinding.setVariable(BR.messageImage, hisImage)
                    }
                }

                override fun getItemViewType(position: Int): Int {

                    val messageModel = getItem(position)
                    return if (messageModel.senderId == myId)
                        0
                    else
                        1
                }
            }

        binding.messageRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.messageRecyclerView.adapter = firebaseRecyclerAdapter
        firebaseRecyclerAdapter!!.startListening()

    }

    class ViewHolder(var viewDataBinding: ViewDataBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root)

    override fun onPause() {
        super.onPause()
        if (firebaseRecyclerAdapter != null)
            firebaseRecyclerAdapter!!.stopListening()
        appUtil.updateOnlineStatus("offline")
    }

    fun userInfo() {
        val intent = Intent(this, UserInfo::class.java)
        intent.putExtra("userId", hisId)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        appUtil.updateOnlineStatus("online")
    }

    private fun checkOnlineStatus() {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(hisId!!)
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userModel = snapshot.getValue(UserModel::class.java)
                    binding.online = userModel?.online

                    val typing = userModel?.typing

                    if (typing == myId) {
                        binding.lottieAnimation.visibility = View.VISIBLE
                        binding.lottieAnimation.playAnimation()
                    } else {
                        binding.lottieAnimation.cancelAnimation()
                        binding.lottieAnimation.visibility = View.GONE

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun typingStatus(typing: String) {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(myId)
        val map = HashMap<String, Any>()
        map["typing"] = typing
        databaseReference.updateChildren(map)
    }

    private fun getToken(message: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(hisId!!)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val token = snapshot.child("token").value.toString()

                    val to = JSONObject()
                    val data = JSONObject()

                    data.put("hisId", myId)
                    data.put("hisImage", myImage)
                    data.put("title", myName)
                    data.put("message", message)
                    data.put("chatId", chatId)

                    to.put("to", token)
                    to.put("data", data)
                    sendNotification(to)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun sendNotification(to: JSONObject) {

        val request: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            AppConstants.NOTIFICATION_URL,
            to,
            Response.Listener { response: JSONObject ->
                Log.d("TAG", "onResponse: $response")
            },
            Response.ErrorListener {
                Log.d("TAG", "onError: $it")
            }) {

            override fun getHeaders(): MutableMap<String, String> {
                val map: MutableMap<String, String> = HashMap()

                map["Authorization"] = "key=" + AppConstants.SERVER_KEY
                map["Content-type"] = "application/json"

                return map
            }

            override fun getBodyContentType(): String {
                return "application/json"
            }
        }

        val requestQueue = Volley.newRequestQueue(this)
        request.retryPolicy = DefaultRetryPolicy(
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        requestQueue.add(request)

    }

    private fun pickImage() {

        val options: Options = Options.init()
            .setRequestCode(100)
            .setCount(5)
            .setFrontfacing(true)
            .setSpanCount(4)
            .setExcludeVideos(true)
            .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)
            .setPath("/Chat Me/Media/Sent")

        Pix.start(this@Message, options)
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("ObsoleteSdkInt")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == 100) {

            val returnValue = data?.getStringArrayListExtra(Pix.IMAGE_RESULTS)

            if (chatId == null)
                Toast.makeText(this, "Please send text message first", Toast.LENGTH_SHORT).show()
            else {
                Toast.makeText(this, "Called", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, SendMediaService::class.java)
                intent.putExtra("hisID", hisId)
                intent.putExtra("chatID", chatId)
                intent.putStringArrayListExtra("media", returnValue)

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
                    startForegroundService(intent)
                else
                    startService(intent)
            }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS -> {

                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImage()
                } else {
                    Toast.makeText(
                        this@Message,
                        "Approve permissions to open Pix ImagePicker",
                        Toast.LENGTH_LONG
                    ).show()
                }

                return
            }
        }
    }
}