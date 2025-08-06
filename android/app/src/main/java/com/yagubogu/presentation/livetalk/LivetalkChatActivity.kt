package com.yagubogu.presentation.livetalk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.yagubogu.databinding.ActivityLivetalkChatBinding

class LivetalkChatActivity : AppCompatActivity() {
    private val binding: ActivityLivetalkChatBinding by lazy {
        ActivityLivetalkChatBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val layoutManager =
            LinearLayoutManager(this).apply {
                stackFromEnd = true
                reverseLayout = true
            }

        binding.rvChatMessages.apply {
            this.layoutManager = layoutManager
            // Todo: adapter = chatAdapter

            setHasFixedSize(true)
            itemAnimator = null

            clipToPadding = false
        }
    }
}
