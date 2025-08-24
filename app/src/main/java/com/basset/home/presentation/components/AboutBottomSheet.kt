package com.basset.home.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.basset.BuildConfig
import com.basset.R

@Composable
fun AboutBottomSheet() {
    Column {
        AboutTextButton(
            titleId = R.string.about_email_title,
            descriptionId = R.string.about_email_description,
            url = "mailto:mohammadamansour03@gmail.com",
            iconId = R.drawable.at
        )
        AboutTextButton(
            titleId = R.string.about_github_title,
            descriptionId = R.string.about_github_description,
            url = "https://github.com/mohammadmansour200/basset-mobile-kotlin",
            iconId = R.drawable.github
        )
        Text(
            text = stringResource(R.string.about_prayer_note),
            style = MaterialTheme.typography.bodySmall,
            color = LocalContentColor.current.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 4.dp)
        )
        Text(
            text = "v${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp),
            color = LocalContentColor.current.copy(alpha = 0.5f)
        )
    }
}