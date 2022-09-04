package com.sinasamaki.swipedismiss

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ListItem(
    title: String,
    subtitle: String,
) {
    Column {

        Row(
            modifier = Modifier
                .clickable {  }
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column(
                Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.subtitle1,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.alpha(.64f)
                )
            }

            Text(
                text = "5:00",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.alpha(.56f),
                fontSize = 12.sp
            )
        }

        Divider(
            thickness = 0.dp
        )
    }
}