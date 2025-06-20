package com.example.voyago.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em

@Composable
fun NavigationBar(
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    pressable: Boolean
){

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .drawBehind {
                // Disegna solo il bordo superiore
                drawLine(
                    color = Color.LightGray,
                    strokeWidth = 1.dp.toPx(),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f)
                )
            }
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = onBackClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp),
            shape = RoundedCornerShape(8.dp),

            ) {
            Text("Back",
                fontSize = 5.em,
                textDecoration = TextDecoration.Underline,
                color = Color.Black
            )
        }

        Button(
            onClick = onNextClick,
            enabled = pressable,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (pressable) MaterialTheme.colorScheme.primary else Color.LightGray,
                contentColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.size(width = 120.dp, height = 50.dp)
        ) {
            Text(
                "Next",
                fontSize = 4.em,
                color = Color.White
            )
        }
    }
}

@Composable
fun NavigationBarPublish(
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
){

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .drawBehind {
                // Disegna solo il bordo superiore
                drawLine(
                    color = Color.LightGray,
                    strokeWidth = 1.dp.toPx(),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f)
                )
            }
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = onBackClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp),
            shape = RoundedCornerShape(8.dp),

            ) {
            Text("Back",
                fontSize = 5.em,
                textDecoration = TextDecoration.Underline,
                color = Color.Black
            )
        }

        Button(
            onClick = onNextClick,
            enabled = true,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.size(width = 120.dp, height = 50.dp)
        ) {
            Text(
                "Publish",
                fontSize = 4.em,
                color = Color.White
            )
        }
    }
}