package pe.edu.upc.creditotiendas.cliente.model.beans

data class PaymentDueBreakdown(
    val installmentId: Long,
    val installmentNumber: Int,
    val dueDate: String,
    val capital: Double,
    val interest: Double,
    val lateFee: Double,
    val daysOverdue: Int,
    val total: Double
)

data class PaymentDueResponse(
    val creditProfileId: Long,
    val asOf: String,
    val items: List<PaymentDueBreakdown>,
    val totalAmount: Double
)

data class RegisterPaymentRequest(val installmentIds: List<Long>)

data class PaymentAllocation(
    val installmentId: Long,
    val installmentNumber: Int?,
    val lateFeeAmount: Double,
    val interestAmount: Double,
    val capitalAmount: Double
)

data class PaymentResponse(
    val id: Long,
    val creditProfileId: Long,
    val creditSaleId: Long,
    val paymentDate: String,
    val totalAmount: Double,
    val allocations: List<PaymentAllocation>
)
