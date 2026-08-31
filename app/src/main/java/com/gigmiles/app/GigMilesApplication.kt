package com.gigmiles.app

import android.app.Application
import androidx.room.Room
import com.gigmiles.app.data.GigMilesDatabase

class GigMilesApplication : Application() {
    val database: GigMilesDatabase by lazy {
        Room.databaseBuilder(this, GigMilesDatabase::class.java, "gigmiles.db").build()
    }
}
