package fr.uge.mobistory.wiki

import kotlinx.serialization.Serializable

@Serializable
data class ImagePath(
    val imageName: String,
    val imageURL: String
)
