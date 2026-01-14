package com.yagubogu.talk.dto.v1;

import com.yagubogu.talk.dto.CursorResultParam;

public record TalkCursorResultResponse(CursorResultParam<TalkResponse> cursorResult) {
}
