package com.gigmiles.app

import android.app.Application
import androidx.room.Room
import com.gigmiles.app.data.GigMilesDatabase
import org.maplibre.android.MapLibre

class GigMilesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MapLibre.getInstance(this)
    }

    val database: GigMilesDatabase by lazy {
        Room.databaseBuilder(this, GigMilesDatabase::class.java, "gigmiles.db").build()
    }
}
