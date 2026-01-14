package com.yagubogu.talk.dto;

import java.util.List;

public record CursorResultParam<T>(
        List<T> content,
        Long nextCursorId,
        boolean hasNext
) {
}
