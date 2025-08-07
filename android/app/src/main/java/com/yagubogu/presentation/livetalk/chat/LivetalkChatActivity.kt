package com.yagubogu.presentation.livetalk.chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.yagubogu.databinding.ActivityLivetalkChatBinding
import java.time.LocalDateTime

class LivetalkChatActivity : AppCompatActivity() {
    private val binding: ActivityLivetalkChatBinding by lazy {
        ActivityLivetalkChatBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupRecyclerView()
        setupListener()
    }

    private fun setupListener() {
        binding.ivArrowLeft.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        val chatLayoutManager =
            LinearLayoutManager(this).apply {
                stackFromEnd = false
                reverseLayout = true
            }

        val adapter = LivetalkChatAdapter()
        adapter.submitList(DUMMY_LIVETALK_CHAT_BUBBLE_ITEMS)

        binding.rvChatMessages.apply {
            this.layoutManager = chatLayoutManager
            this.adapter = adapter
            setHasFixedSize(true)
            itemAnimator = null
            clipToPadding = false
        }
    }

    companion object {
        private val DUMMY_LIVETALK_CHAT_BUBBLE_ITEMS: List<LivetalkChatBubbleItem> =
            listOf(
                // 가장 최신 메시지 (15분 전) - 2025-08-06 20:55
                LivetalkChatBubbleItem.MyBubbleItem(
                    livetalkChatItem =
                        LivetalkChatItem(
                            chatId = 3,
                            isMine = true,
                            message = "승리 기원합니다!",
                            profileImageUrl = "",
                            nickname = "내닉네임",
                            teamName = "두산",
                            timestamp = LocalDateTime.of(2025, 8, 6, 20, 55),
                        ),
                ),
                // 오늘 저녁 (45분 전) - 2025-08-06 20:25
                LivetalkChatBubbleItem.MyBubbleItem(
                    livetalkChatItem =
                        LivetalkChatItem(
                            chatId = 17,
                            isMine = true,
                            message = "다음 경기에서는 더 좋은 결과 있으면 좋겠어요!",
                            profileImageUrl = "",
                            nickname = "내닉네임",
                            teamName = "두산",
                            timestamp = LocalDateTime.of(2025, 8, 6, 20, 25),
                        ),
                ),
                // 오늘 저녁 (1시간 25분 전) - 2025-08-06 19:45
                LivetalkChatBubbleItem.OtherBubbleItem(
                    livetalkChatItem =
                        LivetalkChatItem(
                            chatId = 16,
                            isMine = false,
                            message = "7회에 나온 그 홈런은 정말 대박이었어요. 구장 전체가 들썩들썩했습니다!",
                            profileImageUrl = "",
                            nickname = "현장러",
                            teamName = "LG",
                            timestamp = LocalDateTime.of(2025, 8, 6, 19, 45),
                        ),
                ),
                // 오늘 저녁 (2시간 10분 전) - 2025-08-06 19:00
                LivetalkChatBubbleItem.MyBubbleItem(
                    livetalkChatItem =
                        LivetalkChatItem(
                            chatId = 11,
                            isMine = true,
                            message = "오늘 경기 정말 치열했어요! 9회까지 동점이었는데 연장전에서 극적인 홈런이 나왔습니다. 우리 팀 주포의 마지막 타석이 정말 인상적이었어요.",
                            profileImageUrl = "",
                            nickname = "내닉네임",
                            teamName = "두산",
                            timestamp = LocalDateTime.of(2025, 8, 6, 19, 0),
                        ),
                ),
                // 오늘 오후 (3시간 15분 전) - 2025-08-06 17:55
                LivetalkChatBubbleItem.OtherBubbleItem(
                    livetalkChatItem =
                        LivetalkChatItem(
                            chatId = 5,
                            isMine = false,
                            message = "홈런이다! 대박!",
                            profileImageUrl = "",
                            nickname = "홈런왕",
                            teamName = "삼성",
                            timestamp = LocalDateTime.of(2025, 8, 6, 17, 55),
                        ),
                ),
                // 오늘 오후 (4시간 30분 전) - 2025-08-06 16:40
                LivetalkChatBubbleItem.MyBubbleItem(
                    livetalkChatItem =
                        LivetalkChatItem(
                            chatId = 14,
                            isMine = true,
                            message = "투수 교체 타이밍이 좀 아쉬웠네요. 그래도 전반적으로 좋은 경기였다고 생각합니다.",
                            profileImageUrl = "",
                            nickname = "내닉네임",
                            teamName = "두산",
                            timestamp = LocalDateTime.of(2025, 8, 6, 16, 40),
                        ),
                ),
                // 오늘 오전 (8시간 전) - 2025-08-06 13:10
                LivetalkChatBubbleItem.OtherBubbleItem(
                    livetalkChatItem =
                        LivetalkChatItem(
                            chatId = 7,
                            isMine = false,
                            message = "고척돔 분위기 너무 좋아요! 직관 중입니다 ⚾ 날씨도 완벽하고 경기도 재밌어요!",
                            profileImageUrl = "",
                            nickname = "직관러",
                            teamName = "키움",
                            timestamp = LocalDateTime.of(2025, 8, 6, 13, 10),
                        ),
                ),
                // 어제 밤 (늦은 시간) - 2025-08-05 22:30
                LivetalkChatBubbleItem.OtherBubbleItem(
                    livetalkChatItem =
                        LivetalkChatItem(
                            chatId = 15,
                            isMine = false,
                            message = "어제 경기 정말 인상깊었어요! 처음에는 우리 팀이 뒤지고 있어서 걱정했는데 중반부터 분위기가 완전히 바뀌었습니다.",
                            profileImageUrl = "",
                            nickname = "베테랑팬",
                            teamName = "롯데",
                            timestamp = LocalDateTime.of(2025, 8, 5, 22, 30),
                        ),
                ),
                // 어제 밤 - 2025-08-05 21:15
                LivetalkChatBubbleItem.OtherBubbleItem(
                    livetalkChatItem =
                        LivetalkChatItem(
                            chatId = 8,
                            isMine = false,
                            message = "9회말 역전 가능할까요?",
                            profileImageUrl = "",
                            nickname = "끝까지응원",
                            teamName = "한화",
                            timestamp = LocalDateTime.of(2025, 8, 5, 21, 15),
                        ),
                ),
                // 어제 오후 - 2025-08-05 17:19
                LivetalkChatBubbleItem.OtherBubbleItem(
                    livetalkChatItem =
                        LivetalkChatItem(
                            chatId = 12,
                            isMine = false,
                            message = "키움 선수들 컨디션 정말 좋아 보이네요. 특히 투수진이 안정적이어서 올 시즌 기대됩니다!",
                            profileImageUrl = "",
                            nickname = "야구매니아",
                            teamName = "키움",
                            timestamp = LocalDateTime.of(2025, 8, 5, 17, 19),
                        ),
                ),
                // 2일 전 - 2025-08-04 20:19
                LivetalkChatBubbleItem.OtherBubbleItem(
                    livetalkChatItem =
                        LivetalkChatItem(
                            chatId = 13,
                            isMine = false,
                            message = "오늘 직관하러 왔는데 분위기가 정말 끝내줘요! 팬들 응원소리도 크고 선수들도 열심히 뛰고 있어서 보는 재미가 쏠쏠합니다.",
                            profileImageUrl = "",
                            nickname = "직관러",
                            teamName = "키움",
                            timestamp = LocalDateTime.of(2025, 8, 4, 20, 19),
                        ),
                ),
                // 가장 오래된 메시지 (5일 전) - 2025-08-01 12:00
                LivetalkChatBubbleItem.OtherBubbleItem(
                    livetalkChatItem =
                        LivetalkChatItem(
                            chatId = 0,
                            isMine = false,
                            message = "야구를 보니 너무좋네요~",
                            profileImageUrl = "",
                            nickname = "이포르",
                            teamName = "KIA",
                            timestamp = LocalDateTime.of(2025, 8, 1, 12, 0),
                        ),
                ),
            ) // sortedBy 제거 - 이미 최신순으로 정렬됨
    }
}
