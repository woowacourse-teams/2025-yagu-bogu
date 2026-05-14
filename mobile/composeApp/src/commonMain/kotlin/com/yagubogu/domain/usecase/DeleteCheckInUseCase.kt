package com.yagubogu.domain.usecase

import com.yagubogu.data.repository.checkin.CheckInRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class DeleteCheckInUseCase(
    private val checkInRepository: CheckInRepository,
) {
    suspend operator fun invoke(
        checkInId: Long,
        imageIds: List<Long>,
    ): Result<Unit> {
        // 직관 기록(이미지, 메모) 먼저 병렬로 삭제 시도
        val diaryResults =
            coroutineScope {
                val imageJobs =
                    imageIds.map { id: Long ->
                        async {
                            checkInRepository.deleteImage(checkInId, id)
                        }
                    }
                val memoJob = async { checkInRepository.deleteMemo(checkInId) }

                (imageJobs + memoJob).awaitAll()
            }

        // 직관 기록 삭제 중 하나라도 실패했다면, 직관 내역 삭제를 진행하지 않고 즉시 실패 반환
        val diaryFailure = diaryResults.firstOrNull { it.isFailure }
        if (diaryFailure != null) {
            return diaryFailure
        }

        // 직관 기록(이미지, 메모)을 모두 성공적으로 삭제했다면 최종적으로 직관 내역 삭제
        return checkInRepository.deleteCheckIn(checkInId)
    }
}
