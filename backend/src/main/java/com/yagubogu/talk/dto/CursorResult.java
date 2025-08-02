package com.yagubogu.talk.dto;

import java.util.List;

public record CursorResult<T>(
        List<T> content,
        Long nextCursorId,
        boolean hasNext
) {
}
