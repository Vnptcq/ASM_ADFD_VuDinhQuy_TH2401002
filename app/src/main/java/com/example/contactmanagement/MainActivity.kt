package com.example.contactmanagement

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.contactmanagement.adapter.ContactAdapter
import com.example.contactmanagement.database.DatabaseHelper
import com.example.contactmanagement.model.Contact
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

/**
 * Main screen: lists contacts, supports search, add, edit, and delete.
 */
class MainActivity : AppCompatActivity(), ContactAdapter.ContactActionListener {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var adapter: ContactAdapter
    private lateinit var recyclerContacts: RecyclerView
    private lateinit var textEmpty: TextView
    private lateinit var editSearch: TextInputEditText

    private var isSearchActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        databaseHelper = DatabaseHelper(this)

        recyclerContacts = findViewById(R.id.recyclerContacts)
        textEmpty = findViewById(R.id.textEmpty)
        editSearch = findViewById(R.id.editSearch)
        val fabAdd: FloatingActionButton = findViewById(R.id.fabAdd)

        adapter = ContactAdapter(emptyList(), this)
        recyclerContacts.layoutManager = LinearLayoutManager(this)
        recyclerContacts.adapter = adapter

        fabAdd.setOnClickListener { showContactDialog(null) }
        setupSearch()

        loadContacts()
    }

    /** Reload all contacts from the database (no search filter). */
    private fun loadContacts() {
        isSearchActive = false
        val contacts = databaseHelper.getAllContacts()
        updateUi(contacts)
    }

    /** Filter list as the user types in the search field. */
    private fun setupSearch() {
        editSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                isSearchActive = query.isNotEmpty()
                val results = databaseHelper.searchContactsByName(query)
                updateUi(results)
            }
        })
    }

    private fun updateUi(contacts: List<Contact>) {
        adapter.updateContacts(contacts)

        val isEmpty = contacts.isEmpty()
        textEmpty.visibility = if (isEmpty) TextView.VISIBLE else TextView.GONE
        recyclerContacts.visibility = if (isEmpty) RecyclerView.GONE else RecyclerView.VISIBLE

        textEmpty.setText(
            if (isSearchActive) R.string.no_search_results else R.string.empty_list
        )
    }

    /**
     * Show add/edit dialog. Pass [contact] when editing; null for a new contact.
     */
    private fun showContactDialog(contact: Contact?) {
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_contact, null)

        val editName = dialogView.findViewById<TextInputEditText>(R.id.editName)
        val editPhone = dialogView.findViewById<TextInputEditText>(R.id.editPhone)
        val editGroup = dialogView.findViewById<TextInputEditText>(R.id.editGroup)

        val isEdit = contact != null
        if (isEdit) {
            editName.setText(contact!!.name)
            editPhone.setText(contact.phone)
            editGroup.setText(contact.group)
        }

        AlertDialog.Builder(this)
            .setTitle(if (isEdit) R.string.edit_contact else R.string.add_contact)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { dialog, _ ->
                val name = editName.text?.toString()?.trim().orEmpty()
                val phone = editPhone.text?.toString()?.trim().orEmpty()
                val group = editGroup.text?.toString()?.trim().orEmpty()

                if (name.isEmpty()) {
                    Toast.makeText(this, R.string.error_name_required, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (phone.isEmpty()) {
                    Toast.makeText(this, R.string.error_phone_required, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val saved = Contact(
                    id = contact?.id ?: -1,
                    name = name,
                    phone = phone,
                    group = group
                )

                if (isEdit) {
                    databaseHelper.updateContact(saved)
                } else {
                    databaseHelper.insertContact(saved)
                }

                Toast.makeText(this, R.string.contact_saved, Toast.LENGTH_SHORT).show()
                refreshAfterChange()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onEdit(contact: Contact) {
        showContactDialog(contact)
    }

    override fun onDelete(contact: Contact) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_confirm_title)
            .setMessage(getString(R.string.delete_confirm_message, contact.name))
            .setPositiveButton(R.string.delete) { dialog, _ ->
                databaseHelper.deleteContact(contact.id)
                Toast.makeText(this, R.string.contact_deleted, Toast.LENGTH_SHORT).show()
                refreshAfterChange()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    /** Refresh list while preserving an active search query. */
    private fun refreshAfterChange() {
        val query = editSearch.text?.toString()?.trim().orEmpty()
        if (query.isNotEmpty()) {
            updateUi(databaseHelper.searchContactsByName(query))
        } else {
            loadContacts()
        }
    }
}
