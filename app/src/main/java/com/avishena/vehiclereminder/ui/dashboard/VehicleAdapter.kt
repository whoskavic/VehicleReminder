package com.avishena.vehiclereminder.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.avishena.vehiclereminder.data.model.Vehicle
import com.avishena.vehiclereminder.databinding.ItemVehicleBinding

class VehicleAdapter(
    private val onItemClick: (Vehicle) -> Unit,
    private val onAlarmClick: (Vehicle) -> Unit,
    private val onDeleteClick: (Vehicle) -> Unit
) : RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder>() {

    private var vehicles: List<Vehicle> = emptyList()

    companion object {
        private const val OIL_WARNING_THRESHOLD = 2000
    }

    fun submitList(newList: List<Vehicle>) {
        vehicles = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val binding = ItemVehicleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VehicleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        holder.bind(vehicles[position])
    }

    override fun getItemCount() = vehicles.size

    inner class VehicleViewHolder(private val binding: ItemVehicleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(vehicle: Vehicle) {
            binding.tvVehicleName.text = vehicle.name
            binding.tvVehicleType.text = vehicle.type
            binding.tvOdometer.text = "Odometer: ${"%,d".format(vehicle.currentOdometer)} km"

            val diff = vehicle.currentOdometer - vehicle.lastOilChangeOdometer
            binding.tvOilWarning.visibility =
                if (diff >= OIL_WARNING_THRESHOLD) View.VISIBLE else View.GONE

            binding.root.setOnClickListener { onItemClick(vehicle) }
            binding.root.setOnLongClickListener { onDeleteClick(vehicle); true }
            binding.btnAlarmSetting.setOnClickListener { onAlarmClick(vehicle) }
        }
    }
}
