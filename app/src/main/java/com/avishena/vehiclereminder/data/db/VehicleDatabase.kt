package com.avishena.vehiclereminder.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.avishena.vehiclereminder.data.model.OdometerHistory
import com.avishena.vehiclereminder.data.model.OilReminder
import com.avishena.vehiclereminder.data.model.Vehicle
import com.avishena.vehiclereminder.data.model.WarmupAlarm

@Database(
    entities = [Vehicle::class, WarmupAlarm::class, OilReminder::class, OdometerHistory::class],
    version = 2,
    exportSchema = false
)
abstract class VehicleDatabase : RoomDatabase() {

    abstract fun vehicleDao(): VehicleDao

    companion object {
        @Volatile
        private var INSTANCE: VehicleDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `odometer_history` " +
                    "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`vehicleId` INTEGER NOT NULL, " +
                    "`odometer` INTEGER NOT NULL, " +
                    "`recordedAt` INTEGER NOT NULL)"
                )
            }
        }

        fun getDatabase(context: Context): VehicleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VehicleDatabase::class.java,
                    "vehicle_reminder_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
