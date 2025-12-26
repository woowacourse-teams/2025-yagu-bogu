package com.yagubogu.presentation.mapper

import com.yagubogu.data.dto.response.talk.CursorResultTalkDto
import com.yagubogu.data.dto.response.talk.TalkCursorResponse
import com.yagubogu.data.dto.response.talk.TalkEntranceResponse
import com.yagubogu.data.dto.response.talk.TalkResponse
import com.yagubogu.presentation.livetalk.chat.model.LivetalkChatItem
import com.yagubogu.presentation.livetalk.chat.model.LivetalkCursorItem
import com.yagubogu.presentation.livetalk.chat.model.LivetalkResponseItem
import com.yagubogu.presentation.livetalk.chat.model.LivetalkTeams
import java.time.LocalDateTime

fun TalkCursorResponse.toUiModel(): LivetalkResponseItem =
    LivetalkResponseItem(
        cursor = cursorResult.toUiModel(),
    )

fun CursorResultTalkDto.toUiModel(): LivetalkCursorItem =
    LivetalkCursorItem(
        chats = contents.map { it.toUiModel() },
        nextCursorId = nextCursorId,
        hasNext = hasNext,
    )

fun TalkResponse.toUiModel(): LivetalkChatItem =
    LivetalkChatItem(
        chatId = id,
        memberId = memberId,
        isMine = isMine,
        message = content,
        profileImageUrl = imageUrl,
        nickname = nickname,
        teamName = favorite,
        timestamp = LocalDateTime.parse(createdAt),
        reported = content.contains("숨김처리되었습니다"), // TODO : 추후 백엔드에서 신고된 메시지 보내주는 방식 변경 필요
    )

fun TalkEntranceResponse.toUiModel(): LivetalkTeams =
    LivetalkTeams(
        stadiumName = stadiumName,
        homeTeamCode = homeTeamCode,
        awayTeamCode = awayTeamCode,
        myTeamCode = myTeamCode,
    )
