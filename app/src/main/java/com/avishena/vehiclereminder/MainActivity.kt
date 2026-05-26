package com.avishena.vehiclereminder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.avishena.vehiclereminder.data.db.VehicleDatabase
import com.avishena.vehiclereminder.data.model.Vehicle
import com.avishena.vehiclereminder.data.repository.VehicleRepository
import com.avishena.vehiclereminder.databinding.ActivityMainBinding
import com.avishena.vehiclereminder.service.NotificationHelper
import com.avishena.vehiclereminder.ui.alarm.AlarmSettingActivity
import com.avishena.vehiclereminder.ui.dashboard.VehicleAdapter
import com.avishena.vehiclereminder.ui.detail.VehicleDetailActivity
import com.avishena.vehiclereminder.ui.register.RegisterVehicleActivity
import com.avishena.vehiclereminder.viewmodel.VehicleViewModel
import com.avishena.vehiclereminder.viewmodel.VehicleViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: VehicleViewModel
    private lateinit var adapter: VehicleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        NotificationHelper.createChannels(this)

        val db = VehicleDatabase.getDatabase(this)
        val repo = VehicleRepository(db.vehicleDao())
        viewModel = ViewModelProvider(this, VehicleViewModelFactory(repo))[VehicleViewModel::class.java]

        adapter = VehicleAdapter(
            onItemClick = { vehicle -> openVehicleDetail(vehicle) },
            onAlarmClick = { vehicle -> openAlarmSetting(vehicle) },
            onDeleteClick = { vehicle -> showDeleteConfirmation(vehicle) }
        )
        binding.rvVehicles.layoutManager = LinearLayoutManager(this)
        binding.rvVehicles.adapter = adapter

        viewModel.allVehicles.observe(this) { vehicles ->
            adapter.submitList(vehicles)
            binding.tvEmpty.visibility = if (vehicles.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.fabAddVehicle.setOnClickListener {
            startActivity(Intent(this, RegisterVehicleActivity::class.java))
        }

        requestNotificationPermission()
    }

    private fun openVehicleDetail(vehicle: Vehicle) {
        startActivity(
            Intent(this, VehicleDetailActivity::class.java).apply {
                putExtra(VehicleDetailActivity.EXTRA_VEHICLE_ID, vehicle.id)
            }
        )
    }

    private fun openAlarmSetting(vehicle: Vehicle) {
        startActivity(
            Intent(this, AlarmSettingActivity::class.java).apply {
                putExtra(AlarmSettingActivity.EXTRA_VEHICLE_ID, vehicle.id)
                putExtra(AlarmSettingActivity.EXTRA_VEHICLE_NAME, vehicle.name)
            }
        )
    }

    private fun showDeleteConfirmation(vehicle: Vehicle) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Kendaraan")
            .setMessage("Apakah Anda yakin ingin menghapus ${vehicle.name}?\nSemua riwayat odometer juga akan terhapus.")
            .setPositiveButton("Hapus") { _, _ -> viewModel.deleteVehicle(vehicle) }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }
    }
}
