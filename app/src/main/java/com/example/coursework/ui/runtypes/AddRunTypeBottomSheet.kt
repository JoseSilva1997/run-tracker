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

// Capped because auto-finish triggers when measured distance reaches the target — an unreachable
// target would mean a run that never ends.
private const val MAX_DISTANCE_METERS = 100_000
private const val MAX_NAME_LENGTH = 8

// Bottom sheet form for creating a new run type.
// Validates the name and target distance before handing them back via onSave.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRunTypeBottomSheet (
    onSave: (name: String, distanceMeters: Float) -> Unit,
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
                    if (it.length <= MAX_NAME_LENGTH) {
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
                        Text("${name.length}/$MAX_NAME_LENGTH")
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
                        onSave(trimmedName, distance.toInt().toFloat())
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

// Checks the trimmed name is non-empty and within the length cap, reporting any failure through
// onError.
private fun isValidName(trimmedName: String, onError: (String?) -> Unit): Boolean {
    return when {
        trimmedName.isEmpty() -> {
            onError("Name cannot be empty")
            false
        }
        trimmedName.length > MAX_NAME_LENGTH -> {
            onError("Name must be $MAX_NAME_LENGTH characters or less")
            false
        }
        else -> {
            onError(null)
            true
        }
    }
}

// Parses and bounds-checks the target distance. Validated as an Int rather than a Float because the
// UI doesn't accept fractional metres — whole-number targets only.
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
        distance > MAX_DISTANCE_METERS -> { // Matches MAX_DISTANCE_METERS so the target is always
                                            // something a run can actually hit.
            onError("Distance is too large (max ${MAX_DISTANCE_METERS}m)")
            false
        }
        else -> {
            onError(null)
            true
        }
    }
}