package pe.edu.upc.creditotiendas.cliente.model.beans

// --- Request / Response de autenticación (coincide con IAM backend) ---
data class SignInRequest(val email: String, val password: String)

data class SignInResponse(
    val id: Long,
    val email: String,
    val fullName: String?,
    val firstName: String?,
    val lastName: String?,
    val role: String,
    val token: String,
    val storeIds: List<Long>?
)

data class MeResponse(
    val id: Long,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val role: String,
    val active: Boolean,
    val storeIds: List<Long>?
)
