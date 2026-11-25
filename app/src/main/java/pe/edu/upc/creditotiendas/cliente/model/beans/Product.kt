package pe.edu.upc.creditotiendas.cliente.model.beans

data class ProductResponse(
    val id: Long,
    val storeId: Long,
    val name: String,
    val description: String?,
    val type: String,                       // PRODUCT | SERVICE
    val supplier: String?,
    val brand: String?,
    val measurementUnitId: Long?,
    val cashPrice: Double,
    val creditPrice: Double,
    val imageUrl: String?,
    val active: Boolean
)
