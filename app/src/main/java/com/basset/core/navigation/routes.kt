package com.basset.core.navigation

import com.basset.core.domain.model.MimeType
import com.basset.core.domain.model.OperationType
import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
data class OperationRoute(
    val mimeType: MimeType = MimeType.VIDEO,
    val uri: String = "uri",
    val operationType: OperationType = OperationType.CUT
)

