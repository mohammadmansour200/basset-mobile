package com.basset.core.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.net.toFile

fun Uri.getFileName(context: Context): String {
    when (scheme) {
        ContentResolver.SCHEME_FILE -> {
            return toFile().nameWithoutExtension
        }

        ContentResolver.SCHEME_CONTENT -> {
            val cursor = context.contentResolver.query(
                this,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            ) ?: throw Exception("Failed to obtain cursor from the content resolver")
            cursor.moveToFirst()
            if (cursor.count == 0) {
                throw Exception("The given Uri doesn't represent any file")
            }
            val displayNameColumnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val displayName = cursor.getString(displayNameColumnIndex)
            cursor.close()
            return displayName.substringBeforeLast(".")
        }

        ContentResolver.SCHEME_ANDROID_RESOURCE -> {
            // for uris like [android.resource://com.example.app/1234567890]
            var resourceId = lastPathSegment?.toIntOrNull()
            if (resourceId != null) {
                return context.resources.getResourceName(resourceId)
            }
            // for uris like [android.resource://com.example.app/raw/sample]
            val packageName = authority
            val resourceType = if (pathSegments.size >= 1) {
                pathSegments[0]
            } else {
                throw Exception("Resource type could not be found")
            }
            val resourceEntryName = if (pathSegments.size >= 2) {
                pathSegments[1]
            } else {
                throw Exception("Resource entry name could not be found")
            }
            resourceId = context.resources.getIdentifier(
                resourceEntryName,
                resourceType,
                packageName
            )
            return context.resources.getResourceName(resourceId)
        }

        else -> {
            // probably a http uri
            return toString().substringBeforeLast(".").substringAfterLast("/")
        }
    }
}