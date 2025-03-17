data class LoginResponse(
    val status: String,
    val message: String,
    val email: String? = null,
    val name: String? = null,
    val preferredCity: String? = null
)