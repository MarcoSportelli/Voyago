package com.example.voyago.components

import android.appwidget.AppWidgetHost
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun Popup(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    title: String? = null,
    message: String,
    buttons: @Composable () -> Unit = {},
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    messageColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    dialogWidth: Float = 0.9f,
    animationDuration: Int = 300
) {
    AnimatedVisibility(
        visible = showDialog,
        enter = fadeIn(tween(animationDuration)) + expandVertically(
            expandFrom = Alignment.Bottom,
            animationSpec = tween(animationDuration)
        ),
        exit = fadeOut(tween(animationDuration)) + shrinkVertically(
            shrinkTowards = Alignment.Bottom,
            animationSpec = tween(animationDuration)
        )
    ) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 16.dp)
                    .widthIn(max = 600.dp),
                shape = RoundedCornerShape(16.dp),
                color = backgroundColor,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    title?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = titleColor
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = messageColor,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        buttons()
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationPopup(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String = "Confirm Elimination",
    message: String = "Are you sure do you want to delete this trip? This operation is irreversible!",
    confirmText: String = "Delete",
    cancelText: String = "Cancel"
) {
    Popup(
        showDialog = showDialog,
        onDismiss = onDismiss,
        title = title,
        message = message,
        backgroundColor = MaterialTheme.colorScheme.errorContainer,
        titleColor = MaterialTheme.colorScheme.onErrorContainer,
        messageColor = MaterialTheme.colorScheme.onErrorContainer,
        buttons = {
            Button(
                onClick = { onDismiss() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                elevation = null
            ) {
                Text(cancelText, color = Color.White)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(confirmText, color = Color.White)
            }
        }
    )
}