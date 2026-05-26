package com.avishena.vehiclereminder.ui.register

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.avishena.vehiclereminder.data.db.VehicleDatabase
import com.avishena.vehiclereminder.data.model.Vehicle
import com.avishena.vehiclereminder.data.repository.VehicleRepository
import com.avishena.vehiclereminder.databinding.ActivityRegisterVehicleBinding
import com.avishena.vehiclereminder.viewmodel.VehicleViewModel
import com.avishena.vehiclereminder.viewmodel.VehicleViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RegisterVehicleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterVehicleBinding
    private lateinit var viewModel: VehicleViewModel
    private var selectedDate: Long = System.currentTimeMillis()
    private val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale("id"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterVehicleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Daftarkan Kendaraan"

        val db = VehicleDatabase.getDatabase(this)
        val repo = VehicleRepository(db.vehicleDao())
        viewModel = ViewModelProvider(this, VehicleViewModelFactory(repo))[VehicleViewModel::class.java]

        binding.tvLastOilDate.text = dateFormatter.format(Calendar.getInstance().time)

        binding.btnPickDate.setOnClickListener { showDatePicker() }
        binding.btnSave.setOnClickListener { saveVehicle() }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                cal.set(year, month, day)
                selectedDate = cal.timeInMillis
                binding.tvLastOilDate.text = dateFormatter.format(cal.time)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveVehicle() {
        val name = binding.etVehicleName.text.toString().trim()
        if (name.isEmpty()) {
            binding.etVehicleName.error = "Nama kendaraan wajib diisi"
            return
        }

        val type = if (binding.rbMotor.isChecked) "Motor" else "Mobil"
        val odometer = binding.etCurrentOdometer.text.toString().toIntOrNull() ?: 0
        val lastOilOdometer = binding.etLastOilOdometer.text.toString().toIntOrNull() ?: 0

        val vehicle = Vehicle(
            name = name,
            type = type,
            currentOdometer = odometer,
            lastOilChangeDate = selectedDate,
            lastOilChangeOdometer = lastOilOdometer
        )

        viewModel.insertVehicle(vehicle)
        Toast.makeText(this, "Kendaraan berhasil didaftarkan", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
