package com.mahmutalperenunal.talkytalk.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mahmutalperenunal.talkytalk.R
import com.mahmutalperenunal.talkytalk.activity.Message
import com.mahmutalperenunal.talkytalk.databinding.ChatItemLayoutBinding
import com.mahmutalperenunal.talkytalk.databinding.FragmentChatBinding
import com.mahmutalperenunal.talkytalk.model.ChatListModel
import com.mahmutalperenunal.talkytalk.model.ChatModel
import com.mahmutalperenunal.talkytalk.model.UserModel
import com.mahmutalperenunal.talkytalk.util.AppUtil

class Chat : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var appUtil: AppUtil
    private lateinit var firebaseRecyclerAdapter: FirebaseRecyclerAdapter<ChatListModel, ViewHolder>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)

        appUtil = AppUtil()
        readChat()

        return binding.root
    }

    private fun readChat() {

        val query =
            FirebaseDatabase.getInstance().getReference("ChatList").child(appUtil.getUID()!!)
        val firebaseRecyclerOptions = FirebaseRecyclerOptions.Builder<ChatListModel>()
            .setLifecycleOwner(this)
            .setQuery(query, ChatListModel::class.java)
            .build()
        firebaseRecyclerAdapter =
            object : FirebaseRecyclerAdapter<ChatListModel, ViewHolder>(firebaseRecyclerOptions) {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                    val chatItemLayoutBinding: ChatItemLayoutBinding = DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.chat_item_layout, parent, false
                    )

                    return ViewHolder(chatItemLayoutBinding)
                }

                override fun onBindViewHolder(
                    holder: ViewHolder,
                    p1: Int,
                    chatListModel: ChatListModel
                ) {
                    val databaseReference = FirebaseDatabase.getInstance().getReference("Users")
                        .child(chatListModel.member)
                    databaseReference.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val userModel = snapshot.getValue(UserModel::class.java)
                                val date = appUtil.getTimeAgo(chatListModel.date.toLong())

                                val chatModel = ChatModel(
                                    chatListModel.chatId,
                                    userModel?.name,
                                    chatListModel.lastMessage,
                                    userModel?.image,
                                    date,
                                    userModel?.online
                                )

                                holder.chatItemLayoutBinding.chatModel = chatModel

                                holder.itemView.setOnClickListener {
                                    val intent = Intent(context, Message::class.java)
                                    intent.putExtra("hisId", userModel?.uid)
                                    intent.putExtra("hisImage", userModel?.image)
                                    intent.putExtra("chatId", chatListModel.chatId)
                                    startActivity(intent)
                                }

                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
            }

        binding.recyclerViewChat.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewChat.setHasFixedSize(false)
        binding.recyclerViewChat.adapter = firebaseRecyclerAdapter
    }


    class ViewHolder(val chatItemLayoutBinding: ChatItemLayoutBinding) :
        RecyclerView.ViewHolder(chatItemLayoutBinding.root)

    override fun onResume() {
        super.onResume()
        firebaseRecyclerAdapter.startListening()
    }

    override fun onPause() {
        super.onPause()
        firebaseRecyclerAdapter.stopListening()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}