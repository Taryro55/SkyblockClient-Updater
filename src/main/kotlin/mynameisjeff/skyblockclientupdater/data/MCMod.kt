package mynameisjeff.skyblockclientupdater.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MCMod(
    @SerialName("modid")
    val modId: String,
)
