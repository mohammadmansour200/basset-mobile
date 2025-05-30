package com.basset.core.navigation

import com.basset.home.presentation.components.MimeType
import com.basset.home.presentation.components.OperationType
import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
data class OperationRoute(
    val mimeType: MimeType = MimeType.VIDEO,
    val uri: String = "uri",
    val operationType: OperationType = OperationType.CUT
)

