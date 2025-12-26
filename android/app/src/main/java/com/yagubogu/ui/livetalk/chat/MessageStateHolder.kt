package com.yagubogu.ui.livetalk.chat

import com.yagubogu.ui.livetalk.chat.model.LivetalkChatBubbleItem
import com.yagubogu.ui.livetalk.chat.model.LivetalkChatItem
import com.yagubogu.ui.livetalk.chat.model.LivetalkResponseItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

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
 */
class MessageStateHolder(
    val isVerified: Boolean = false,
) {
    private val _isInitialLoadCompleted = MutableStateFlow(false)
    val isInitialLoadCompleted: StateFlow<Boolean> = _isInitialLoadCompleted.asStateFlow()

    private val lock = Mutex()
    var hasNext: Boolean = true
        private set
    var oldestMessageCursor: Long? = null
        private set
    var newestMessageCursor: Long? = null
        private set

    private val _messageText = MutableStateFlow("")
    val messageText: StateFlow<String> = _messageText.asStateFlow()

    private val _livetalkChatBubbleItems =
        MutableStateFlow<List<LivetalkChatBubbleItem>>(emptyList())
    val livetalkChatBubbleItems: StateFlow<List<LivetalkChatBubbleItem>> = _livetalkChatBubbleItems

    private val _pendingReportChat = MutableStateFlow<LivetalkChatItem?>(null)
    val pendingReportChat: StateFlow<LivetalkChatItem?> = _pendingReportChat.asStateFlow()

    private val _pendingDeleteChat = MutableStateFlow<LivetalkChatItem?>(null)
    val pendingDeleteChat: StateFlow<LivetalkChatItem?> = _pendingDeleteChat.asStateFlow()

    private val pendingWriteChatIds = mutableSetOf<Long>()

    suspend fun addBeforeChats(response: LivetalkResponseItem) {
        _isInitialLoadCompleted.value = true
        val beforeChats: List<LivetalkChatBubbleItem> =
            response.cursor.chats.map { LivetalkChatBubbleItem.of(it) }

        lock.withLock {
            val currentChats: List<LivetalkChatBubbleItem> = livetalkChatBubbleItems.value
            _livetalkChatBubbleItems.value = currentChats + beforeChats

            hasNext = response.cursor.hasNext
            oldestMessageCursor = response.cursor.nextCursorId ?: oldestMessageCursor
            newestMessageCursor =
                currentChats.firstOrNull()?.livetalkChatItem?.chatId
        }
    }

    suspend fun addPendingWriteChat(pendingChat: LivetalkChatBubbleItem.MyPendingBubbleItem) {
        lock.withLock {
            val currentChats: List<LivetalkChatBubbleItem> = _livetalkChatBubbleItems.value
            pendingWriteChatIds.add(pendingChat.livetalkChatItem.chatId)
            _livetalkChatBubbleItems.value = listOf(pendingChat) + currentChats
        }
    }

    private suspend fun clearPendingWriteChats() {
        lock.withLock {
            pendingWriteChatIds.forEach { chatId ->
                val currentChats: List<LivetalkChatBubbleItem> = _livetalkChatBubbleItems.value
                _livetalkChatBubbleItems.value =
                    currentChats.filter { it.livetalkChatItem.chatId != chatId }
            }
            pendingWriteChatIds.clear()
        }
    }

    suspend fun addAfterChats(response: LivetalkResponseItem) {
        _isInitialLoadCompleted.value = true
        val newChats: List<LivetalkChatBubbleItem> =
            response.cursor.chats.map { LivetalkChatBubbleItem.of(it) }

        if (newChats.isEmpty()) return

        clearPendingWriteChats()
        lock.withLock {
            val currentChats: List<LivetalkChatBubbleItem> =
                _livetalkChatBubbleItems.value
            _livetalkChatBubbleItems.value = newChats + currentChats

            hasNext = response.cursor.hasNext
            newestMessageCursor = newChats.first().livetalkChatItem.chatId
            oldestMessageCursor = oldestMessageCursor ?: newChats.last().livetalkChatItem.chatId
        }
    }

    suspend fun deleteChat(chatId: Long) {
        lock.withLock {
            val currentChats: List<LivetalkChatBubbleItem> =
                _livetalkChatBubbleItems.value
            val deletedChats: List<LivetalkChatBubbleItem> =
                currentChats.filter { it.livetalkChatItem.chatId != chatId }

            newestMessageCursor = deletedChats.firstOrNull()?.livetalkChatItem?.chatId
            oldestMessageCursor = deletedChats.lastOrNull()?.livetalkChatItem?.chatId
            _livetalkChatBubbleItems.value = deletedChats
            _pendingDeleteChat.value = null
        }
    }

    suspend fun reportChat(chatId: Long) {
        lock.withLock {
            val currentChats: List<LivetalkChatBubbleItem> =
                _livetalkChatBubbleItems.value
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
            _livetalkChatBubbleItems.value = updatedChats
            _pendingReportChat.value = null
        }
    }

    fun requestDelete(chat: LivetalkChatItem) {
        _pendingDeleteChat.value = chat
        Timber.d("삭제 요청: ${chat.chatId}")
    }

    fun dismissDeleteDialog() {
        _pendingDeleteChat.value = null
    }

    fun requestReport(chat: LivetalkChatItem) {
        _pendingReportChat.value = chat
        Timber.d("신고 요청: ${chat.chatId}")
    }

    fun dismissReportDialog() {
        _pendingReportChat.value = null
    }

    fun updateMessageText(text: String) {
        _messageText.value = text
    }
}
