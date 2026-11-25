package pe.edu.upc.creditotiendas.cliente.model.beans

data class AccountStatementResponse(
    val id: Long,
    val creditProfileId: Long,
    val period: String,                     // YYYY-MM
    val previousBalance: Double,
    val periodPurchases: Double,
    val periodPayments: Double,
    val periodInterest: Double,
    val periodLateFees: Double,
    val finalBalance: Double,
    val generatedAt: String
)
