package com.yagubogu.stat.dto;

import java.util.List;

public record SyncBatchData(List<UpdateDto> toUpdate, List<InsertDto> toInsert) {
}
