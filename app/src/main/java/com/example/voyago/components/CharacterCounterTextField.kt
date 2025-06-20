package com.example.voyago.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun CharacterCounterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    modifier2: Modifier,
    label: String = "",
    placeholder: String = "",
    maxCharacters: Int = 500,
    singleLine: Boolean = false
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { newText ->
                if (newText.length <= maxCharacters) {
                    onValueChange(newText)
                }
            },
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            modifier = modifier2,
            singleLine = singleLine,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text
            )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, start = 4.dp)
        ) {
            Text(
                text = "${value.length}/$maxCharacters",
                style = MaterialTheme.typography.bodySmall,
                color = if (value.length > maxCharacters * 0.9) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterStart)
            )
        }
    }
}