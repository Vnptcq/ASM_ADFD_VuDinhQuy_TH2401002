package com.example.contactmanagement.model

/**
 * Data model representing a single phone contact stored in SQLite.
 *
 * @property id        Auto-generated primary key (-1 when not yet saved).
 * @property name      Display name of the contact.
 * @property phone     Phone number.
 * @property group     Category label (e.g. Family, Work, Friends).
 */
data class Contact(
    var id: Long = -1,
    var name: String = "",
    var phone: String = "",
    var group: String = ""
)
