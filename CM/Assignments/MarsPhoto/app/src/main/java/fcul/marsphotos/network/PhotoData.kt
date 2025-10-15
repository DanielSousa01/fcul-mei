package fcul.marsphotos.network

sealed interface PhotoData {
    val id: String
    val imgSrc: String
}