package com.example.contactlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ContactViewModel : ViewModel() {

    private val _contacts = MutableLiveData<List<Contact>>()
    val contacts: LiveData<List<Contact>> get() = _contacts

    private val _searchQuery = MutableLiveData<String>("")
    val searchQuery: LiveData<String> get() = _searchQuery

    init {
        initSampleContacts()
    }

    private fun initSampleContacts() {
        _contacts.value = listOf(
            Contact(
                id = "1",
                nom = "Dupont",
                prenom = "Jean",
                telephone = "+33 6 12 34 56 78",
                email = "jean.dupont@email.com",
                photoResId = R.drawable.ic_avatar_1,
                latitude = 48.8566,
                longitude = 2.3522
            ),
            Contact(
                id = "2",
                nom = "Curie",
                prenom = "Marie",
                telephone = "+33 6 98 76 54 32",
                email = "marie.curie@science.fr",
                photoResId = R.drawable.ic_avatar_2,
                latitude = 48.8462,
                longitude = 2.3444
            ),
            Contact(
                id = "3",
                nom = "Pesquet",
                prenom = "Thomas",
                telephone = "+33 6 11 22 33 44",
                email = "thomas.pesquet@esa.int",
                photoResId = R.drawable.ic_avatar_3,
                latitude = 43.6047,
                longitude = 1.4442
            ),
            Contact(
                id = "4",
                nom = "Martin",
                prenom = "Sophie",
                telephone = "+33 7 44 55 66 77",
                email = "sophie.martin@tech.org",
                photoResId = R.drawable.ic_avatar_4,
                latitude = 45.7640,
                longitude = 4.8357
            ),
            Contact(
                id = "5",
                nom = "Bernard",
                prenom = "Lucas",
                telephone = "+33 6 55 44 33 22",
                email = "lucas.bernard@work.com",
                photoResId = R.drawable.ic_avatar_5,
                latitude = null,
                longitude = null
            )
        )
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun getFilteredContacts(): List<Contact> {
        val query = _searchQuery.value?.trim()?.lowercase().orEmpty()
        val currentList = _contacts.value.orEmpty()
        if (query.isEmpty()) return currentList

        return currentList.filter { contact ->
            contact.nom.lowercase().contains(query) ||
                    contact.prenom.lowercase().contains(query) ||
                    contact.fullName.lowercase().contains(query) ||
                    contact.telephone.lowercase().contains(query)
        }
    }

    fun getContactById(id: String): Contact? {
        return _contacts.value?.find { it.id == id }
    }

    fun updateContact(updatedContact: Contact) {
        val currentList = _contacts.value?.toMutableList() ?: return
        val index = currentList.indexOfFirst { it.id == updatedContact.id }
        if (index != -1) {
            currentList[index] = updatedContact
            _contacts.value = currentList
        }
    }
}
