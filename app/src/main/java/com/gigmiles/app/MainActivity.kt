package com.gigmiles.app

import android.Manifest
import android.os.Bundle
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import com.gigmiles.app.data.DriveRecord
import com.gigmiles.app.location.DriveLocationTracker
import com.google.android.gms.location.LocationServices

enum class DeliveryApp { SPARK, DOORDASH }
private enum class AppTab { TRACK, MAP, HISTORY, EXPENSES }

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
    val context = LocalContext.current
    val viewModel: GigMilesViewModel = viewModel()
    val savedDrives by viewModel.drives.collectAsState()
    var selectedApp by remember { mutableStateOf(DeliveryApp.SPARK) }
    var activeTab by remember { mutableStateOf(AppTab.TRACK) }
    var tracking by remember { mutableStateOf(false) }
    var showMap by remember { mutableStateOf(false) }
    var startedAt by remember { mutableLongStateOf(0L) }
    var liveMiles by remember { mutableDoubleStateOf(0.0) }
    var miles by remember { mutableStateOf("0.0") }
    var earnings by remember { mutableStateOf(Earnings()) }
    val locationTracker = remember {
        DriveLocationTracker(LocationServices.getFusedLocationProviderClient(context)) { liveMiles = it }
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            startedAt = System.currentTimeMillis()
            liveMiles = 0.0
            locationTracker.start()
            tracking = true
        }
    }

    MaterialTheme {
        Scaffold(
            topBar = { TopAppBar(title = { Text("GigMiles") }) },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(activeTab == AppTab.TRACK, { activeTab = AppTab.TRACK }, label = { Text("Track") }, icon = { Text("▶") })
                    NavigationBarItem(activeTab == AppTab.MAP, { activeTab = AppTab.MAP }, label = { Text("Map") }, icon = { Text("⌖") })
                    NavigationBarItem(activeTab == AppTab.HISTORY, { activeTab = AppTab.HISTORY }, label = { Text("History") }, icon = { Text("☷") })
                    NavigationBarItem(activeTab == AppTab.EXPENSES, { activeTab = AppTab.EXPENSES }, label = { Text("Expenses") }, icon = { Text("$") })
                }
            }
        ) { padding ->
            if (activeTab == AppTab.TRACK) {
            Column(Modifier.padding(padding).padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("GigMiles", style = MaterialTheme.typography.headlineLarge)
                Text("Your delivery mileage and earnings", style = MaterialTheme.typography.bodyMedium)
                HorizontalDivider()
                Text("Totals", style = MaterialTheme.typography.titleLarge)
                OutlinedButton(onClick = { showMap = !showMap }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (showMap) "Hide Map" else "Show Map")
                }
                if (showMap) MapScreen(Modifier.fillMaxWidth().height(240.dp))
                SummaryRow(savedDrives)
                HorizontalDivider()
                Text("New drive", style = MaterialTheme.typography.titleLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selectedApp == DeliveryApp.SPARK, { selectedApp = DeliveryApp.SPARK }, label = { Text("Spark") })
                    FilterChip(selectedApp == DeliveryApp.DOORDASH, { selectedApp = DeliveryApp.DOORDASH }, label = { Text("DoorDash") })
                }
                Button(onClick = {
                    if (tracking) {
                        locationTracker.stop()
                        miles = "%.2f".format(liveMiles)
                        tracking = false
                    } else {
                        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        if (fine || coarse) {
                            startedAt = System.currentTimeMillis()
                            liveMiles = 0.0
                            locationTracker.start()
                            tracking = true
                        } else {
                            locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                        }
                    }
                }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (tracking) "End Drive" else "Start Drive")
                }
                if (tracking) Text("GPS miles: ${"%.2f".format(liveMiles)}")
                if (!tracking) {
                    Text("Earnings", style = MaterialTheme.typography.titleMedium)
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
            } else {
                when (activeTab) {
                    AppTab.MAP -> MapScreen(Modifier.padding(padding).fillMaxSize())
                    AppTab.HISTORY -> HistoryTab(savedDrives, Modifier.padding(padding))
                    AppTab.EXPENSES -> ExpensesTab(Modifier.padding(padding))
                    AppTab.TRACK -> Unit
                }
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
private fun HistoryTab(drives: List<DriveRecord>, modifier: Modifier = Modifier) {
    Column(modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Drive history", style = MaterialTheme.typography.headlineSmall)
        if (drives.isEmpty()) Text("Your saved drives will appear here.")
        drives.forEach { drive ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(drive.app)
                    Text("${"%.2f".format(drive.miles)} mi  $${"%.2f".format(drive.totalPay())}")
                }
            }
        }
    }
}

@Composable
private fun ExpensesTab(modifier: Modifier = Modifier) {
    Column(modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Expenses", style = MaterialTheme.typography.headlineSmall)
        Text("Track your phone bill and other delivery-business expenses here.")
        Button(onClick = { /* Expense form next */ }, modifier = Modifier.fillMaxWidth()) {
            Text("Add expense")
        }
    }
}

@Composable
private fun MoneyField(label: String, onValueChange: (String) -> Unit) {
    OutlinedTextField("", onValueChange, label = { Text("$label ($)") }, modifier = Modifier.fillMaxWidth())
}
