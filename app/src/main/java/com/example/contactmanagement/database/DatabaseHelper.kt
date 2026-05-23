package com.example.contactmanagement.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.contactmanagement.model.Contact

/**
 * SQLite helper for the contacts table.
 * Provides full CRUD: insert, read all, update, delete, and search by name.
 */
class DatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        private const val DATABASE_NAME = "contacts_db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_CONTACTS = "contacts"
        private const val COL_ID = "id"
        private const val COL_NAME = "name"
        private const val COL_PHONE = "phone"
        private const val COL_GROUP = "contact_group"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_CONTACTS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NAME TEXT NOT NULL,
                $COL_PHONE TEXT NOT NULL,
                $COL_GROUP TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CONTACTS")
        onCreate(db)
    }

    /** Insert a new contact. Returns the new row id, or -1 on failure. */
    fun insertContact(contact: Contact): Long {
        val values = contactToValues(contact)
        val db = writableDatabase
        val id = db.insert(TABLE_CONTACTS, null, values)
        db.close()
        return id
    }

    /** Return all contacts ordered by name. */
    fun getAllContacts(): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_CONTACTS,
            null,
            null,
            null,
            null,
            null,
            "$COL_NAME COLLATE NOCASE ASC"
        )

        cursor.use {
            val idIndex = it.getColumnIndexOrThrow(COL_ID)
            val nameIndex = it.getColumnIndexOrThrow(COL_NAME)
            val phoneIndex = it.getColumnIndexOrThrow(COL_PHONE)
            val groupIndex = it.getColumnIndexOrThrow(COL_GROUP)

            while (it.moveToNext()) {
                contacts.add(
                    Contact(
                        id = it.getLong(idIndex),
                        name = it.getString(nameIndex),
                        phone = it.getString(phoneIndex),
                        group = it.getString(groupIndex) ?: ""
                    )
                )
            }
        }
        db.close()
        return contacts
    }

    /** Search contacts whose name contains [query] (case-insensitive). */
    fun searchContactsByName(query: String): List<Contact> {
        if (query.isBlank()) return getAllContacts()

        val contacts = mutableListOf<Contact>()
        val db = readableDatabase
        val selection = "$COL_NAME LIKE ?"
        val selectionArgs = arrayOf("%$query%")

        val cursor = db.query(
            TABLE_CONTACTS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "$COL_NAME COLLATE NOCASE ASC"
        )

        cursor.use {
            val idIndex = it.getColumnIndexOrThrow(COL_ID)
            val nameIndex = it.getColumnIndexOrThrow(COL_NAME)
            val phoneIndex = it.getColumnIndexOrThrow(COL_PHONE)
            val groupIndex = it.getColumnIndexOrThrow(COL_GROUP)

            while (it.moveToNext()) {
                contacts.add(
                    Contact(
                        id = it.getLong(idIndex),
                        name = it.getString(nameIndex),
                        phone = it.getString(phoneIndex),
                        group = it.getString(groupIndex) ?: ""
                    )
                )
            }
        }
        db.close()
        return contacts
    }

    /** Update an existing contact by id. Returns number of rows affected. */
    fun updateContact(contact: Contact): Int {
        val values = contactToValues(contact)
        val db = writableDatabase
        val rows = db.update(
            TABLE_CONTACTS,
            values,
            "$COL_ID = ?",
            arrayOf(contact.id.toString())
        )
        db.close()
        return rows
    }

    /** Delete a contact by id. Returns number of rows deleted. */
    fun deleteContact(id: Long): Int {
        val db = writableDatabase
        val rows = db.delete(TABLE_CONTACTS, "$COL_ID = ?", arrayOf(id.toString()))
        db.close()
        return rows
    }

    private fun contactToValues(contact: Contact): ContentValues {
        return ContentValues().apply {
            put(COL_NAME, contact.name.trim())
            put(COL_PHONE, contact.phone.trim())
            put(COL_GROUP, contact.group.trim())
        }
    }
}
