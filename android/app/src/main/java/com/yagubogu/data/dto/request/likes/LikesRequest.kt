import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LikesRequest(
    @SerialName("likes")
    val likes: List<TeamLike>,
)