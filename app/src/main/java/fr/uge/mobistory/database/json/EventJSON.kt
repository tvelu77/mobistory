package fr.uge.mobistory.database.json

import kotlinx.serialization.Contextual
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.Optional

@Serializable

data class EventJSON (
    @Required val id: Int,
    @Required val label: String,
    @Required val description: String,
    val wikipedia: String? = null,
    val aliases: String? = null,
    val popularity: Map<String, Int> = HashMap(),
    val sourceId: Int,
    val claims: List<Claim> = ArrayList()
    )

@Serializable
data class Claim(val id : Int, val verboseName: String, val item : EventJSON? = null, val value: String, val formatterUrl: String? = null, val qualifiers: List<Claim> = ArrayList())
