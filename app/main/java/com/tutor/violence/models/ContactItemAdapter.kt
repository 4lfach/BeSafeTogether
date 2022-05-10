package com.tutor.noviolence.models

import android.content.Context
import android.provider.Contacts
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.tutor.noviolence.ContactsFragment
import com.tutor.noviolence.MainActivity
import com.tutor.noviolence.R
import kotlinx.android.synthetic.main.contact_item.view.*
import kotlinx.android.synthetic.main.dialog_update.view.*
import java.util.concurrent.RecursiveAction

class ContactItemAdapter(val context: Context, val items: ArrayList<ContactModel>, val requestFragment: ContactsFragment) :
    RecyclerView.Adapter<ContactItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.contact_item, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items.get(position)
        holder.tvName.text = item.name
        holder.tvPhone.text= item.phone
        holder.cbIsGpsOn.isChecked = item.gpsIsOn


        if (position % 2 == 0){
            holder.llContactItem.setBackgroundColor(ContextCompat.getColor(context, R.color.lightGray))
        } else{
            holder.llContactItem.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
        }

        holder.ivEdit.setOnClickListener { view ->
            requestFragment.updateContactDialog(ContactModel(item.id, item.name, item.phone, item.gpsIsOn))
        }
        holder.ivDelete.setOnClickListener{view ->
            requestFragment.deleteContactDialog(ContactModel(item.id))
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val llContactItem : LinearLayout = view.llContactItem
        val tvName : TextView = view.tvName
        val tvPhone: TextView = view.tvPhone
        val cbIsGpsOn : CheckBox = view.cbIsGpsOn
        val ivEdit : ImageView = view.ivEdit
        val ivDelete : ImageView = view.ivDelete
    }
}