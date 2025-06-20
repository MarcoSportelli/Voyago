package com.example.voyago.pages.createtravel

import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.voyago.viewModels.TravelViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun Step6Screen(viewModel: TravelViewModel) {
    val travel by viewModel.travel.collectAsState()
    var showDateRangePicker by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = "How long will the trip take?",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(screenHeight * 0.05f))

        Button(
            onClick = { showDateRangePicker = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
            ,
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(6.dp),
        ) {
            Text("Select Travel Dates", color = Color.White)
        }

        Spacer(modifier = Modifier.height(screenHeight * 0.05f))

        if (travel.startDate.isNotEmpty() && travel.endDate.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Card per la data di partenza
                Card(
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Start date",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Start: ${travel.startDate}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "To",
                    tint = Color.Black
                )

                // Card per la data di fine
                Card(
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "End date",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "End: ${travel.endDate}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        if (showDateRangePicker) {
            DateRangePickerDialog(
                onDateRangeSelected = { start, end ->
                    viewModel.updateDates(start, end)
                    showDateRangePicker = false
                },
                onDismissRequest = {
                    showDateRangePicker = false
                }
            )
        }
    }
}

@Composable
fun DateRangePickerDialog(
    onDateRangeSelected: (start: String, end: String) -> Unit,
    onDismissRequest: () -> Unit
) {
    AndroidView(
        factory = { ctx ->
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select travel dates")
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val startMillis = selection.first
            val endMillis = selection.second

            if (startMillis != null && endMillis != null) {
                val formatter = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH)
                val start = formatter.format(Date(startMillis))
                val end = formatter.format(Date(endMillis))
                onDateRangeSelected(start, end)
            } else {
                onDismissRequest()
            }
        }

        picker.addOnNegativeButtonClickListener {
            onDismissRequest()
        }

        picker.addOnCancelListener {
            onDismissRequest()
        }

        picker.show((ctx as AppCompatActivity).supportFragmentManager, picker.toString())
        FrameLayout(ctx)
    })
}
