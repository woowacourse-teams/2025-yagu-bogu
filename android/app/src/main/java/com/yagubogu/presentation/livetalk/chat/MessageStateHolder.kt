package com.yagubogu.presentation.livetalk.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.yagubogu.presentation.livetalk.chat.model.LivetalkChatBubbleItem
import com.yagubogu.presentation.livetalk.chat.model.LivetalkReportEvent
import com.yagubogu.presentation.livetalk.chat.model.LivetalkResponseItem
import com.yagubogu.presentation.util.livedata.MutableSingleLiveData
import com.yagubogu.presentation.util.livedata.SingleLiveData
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 현장톡 채팅 화면의 메시지 상태를 관리합니다.
 *
 * 이 클래스는 채팅 메시지 목록을 관리하고, 과거 및 신규 메시지에 대한 페이지네이션을 처리하며,
 * 메시지 입력 폼의 상태를 관리하는 책임을 가집니다. [Mutex]를 사용하여 메시지 목록에 대한
 * 스레드 안전한 업데이트를 보장합니다.
 *
 * @param isVerified 현재 사용자가 메시지를 보낼 수 있는 인증된 사용자인지 여부를 나타내는 Boolean 값.
 *                   이 값은 `canSendMessage` 상태를 제어합니다.
 *
 * @property hasNext 불러올 이전 메시지가 더 있는지 여부를 나타냅니다.
 * @property oldestMessageCursor 이전 메시지를 가져오기 위한 커서 ID (페이지네이션용).
 * @property newestMessageCursor 최신 메시지를 가져오기 위한 커서 ID.
 * @property messageFormText 메시지 입력 필드의 현재 텍스트를 담고 있는 [MutableLiveData].
 * @property canSendMessage 사용자가 인증되었고 메시지 입력이 비어있지 않을 때 true가 되는 [LiveData].
 * @property liveTalkChatBubbleItems 화면에 표시될 현재 채팅 메시지 목록을 담고 있는 [LiveData].
 */
class MessageStateHolder(
    private val isVerified: Boolean = false,
) {
    private val lock = Mutex()
    var hasNext: Boolean = true
        private set
    var oldestMessageCursor: Long? = null
        private set
    var newestMessageCursor: Long? = null
        private set

    val messageFormText = MutableLiveData<String>()
    val canSendMessage: LiveData<Boolean> =
        messageFormText.map { isVerified && !it.isNullOrBlank() }

    private val _liveTalkChatBubbleItems = MutableLiveData<List<LivetalkChatBubbleItem>>()
    val liveTalkChatBubbleItems: LiveData<List<LivetalkChatBubbleItem>> get() = _liveTalkChatBubbleItems

    private val _livetalkReportEvent = MutableSingleLiveData<LivetalkReportEvent>()
    val livetalkReportEvent: SingleLiveData<LivetalkReportEvent> get() = _livetalkReportEvent

    private val _livetalkDeleteEvent = MutableSingleLiveData<Unit>()
    val livetalkDeleteEvent: SingleLiveData<Unit> get() = _livetalkDeleteEvent

    suspend fun addBeforeChats(response: LivetalkResponseItem) {
        val beforeChats: List<LivetalkChatBubbleItem> =
            response.cursor.chats.map { LivetalkChatBubbleItem.of(it) }

        lock.withLock {
            val currentChats: List<LivetalkChatBubbleItem> =
                liveTalkChatBubbleItems.value ?: emptyList()

            _liveTalkChatBubbleItems.value = currentChats + beforeChats

            hasNext = response.cursor.hasNext
            oldestMessageCursor = response.cursor.nextCursorId
            newestMessageCursor =
                currentChats.firstOrNull()?.livetalkChatItem?.chatId
        }
    }

    suspend fun addAfterChats(response: LivetalkResponseItem) {
        val newChats: List<LivetalkChatBubbleItem> =
            response.cursor.chats.map { LivetalkChatBubbleItem.of(it) }

        lock.withLock {
            if (newChats.isNotEmpty()) {
                val currentChats = _liveTalkChatBubbleItems.value ?: emptyList()
                _liveTalkChatBubbleItems.value = newChats + currentChats

                newestMessageCursor = newChats.first().livetalkChatItem.chatId
            }
        }
    }

    suspend fun deleteChat(chatId: Long) {
        lock.withLock {
            val currentChats: List<LivetalkChatBubbleItem> =
                _liveTalkChatBubbleItems.value ?: emptyList()
            val deletedChats: List<LivetalkChatBubbleItem> =
                currentChats.filter { it.livetalkChatItem.chatId != chatId }

            newestMessageCursor = deletedChats.firstOrNull()?.livetalkChatItem?.chatId
            oldestMessageCursor = deletedChats.lastOrNull()?.livetalkChatItem?.chatId
            _liveTalkChatBubbleItems.value = deletedChats
            _livetalkDeleteEvent.setValue(Unit)
        }
    }

    suspend fun reportChat(chatId: Long) {
        lock.withLock {
            val currentChats: List<LivetalkChatBubbleItem> =
                _liveTalkChatBubbleItems.value ?: emptyList()
            val updatedChats: List<LivetalkChatBubbleItem> =
                currentChats.map { chatBubbleItem: LivetalkChatBubbleItem ->
                    if (chatBubbleItem.livetalkChatItem.chatId == chatId) {
                        val updatedChatItem =
                            chatBubbleItem.livetalkChatItem.copy(
                                reported = true,
                                message = "숨김처리되었습니다",
                            )
                        LivetalkChatBubbleItem.OtherBubbleItem(updatedChatItem)
                    } else {
                        chatBubbleItem
                    }
                }
            _liveTalkChatBubbleItems.value = updatedChats
        }
    }

    fun updateLivetalkReportEvent(event: LivetalkReportEvent) {
        _livetalkReportEvent.setValue(event)
    }
}
