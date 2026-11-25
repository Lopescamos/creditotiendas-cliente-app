package pe.edu.upc.creditotiendas.cliente.model.beans

data class CreditProfileResponse(
    val id: Long,
    val storeId: Long,
    val clientUserId: Long,
    val clientFullName: String?,
    val clientDocument: String?,
    val currencyId: Long?,
    val creditLimit: Double,
    val availableCredit: Double,
    val interestRate: Double,
    val interestRateType: String?,          // NOMINAL | EFFECTIVE
    val capitalizationPeriod: String?,
    val lateInterestRate: Double,
    val lateInterestRateType: String?,
    val maxTermDays: Int,
    val cutoffDay: Int,
    val paymentDay: Int,
    val status: String                      // ACTIVE | SUSPENDED | BLOCKED | CLOSED
)
