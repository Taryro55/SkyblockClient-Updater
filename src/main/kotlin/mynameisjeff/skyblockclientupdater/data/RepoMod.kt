package mynameisjeff.skyblockclientupdater.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RepoMod(
    @SerialName("file")
    val fileName: String,
    @SerialName("forge_id")
    val modId: String? = null,
    @SerialName("url")
    val updateURL: String = "https://github.com/Taryro55/SkyblockClient-REPO/raw/main/files/mods/$fileName",
    @SerialName("update_to_ids")
    val updateToIds: Array<String> = arrayOf<String>(),
    val ignored: Boolean = false,
    val hasBrokenMCModInfo: Boolean = false,
    val alwaysConsider: Boolean = false,
)
