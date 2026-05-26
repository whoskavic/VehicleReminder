package com.avishena.vehiclereminder.ui.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.avishena.vehiclereminder.data.model.OdometerHistory
import com.avishena.vehiclereminder.databinding.ItemOdometerHistoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OdometerHistoryAdapter : RecyclerView.Adapter<OdometerHistoryAdapter.HistoryViewHolder>() {

    private var records: List<OdometerHistory> = emptyList()
    private val dateFormat = SimpleDateFormat("dd MMM yyyy  HH:mm", Locale("id"))

    fun submitList(newList: List<OdometerHistory>) {
        records = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemOdometerHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val record = records[position]
        // Delta: selisih dengan entri sebelumnya (lebih lama = index lebih tinggi karena sorted DESC)
        val delta = if (position < records.size - 1) {
            record.odometer - records[position + 1].odometer
        } else null
        holder.bind(record, delta)
    }

    override fun getItemCount() = records.size

    inner class HistoryViewHolder(private val binding: ItemOdometerHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(record: OdometerHistory, delta: Int?) {
            binding.tvHistoryDate.text = dateFormat.format(Date(record.recordedAt))
            binding.tvHistoryOdometer.text = "%,d km".format(record.odometer)
            if (delta != null) {
                binding.tvHistoryDelta.text = "+%,d km".format(delta)
                binding.tvHistoryDelta.visibility = View.VISIBLE
            } else {
                binding.tvHistoryDelta.visibility = View.INVISIBLE
            }
        }
    }
}
