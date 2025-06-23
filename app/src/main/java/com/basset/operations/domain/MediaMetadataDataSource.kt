package com.basset.operations.domain

import android.net.Uri
import com.basset.core.domain.model.MediaType
import com.basset.operations.domain.model.Metadata

interface MediaMetadataDataSource {
    suspend fun loadMetadata(uri: Uri, mediaType: MediaType): Metadata?
}
