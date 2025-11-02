package com.example.event

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class EventAdapter(
    private var events: List<Event>,
    private val onEditClick: (Event) -> Unit,
    private val onDeleteClick: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvEventTitle)
        val tvDateTime: TextView = itemView.findViewById(R.id.tvEventDateTime)
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
        holder.tvTitle.text = event.title

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        holder.tvDateTime.text = timeFormat.format(Date(event.timeMillis))

        holder.btnEdit.setOnClickListener { onEditClick(event) }
        holder.btnDelete.setOnClickListener { onDeleteClick(event) }
    }

    override fun getItemCount(): Int = events.size

    fun updateList(newList: List<Event>) {
        events = newList
        notifyDataSetChanged()
    }
}
