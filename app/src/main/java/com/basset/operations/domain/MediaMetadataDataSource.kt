package com.basset.operations.domain

import android.net.Uri
import com.basset.core.domain.model.MimeType
import com.basset.operations.domain.model.Metadata

interface MediaMetadataDataSource {
    suspend fun loadMetadata(uri: Uri, mimeType: MimeType): Metadata?
}
