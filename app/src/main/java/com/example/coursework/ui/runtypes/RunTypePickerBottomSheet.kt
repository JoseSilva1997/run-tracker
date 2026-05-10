package com.example.coursework.ui.runtypes

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.coursework.domain.model.RunType
import com.example.coursework.ui.theme.TextPrimary
import com.example.coursework.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunTypePickerBottomSheet(
    options: List<RunType>,
    selectedRunTypeName: String,
    onRunTypeSelected: (String) -> Unit,
    onAddRunType: () -> Unit,
    onDeleteRequest: (RunType) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Choose run type",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            if (options.isEmpty()) {
                Text(
                    text = "Create a run type to start tracking.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
            } else {
                options.forEach { option ->
                    ListItem(
                        headlineContent = { Text(option.name) },
                        leadingContent = {
                            RadioButton(
                                selected = option.name == selectedRunTypeName,
                                onClick = null
                            )
                        },
                        trailingContent = {
                            IconButton(onClick = { onDeleteRequest(option) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete ${option.name}"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = LocalIndication.current
                            ) { onRunTypeSelected(option.name) }
                    )
                }
            }

            OutlinedButton(
                onClick = onAddRunType,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add new run type")
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
