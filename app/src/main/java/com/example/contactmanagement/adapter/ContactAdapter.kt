package com.example.contactmanagement.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.contactmanagement.R
import com.example.contactmanagement.model.Contact

/**
 * RecyclerView adapter for the contact list.
 * Delegates Edit and Delete actions to [ContactActionListener].
 */
class ContactAdapter(
    private var contacts: List<Contact>,
    private val listener: ContactActionListener
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    /** Callbacks for row actions. */
    interface ContactActionListener {
        fun onEdit(contact: Contact)
        fun onDelete(contact: Contact)
    }

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName: TextView = itemView.findViewById(R.id.textName)
        val textPhone: TextView = itemView.findViewById(R.id.textPhone)
        val textGroup: TextView = itemView.findViewById(R.id.textGroup)
        val buttonEdit: ImageButton = itemView.findViewById(R.id.buttonEdit)
        val buttonDelete: ImageButton = itemView.findViewById(R.id.buttonDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        val context = holder.itemView.context

        holder.textName.text = contact.name
        holder.textPhone.text = contact.phone

        if (contact.group.isNotBlank()) {
            holder.textGroup.visibility = View.VISIBLE
            holder.textGroup.text =
                context.getString(R.string.label_group, contact.group)
        } else {
            holder.textGroup.visibility = View.GONE
        }

        holder.buttonEdit.setOnClickListener { listener.onEdit(contact) }
        holder.buttonDelete.setOnClickListener { listener.onDelete(contact) }
    }

    override fun getItemCount(): Int = contacts.size

    /** Replace the displayed list and refresh the RecyclerView. */
    fun updateContacts(newContacts: List<Contact>) {
        contacts = newContacts
        notifyDataSetChanged()
    }
}
