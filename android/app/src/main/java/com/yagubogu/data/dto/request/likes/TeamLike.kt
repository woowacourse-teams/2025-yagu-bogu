import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TeamLike(
    @SerialName("teamId")
    val teamId: Long, // 좋아요가 적용되는 팀 ID (홈팀=1, 원정팀=2)  gameId에 해당하는 팀 2개.
    @SerialName("count")
    val count: Int, // Long	좋아요의 증감 수치.
)
