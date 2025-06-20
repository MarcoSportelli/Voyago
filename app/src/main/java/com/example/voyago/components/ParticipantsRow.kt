package com.example.voyago.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ParticipantsRow(
    participantType: String,
    onClickPlus: () -> Unit = {},
    onClickMinus: () -> Unit = {},
    count: Int,
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 30.dp, bottom = 30.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
        ) {
            Text(
                participantType,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(
        ) {
            ParticipantsNumberSelector(count, onClickPlus, onClickMinus)
        }
    }
}

@Composable
fun ParticipantsNumberSelector(count: Int, onClickPlus: ()->Unit, onClickMinus: ()->Unit){

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White,
            onClick = onClickMinus,
            modifier = Modifier
                .size(50.dp)
                .shadow(2.dp, shape = CircleShape),

            ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Minus",
                tint = Color.Black,
                modifier = Modifier.padding(8.dp)
            )
        }

        Text(count.toString(),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        Surface(
            shape = CircleShape,
            color = Color.White,
            onClick = onClickPlus,
            modifier = Modifier
                .size(50.dp)
                .shadow(2.dp, shape = CircleShape),

            ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Minus",
                tint = Color.Black,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}