package com.basset.core.navigation

import androidx.navigation3.runtime.NavKey
import com.basset.core.domain.model.MediaType
import com.basset.core.domain.model.OperationType
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute : NavKey

@Serializable
data class OperationRoute(
    val mediaType: MediaType = MediaType.VIDEO,
    val uri: String = "uri",
    val operationType: OperationType = OperationType.CUT
) : NavKey

