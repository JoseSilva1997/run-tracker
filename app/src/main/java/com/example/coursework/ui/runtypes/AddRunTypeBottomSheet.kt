package com.example.coursework.ui.runtypes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRunTypeBottomSheet (
    onSave: (name: String, distance: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var distanceError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text("Add Run Type", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { 
                    if (it.length <= 8) { // Prevent typing more than 8 chars
                        name = it
                        nameError = null // Clear error on typing
                    }
                },
                label = { Text("Run Type name (e.g., 5K)") },
                singleLine = true,
                isError = nameError != null,
                supportingText = {
                    if (nameError != null) {
                        Text(
                            text = nameError!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text("${name.length}/8")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = distance,
                onValueChange = { 
                    distance = it
                    distanceError = null // Clear error on typing
                },
                label = { Text("Target distance (meters)") },
                singleLine = true,
                isError = distanceError != null,
                supportingText = {
                    if (distanceError != null) {
                        Text(
                            text = distanceError!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    val trimmedName = name.trim()
                    
                    // We need to evaluate both to ensure both error states are updated
                    // if both fields are invalid at the same time.
                    val isNameValid = isValidName(trimmedName) { nameError = it }
                    val isDistanceValid = isValidDistance(distance) { distanceError = it }

                    if (isNameValid && isDistanceValid) {
                        onSave(trimmedName, distance.toInt())
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}

private fun isValidName(trimmedName: String, onError: (String?) -> Unit): Boolean {
    return when {
        trimmedName.isEmpty() -> {
            onError("Name cannot be empty")
            false
        }
        trimmedName.length > 8 -> {
            onError("Name must be 8 characters or less")
            false
        }
        else -> {
            onError(null)
            true
        }
    }
}

private fun isValidDistance(distanceStr: String, onError: (String?) -> Unit): Boolean {
    val distance = distanceStr.toIntOrNull()
    return when {
        distance == null -> {
            onError("Please enter a valid number")
            false
        }
        distance <= 0 -> {
            onError("Distance must be greater than 0")
            false
        }
        distance > 100000 -> { // Cap at 100km for sanity
            onError("Distance is too large (max 100,000m)")
            false
        }
        else -> {
            onError(null)
            true
        }
    }
}