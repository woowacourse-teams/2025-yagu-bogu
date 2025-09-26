import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LikeUpdateRequest(
    @SerialName("windowStartEpochSec")
    val windowStartEpochSec: Long, // 윈도우 시작 시간 (Epoch Second 단위, 0 이상)
    @SerialName("likeDelta")
    val likeDelta: LikeDelta,
)
