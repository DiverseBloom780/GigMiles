package com.gigmiles.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gigmiles.app.data.DriveRecord
import com.gigmiles.app.data.ExpenseRecord
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GigMilesViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = (application as GigMilesApplication).database.gigMilesDao()
    val drives = dao.observeDrives().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val expenses = dao.observeExpenses().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun saveDrive(record: DriveRecord) = viewModelScope.launch { dao.insertDrive(record) }
    fun saveExpense(record: ExpenseRecord) = viewModelScope.launch { dao.insertExpense(record) }
}
