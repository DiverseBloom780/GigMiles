package com.gigmiles.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gigmiles.app.data.DriveRecord

enum class DeliveryApp { SPARK, DOORDASH }

data class Earnings(
    val basePay: String = "", val tips: String = "", val boost: String = "",
    val incentives: String = "", val appPay: String = ""
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { GigMilesScreen() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GigMilesScreen() {
    val viewModel: GigMilesViewModel = viewModel()
    val savedDrives by viewModel.drives.collectAsState()
    var selectedApp by remember { mutableStateOf(DeliveryApp.SPARK) }
    var tracking by remember { mutableStateOf(false) }
    var startedAt by remember { mutableLongStateOf(0L) }
    var miles by remember { mutableStateOf("0.0") }
    var earnings by remember { mutableStateOf(Earnings()) }

    MaterialTheme {
        Scaffold(topBar = { TopAppBar(title = { Text("GigMiles") }) }) { padding ->
            Column(Modifier.padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Track a drive", style = MaterialTheme.typography.headlineSmall)
                SummaryRow(savedDrives)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selectedApp == DeliveryApp.SPARK, { selectedApp = DeliveryApp.SPARK }, label = { Text("Spark") })
                    FilterChip(selectedApp == DeliveryApp.DOORDASH, { selectedApp = DeliveryApp.DOORDASH }, label = { Text("DoorDash") })
                }
                Button(onClick = {
                    tracking = !tracking
                    if (tracking) startedAt = System.currentTimeMillis()
                }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (tracking) "End Drive" else "Start Drive")
                }
                if (!tracking) {
                    OutlinedTextField(miles, { miles = it }, label = { Text("Miles driven") }, modifier = Modifier.fillMaxWidth())
                    if (selectedApp == DeliveryApp.SPARK) {
                        MoneyField("Spark base pay") { earnings = earnings.copy(basePay = it) }
                        MoneyField("Spark tips") { earnings = earnings.copy(tips = it) }
                        MoneyField("Boost") { earnings = earnings.copy(boost = it) }
                        MoneyField("Incentives") { earnings = earnings.copy(incentives = it) }
                    } else {
                        MoneyField("DoorDash pay") { earnings = earnings.copy(appPay = it) }
                        MoneyField("Customer tips") { earnings = earnings.copy(tips = it) }
                    }
                    OutlinedButton(onClick = {
                        val app = if (selectedApp == DeliveryApp.SPARK) "Spark" else "DoorDash"
                        viewModel.saveDrive(
                            DriveRecord(
                                app = app,
                                startedAt = startedAt,
                                endedAt = System.currentTimeMillis(),
                                miles = miles.toDoubleOrNull() ?: 0.0,
                                basePay = earnings.basePay.toDoubleOrNull() ?: 0.0,
                                customerTips = earnings.tips.toDoubleOrNull() ?: 0.0,
                                boost = earnings.boost.toDoubleOrNull() ?: 0.0,
                                incentives = earnings.incentives.toDoubleOrNull() ?: 0.0,
                                appPay = earnings.appPay.toDoubleOrNull() ?: 0.0
                            )
                        )
                        miles = "0.0"
                        earnings = Earnings()
                        startedAt = 0L
                    }, enabled = startedAt != 0L, modifier = Modifier.fillMaxWidth()) {
                        Text("Save Drive")
                    }
                }
                Text("Saved drives: ${savedDrives.size}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun SummaryRow(drives: List<DriveRecord>) {
    val spark = drives.filter { it.app == "Spark" }
    val doorDash = drives.filter { it.app == "DoorDash" }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        SummaryCard("Spark", spark.sumOf { it.miles }, spark.sumOf { it.totalPay() }, Modifier.weight(1f))
        SummaryCard("DoorDash", doorDash.sumOf { it.miles }, doorDash.sumOf { it.totalPay() }, Modifier.weight(1f))
    }
}

@Composable
private fun SummaryCard(app: String, miles: Double, pay: Double, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(app, style = MaterialTheme.typography.titleMedium)
            Text("${"%.1f".format(miles)} mi")
            Text("$${"%.2f".format(pay)}")
        }
    }
}

private fun DriveRecord.totalPay(): Double =
    basePay + customerTips + boost + incentives + appPay

@Composable
private fun MoneyField(label: String, onValueChange: (String) -> Unit) {
    OutlinedTextField("", onValueChange, label = { Text("$label ($)") }, modifier = Modifier.fillMaxWidth())
}
