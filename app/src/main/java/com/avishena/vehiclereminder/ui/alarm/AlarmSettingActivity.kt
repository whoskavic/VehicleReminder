package com.avishena.vehiclereminder.ui.alarm

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.avishena.vehiclereminder.data.db.VehicleDatabase
import com.avishena.vehiclereminder.data.model.OilReminder
import com.avishena.vehiclereminder.data.model.WarmupAlarm
import com.avishena.vehiclereminder.data.repository.VehicleRepository
import com.avishena.vehiclereminder.databinding.ActivityAlarmSettingBinding
import com.avishena.vehiclereminder.service.AlarmScheduler
import com.avishena.vehiclereminder.viewmodel.VehicleViewModel
import com.avishena.vehiclereminder.viewmodel.VehicleViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class AlarmSettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmSettingBinding
    private lateinit var viewModel: VehicleViewModel
    private var vehicleId: Int = -1
    private var vehicleName: String = ""
    private var existingWarmupAlarm: WarmupAlarm? = null
    private var existingOilReminder: OilReminder? = null

    private var selectedHour = 6
    private var selectedMinute = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vehicleId = intent.getIntExtra(EXTRA_VEHICLE_ID, -1)
        vehicleName = intent.getStringExtra(EXTRA_VEHICLE_NAME) ?: ""

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Alarm — $vehicleName"

        val db = VehicleDatabase.getDatabase(this)
        val repo = VehicleRepository(db.vehicleDao())
        viewModel = ViewModelProvider(this, VehicleViewModelFactory(repo))[VehicleViewModel::class.java]

        updateTimeDisplay()
        loadExistingSettings()

        binding.btnPickTime.setOnClickListener { showTimePicker() }
        binding.btnSaveWarmup.setOnClickListener { saveWarmupAlarm() }
        binding.btnCancelWarmup.setOnClickListener { cancelWarmupAlarm() }
        binding.btnSaveOil.setOnClickListener { saveOilReminder() }
        binding.btnCancelOil.setOnClickListener { cancelOilReminder() }
    }

    private fun showTimePicker() {
        TimePickerDialog(
            this,
            { _, hour, minute ->
                selectedHour = hour
                selectedMinute = minute
                updateTimeDisplay()
            },
            selectedHour,
            selectedMinute,
            true
        ).show()
    }

    private fun updateTimeDisplay() {
        binding.tvSelectedTime.text = String.format("%02d:%02d", selectedHour, selectedMinute)
    }

    private fun loadExistingSettings() {
        lifecycleScope.launch {
            existingWarmupAlarm = viewModel.getWarmupAlarm(vehicleId)
            existingOilReminder = viewModel.getOilReminder(vehicleId)

            withContext(Dispatchers.Main) {
                existingWarmupAlarm?.let { alarm ->
                    selectedHour = alarm.hour
                    selectedMinute = alarm.minute
                    updateTimeDisplay()
                    val days = alarm.days.split(",").mapNotNull { it.trim().toIntOrNull() }
                    binding.cbMonday.isChecked = Calendar.MONDAY in days
                    binding.cbTuesday.isChecked = Calendar.TUESDAY in days
                    binding.cbWednesday.isChecked = Calendar.WEDNESDAY in days
                    binding.cbThursday.isChecked = Calendar.THURSDAY in days
                    binding.cbFriday.isChecked = Calendar.FRIDAY in days
                    binding.cbSaturday.isChecked = Calendar.SATURDAY in days
                    binding.cbSunday.isChecked = Calendar.SUNDAY in days
                }
                existingOilReminder?.let { reminder ->
                    binding.etOilIntervalMonths.setText(reminder.intervalMonths.toString())
                }
            }
        }
    }

    private fun saveWarmupAlarm() {
        val days = buildList {
            if (binding.cbSunday.isChecked) add(Calendar.SUNDAY)
            if (binding.cbMonday.isChecked) add(Calendar.MONDAY)
            if (binding.cbTuesday.isChecked) add(Calendar.TUESDAY)
            if (binding.cbWednesday.isChecked) add(Calendar.WEDNESDAY)
            if (binding.cbThursday.isChecked) add(Calendar.THURSDAY)
            if (binding.cbFriday.isChecked) add(Calendar.FRIDAY)
            if (binding.cbSaturday.isChecked) add(Calendar.SATURDAY)
        }
        if (days.isEmpty()) {
            Toast.makeText(this, "Pilih minimal satu hari", Toast.LENGTH_SHORT).show()
            return
        }

        val alarm = WarmupAlarm(
            id = existingWarmupAlarm?.id ?: 0,
            vehicleId = vehicleId,
            hour = selectedHour,
            minute = selectedMinute,
            days = days.joinToString(","),
            isActive = true
        )
        viewModel.saveWarmupAlarm(alarm)
        AlarmScheduler.scheduleWarmupAlarm(this, alarm, vehicleName)
        existingWarmupAlarm = alarm
        Toast.makeText(this, "Alarm ${String.format("%02d:%02d", selectedHour, selectedMinute)} diaktifkan", Toast.LENGTH_SHORT).show()
    }

    private fun cancelWarmupAlarm() {
        val alarm = existingWarmupAlarm ?: run {
            Toast.makeText(this, "Belum ada alarm yang diatur", Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.deleteWarmupAlarm(alarm)
        AlarmScheduler.cancelWarmupAlarm(this, vehicleId)
        existingWarmupAlarm = null
        Toast.makeText(this, "Alarm panasin dimatikan", Toast.LENGTH_SHORT).show()
    }

    private fun saveOilReminder() {
        val intervalMonths = binding.etOilIntervalMonths.text.toString().toIntOrNull() ?: 0
        if (intervalMonths <= 0) {
            binding.etOilIntervalMonths.error = "Masukkan interval bulan yang valid"
            return
        }
        val nextTrigger = Calendar.getInstance().apply {
            add(Calendar.MONTH, intervalMonths)
        }.timeInMillis
        val reminder = OilReminder(
            id = existingOilReminder?.id ?: 0,
            vehicleId = vehicleId,
            intervalMonths = intervalMonths,
            nextTriggerTime = nextTrigger,
            isActive = true
        )
        viewModel.saveOilReminder(reminder)
        AlarmScheduler.scheduleOilReminder(this, reminder, vehicleName)
        existingOilReminder = reminder
        Toast.makeText(this, "Reminder ganti oli setiap $intervalMonths bulan diaktifkan", Toast.LENGTH_SHORT).show()
    }

    private fun cancelOilReminder() {
        val reminder = existingOilReminder ?: run {
            Toast.makeText(this, "Belum ada reminder yang diatur", Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.deleteOilReminder(reminder)
        AlarmScheduler.cancelOilReminder(this, vehicleId)
        existingOilReminder = null
        Toast.makeText(this, "Reminder ganti oli dimatikan", Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val EXTRA_VEHICLE_ID = "extra_vehicle_id"
        const val EXTRA_VEHICLE_NAME = "extra_vehicle_name"
    }
}
