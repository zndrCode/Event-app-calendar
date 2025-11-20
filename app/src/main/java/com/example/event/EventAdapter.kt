package com.example.event

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class EventAdapter(
    private var events: List<Event>,
    private val onEditClick: (Event) -> Unit,
    private val onDeleteClick: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEventTitle: TextView = itemView.findViewById(R.id.tvEventTitle)
        val tvEventLocation: TextView = itemView.findViewById(R.id.tvEventLocation)
        val tvAllDay: TextView = itemView.findViewById(R.id.tvAllDay)
        val timeContainer: LinearLayout = itemView.findViewById(R.id.timeContainer)
        val tvStartTime: TextView = itemView.findViewById(R.id.tvStartTime)
        val tvStartAmPm: TextView = itemView.findViewById(R.id.tvStartAmPm)
        val tvEndTime: TextView = itemView.findViewById(R.id.tvEndTime)
        val tvEndAmPm: TextView = itemView.findViewById(R.id.tvEndAmPm)
        val timeDivider: View = itemView.findViewById(R.id.timeDivider)
        val colorIndicator: View = itemView.findViewById(R.id.colorIndicator)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]

        holder.tvEventTitle.text = event.title
        holder.tvEventLocation.text = event.location
        holder.colorIndicator.setBackgroundColor(event.color)

        if (event.isAllDay) {
            // Show all day layout
            holder.tvAllDay.visibility = View.VISIBLE
            holder.timeContainer.visibility = View.GONE
        } else {
            // Show time layout
            holder.tvAllDay.visibility = View.GONE
            holder.timeContainer.visibility = View.VISIBLE

            // Format start time
            val startTime = formatTime(event.timeMillis)
            holder.tvStartTime.text = startTime.first
            holder.tvStartAmPm.text = startTime.second

            // Format end time (if available)
            if (event.endTimeMillis > 0) {
                val endTime = formatTime(event.endTimeMillis)
                holder.tvEndTime.text = endTime.first
                holder.tvEndAmPm.text = endTime.second
                holder.tvEndTime.visibility = View.VISIBLE
                holder.tvEndAmPm.visibility = View.VISIBLE
                holder.timeDivider.visibility = View.VISIBLE
            } else {
                // If no end time, hide end time elements
                holder.tvEndTime.visibility = View.GONE
                holder.tvEndAmPm.visibility = View.GONE
                holder.timeDivider.visibility = View.GONE
            }
        }

        holder.btnEdit.setOnClickListener { onEditClick(event) }
        holder.btnDelete.setOnClickListener { onDeleteClick(event) }
    }

    private fun formatTime(timeMillis: Long): Pair<String, String> {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timeMillis
        }
        val hour = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)
        val amPm = if (calendar.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"

        val timeString = if (minute == 0) {
            String.format("%d", if (hour == 0) 12 else hour)
        } else {
            String.format("%d:%02d", if (hour == 0) 12 else hour, minute)
        }

        return Pair(timeString, amPm)
    }

    override fun getItemCount(): Int = events.size

    fun updateList(newList: List<Event>) {
        events = newList
        notifyDataSetChanged()
    }
}