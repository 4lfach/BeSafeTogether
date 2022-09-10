package com.tutor.noviolence.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.tutor.noviolence.R
import com.tutor.noviolence.models.adapters.ContactItemAdapter
import com.tutor.noviolence.models.ContactModel
import com.tutor.noviolence.db.DatabaseHandler
import com.tutor.noviolence.models.MessageModel
import kotlinx.android.synthetic.main.dialog_update.*
import kotlinx.android.synthetic.main.fragment_contacts.*

class ContactsFragment : Fragment(R.layout.fragment_contacts) {
    private val TAG = "Contacts Fragment"
    private lateinit var dbHandler: DatabaseHandler

    //TODO add editable police number and logic to it
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createDbHandler()

        btnAddContact.setOnClickListener{ view ->
            addContact()
        }

        btnSaveDefaultMessage.setOnClickListener{view->
            saveMessage(view)
        }
        displayListOfContacts()
        displayDefaultMessage()
    }

    private fun saveMessage(view: View?) {
        val msgM = MessageModel()
        val id = dbHandler.getMessageById().id
        msgM.id = id
        msgM.message = etDefaultMessage.text.toString()

        if (msgM.message.isNotEmpty()){
            val status = dbHandler.updateMessage(msgM)

            if (status > -1){
                Toast.makeText(requireContext(), "Message is saved", Toast.LENGTH_SHORT).show()

            } else{
                Log.w(TAG, "Message was not saved. There are problems with database.")
            }
        } else{
            Toast.makeText(requireContext(), "Field is empty", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createDbHandler(){
        dbHandler = DatabaseHandler(requireContext())
    }
    private fun addContact() {
        val name = etName.text.toString()
        val phone = etPhone.text.toString()
        val gpsIsOn : Boolean = sIsGpsOn.isChecked
        addContactToDatabase(name, phone, gpsIsOn)
    }

    private fun displayDefaultMessage() {
        val msg = dbHandler.getMessageById(1)

        etDefaultMessage.setText(msg.message)
    }

    private fun displayListOfContacts() {
        if (getItemsList().size > 0){
            rvContact.visibility = View.VISIBLE
            tvNoRecords.visibility = View.GONE

            //sets the layout manager recyclerView will use
            rvContact.layoutManager = LinearLayoutManager(context)
            //rv adapter is initialized and items are passed into it
            val itemAdapter = ContactItemAdapter(requireContext(), getItemsList(), this)
            //rv adapter is set
            rvContact.adapter = itemAdapter
        } else{
            rvContact.visibility = View.GONE
            tvNoRecords.visibility = View.VISIBLE
        }
    }

    private fun getItemsList(): ArrayList<ContactModel> {
        //Gets all contacts db has
        val cntList : ArrayList<ContactModel> = dbHandler.getAllContacts()

        return cntList
    }

    fun updateContactDialog(cnt : ContactModel) {
        val updateDialog = Dialog(requireContext(), R.style.Theme_Dialog)

        updateDialog.setCancelable(false)

        updateDialog.setContentView(R.layout.dialog_update)

        updateDialog.etUpdateName.setText(cnt.name)
        updateDialog.etUpdatePhone.setText(cnt.phone)
        updateDialog.sUpdateIsGpsOn.isChecked = cnt.gpsIsOn

        updateDialog.btnUpdateContact.setOnClickListener{
            val name = updateDialog.etUpdateName.text.toString()
            val phone = updateDialog.etUpdatePhone.text.toString()
            val isGpsIsOn : Boolean = updateDialog.sUpdateIsGpsOn.isChecked

            if (name.isNotEmpty() && phone.isNotEmpty()){
                cnt.phone = phone
                cnt.name= name
                cnt.gpsIsOn = isGpsIsOn

                val status = dbHandler.updateContact(cnt)

                if (status > -1){
                    displayListOfContacts()
                    updateDialog.dismiss()

                    Toast.makeText(requireContext(), "Contact is saved", Toast.LENGTH_SHORT).show()

                } else{
                    Log.w(TAG, "Record was not saved. There are problems with database.")
                }
            } else{
                Toast.makeText(requireContext(), "Name or Phone fields are empty", Toast.LENGTH_SHORT).show()
            }
        }

        updateDialog.btnCancelContact.setOnClickListener{
            updateDialog.dismiss()
        }

        updateDialog.show()
    }

    fun deleteContactDialog(cnt: ContactModel){

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete contact?")

        builder.setMessage("Are you sure you want to delete contact ${cnt.name}?")
        builder.setIcon(R.drawable.ic_danger)

        builder.setPositiveButton("Yes"){dialogInterface, which ->
            val status = dbHandler.deleteContact(ContactModel(cnt.id))
            if (status >-1){
                Toast.makeText(requireContext(), "Contact was successfully deleted", Toast.LENGTH_LONG).show()

                displayListOfContacts()
            }
            dialogInterface.dismiss()
        }

        builder.setNegativeButton("No"){dialogInterface, which ->
            dialogInterface.dismiss()
        }

        val alertDialog : AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun addContactToDatabase(name : String, phone: String, recieveGps : Boolean){
        if (name.isNotEmpty() && phone.isNotEmpty()){
            val cnt = ContactModel(0, name, phone, recieveGps)
            val status = dbHandler.addContact(cnt)

            if (status > -1){
                Toast.makeText(requireContext(), "Contact is saved", Toast.LENGTH_SHORT).show()

                etName.text.clear()
                etPhone.text.clear()
                sIsGpsOn.isChecked = false

                displayListOfContacts()
            } else{
                Log.w(TAG, "Record was not saved. There are problems with database.")
            }
        } else{
            Toast.makeText(requireContext(), "Name or Phone fields are empty", Toast.LENGTH_SHORT).show()
        }
    }
}