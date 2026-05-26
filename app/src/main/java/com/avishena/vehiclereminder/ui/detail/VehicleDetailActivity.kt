package com.avishena.vehiclereminder.ui.detail

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.avishena.vehiclereminder.data.db.VehicleDatabase
import com.avishena.vehiclereminder.data.model.Vehicle
import com.avishena.vehiclereminder.data.repository.VehicleRepository
import com.avishena.vehiclereminder.databinding.ActivityVehicleDetailBinding
import com.avishena.vehiclereminder.ui.alarm.AlarmSettingActivity
import com.avishena.vehiclereminder.viewmodel.VehicleViewModel
import com.avishena.vehiclereminder.viewmodel.VehicleViewModelFactory

class VehicleDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVehicleDetailBinding
    private lateinit var viewModel: VehicleViewModel
    private lateinit var historyAdapter: OdometerHistoryAdapter
    private var vehicleId: Int = -1
    private var currentVehicle: Vehicle? = null

    companion object {
        const val EXTRA_VEHICLE_ID = "extra_vehicle_id"
        const val OIL_CHANGE_THRESHOLD_KM = 2000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVehicleDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vehicleId = intent.getIntExtra(EXTRA_VEHICLE_ID, -1)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val db = VehicleDatabase.getDatabase(this)
        val repo = VehicleRepository(db.vehicleDao())
        viewModel = ViewModelProvider(this, VehicleViewModelFactory(repo))[VehicleViewModel::class.java]

        historyAdapter = OdometerHistoryAdapter()
        binding.rvOdometerHistory.layoutManager = LinearLayoutManager(this)
        binding.rvOdometerHistory.adapter = historyAdapter

        observeVehicle()
        observeHistory()

        binding.btnUpdateOdometer.setOnClickListener {
            currentVehicle?.let { showUpdateOdometerDialog(it) }
        }
        binding.btnMarkOilChanged.setOnClickListener {
            currentVehicle?.let { markOilChanged(it) }
        }
        binding.btnGoToAlarm.setOnClickListener {
            val vehicle = currentVehicle ?: return@setOnClickListener
            startActivity(
                Intent(this, AlarmSettingActivity::class.java).apply {
                    putExtra(AlarmSettingActivity.EXTRA_VEHICLE_ID, vehicle.id)
                    putExtra(AlarmSettingActivity.EXTRA_VEHICLE_NAME, vehicle.name)
                }
            )
        }
    }

    private fun observeVehicle() {
        viewModel.getVehicleLive(vehicleId).observe(this) { vehicle ->
            vehicle ?: return@observe
            currentVehicle = vehicle
            supportActionBar?.title = vehicle.name
            binding.tvDetailName.text = vehicle.name
            binding.tvDetailType.text = vehicle.type
            binding.tvCurrentOdo.text = "%,d km".format(vehicle.currentOdometer)
            binding.tvLastOilOdo.text = "%,d km".format(vehicle.lastOilChangeOdometer)

            val diff = vehicle.currentOdometer - vehicle.lastOilChangeOdometer
            binding.tvOdoDiff.text = "%,d km".format(diff)

            if (diff >= OIL_CHANGE_THRESHOLD_KM) {
                binding.tvOdoDiff.setTextColor(getColor(com.avishena.vehiclereminder.R.color.warningText))
                binding.cardOilWarning.visibility = View.VISIBLE
            } else {
                binding.tvOdoDiff.setTextColor(getColor(com.avishena.vehiclereminder.R.color.colorSuccess))
                binding.cardOilWarning.visibility = View.GONE
            }
        }
    }

    private fun observeHistory() {
        viewModel.getOdometerHistory(vehicleId).observe(this) { list ->
            historyAdapter.submitList(list)
            binding.tvNoHistory.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            binding.rvOdometerHistory.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun showUpdateOdometerDialog(vehicle: Vehicle) {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            hint = "Contoh: ${vehicle.currentOdometer + 500}"
            setPadding(48, 24, 48, 24)
        }
        AlertDialog.Builder(this)
            .setTitle("Update Odometer")
            .setMessage("Odometer saat ini: ${"%,d".format(vehicle.currentOdometer)} km\nMasukkan angka odometer terbaru:")
            .setView(input)
            .setPositiveButton("Simpan") { _, _ ->
                val newOdo = input.text.toString().toIntOrNull()
                when {
                    newOdo == null -> Toast.makeText(this, "Masukkan angka yang valid", Toast.LENGTH_SHORT).show()
                    newOdo <= vehicle.currentOdometer -> Toast.makeText(this, "Odometer baru harus lebih besar dari ${"%,d".format(vehicle.currentOdometer)} km", Toast.LENGTH_SHORT).show()
                    else -> viewModel.addOdometerRecord(vehicleId, newOdo)
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun markOilChanged(vehicle: Vehicle) {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Ganti Oli")
            .setMessage("Tandai oli sudah diganti pada odometer ${"%,d".format(vehicle.currentOdometer)} km?")
            .setPositiveButton("Ya, Sudah Diganti") { _, _ ->
                viewModel.markOilChanged(vehicle)
                Toast.makeText(this, "Odometer ganti oli direset ke ${"%,d".format(vehicle.currentOdometer)} km", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
