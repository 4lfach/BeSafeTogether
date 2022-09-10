package com.tutor.noviolence.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.tutor.noviolence.models.ContactModel
import com.tutor.noviolence.models.MessageModel

class DatabaseHandler(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "NoViolence.db"
        private const val DATABASE_VERSION = 6
        private const val KEY_ID = "_id"

        private const val TABLE_CONTACTS = "ContactsTable"
        private const val KEY_NAME = "name"
        private const val KEY_PHONE = "phone"
        private const val KEY_GPS_ON = "gpsIsOn"

        //There should be an option to choose message for separate contacts
        //But as for now separate table will be here
        private const val TABLE_MESSAGES = "MessagesTable"
        private const val KEY_MESSAGE = "message"

        //Table for storing the current user
        private const val TABLE_USER = "UserTable"

        // Login Table Columns names
        private const val KEY_USER_ID = "id"
        private const val KEY_USER_NAME = "username"
        private const val KEY_USER_EMAIL = "email"
        private const val KEY_USER_UID = "uid"
        private const val KEY_USER_CREATED_AT = "created_at"

        private const val TAG = "Database handler"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_NAME + " TEXT," +
                KEY_PHONE + " TEXT," +
                KEY_GPS_ON + " INTEGER" + ")"

        val CREATE_MESSAGES_TABLE = "CREATE TABLE " + TABLE_MESSAGES + "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_MESSAGE + " TEXT" + ")"

        val INSERT_MESSAGE_TABLE_MESSAGE = "INSERT INTO $TABLE_MESSAGES VALUES ('1', '')"

        db?.execSQL(CREATE_CONTACTS_TABLE)
        db?.execSQL(CREATE_MESSAGES_TABLE)
        db?.execSQL(INSERT_MESSAGE_TABLE_MESSAGE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        val DROP_CONTACTS_TABLE = "DROP TABLE IF EXISTS $TABLE_CONTACTS"
        val DROP_MESSAGES_TABLE = "DROP TABLE IF EXISTS $TABLE_MESSAGES"
        val DROP_USER_TABLE = "DROP TABLE IF EXISTS $TABLE_USER"

        db?.execSQL(DROP_MESSAGES_TABLE)
        db?.execSQL(DROP_CONTACTS_TABLE)
        db?.execSQL(DROP_USER_TABLE)

        onCreate(db)
    }
    //CRUD for Contacts
    fun addContact(contact : ContactModel) : Long {
        val db = this.writableDatabase

        var gpsValue : Int = 0
        if (contact.gpsIsOn)
            gpsValue = 1
        val contentValues = ContentValues()
        contentValues.put(KEY_NAME, contact.name)
        contentValues.put(KEY_PHONE, contact.phone)
        contentValues.put(KEY_GPS_ON, gpsValue)

        val success = db.insert(TABLE_CONTACTS, null, contentValues)

        db.close()
        return success
    }

    fun getAllContacts() : ArrayList<ContactModel>{
        val cntList = ArrayList<ContactModel>()

        val selectQuery = "SELECT * FROM $TABLE_CONTACTS"
        val db = this.readableDatabase
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException){
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var id: Int
        var name: String
        var phone : String
        var gpsIsOn : Boolean = false

        if (cursor.moveToFirst()){
            do {
                id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID))
                name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME))
                phone = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PHONE))
                val gpsIsOnInt = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_GPS_ON))

                if (gpsIsOnInt == 1){
                    gpsIsOn = true
                }


                val contact = ContactModel(id = id, name = name, phone = phone, gpsIsOn = gpsIsOn)
                cntList.add(contact)
            }
            while (cursor.moveToNext())
        }

        return cntList
    }

    fun updateContact(cnt: ContactModel) : Int{
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(KEY_NAME, cnt.name)
        contentValues.put(KEY_PHONE, cnt.phone)
        contentValues.put(KEY_GPS_ON, cnt.gpsIsOn)

        val stringArray = arrayOf(cnt.id.toString())

        val success = db.update(TABLE_CONTACTS, contentValues, "$KEY_ID=?", stringArray)

        db.close()
        return success
    }

    fun deleteContact(cnt: ContactModel): Int{
        val db = writableDatabase
        val stringArray = arrayOf(cnt.id.toString())
        val success = db.delete(TABLE_CONTACTS, "$KEY_ID=?", stringArray)

        db.close()
        return success
    }

    //CRUD for messages. Only update will be implemented for now, because message is single row
    fun updateMessage(msg : MessageModel):Int{
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(KEY_MESSAGE, msg.message)

        val stringArray = arrayOf(msg.id.toString())

        val success = db.update(TABLE_MESSAGES, contentValues, "$KEY_ID=?", stringArray)

        db.close()
        return success
    }
    //This method executes only once to set a row for message that could be updated later
    fun addMessage() : Long {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(KEY_MESSAGE, "")

        val success = db.insert(TABLE_CONTACTS, null, contentValues)

        db.close()
        return success
    }

    fun getMessageById(id:Int = 1) : MessageModel {
        val msg = MessageModel()

        //KEY_ID for message is 1 for now, because there is only one
        val selectQuery = "SELECT * FROM $TABLE_MESSAGES WHERE $KEY_ID = $id"
        val db = this.readableDatabase
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException){
            db.execSQL(selectQuery)
            return msg
        }

        var id: Int
        var message: String

        if (cursor.moveToFirst()){
            do {
                id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID))
                message = cursor.getString(cursor.getColumnIndexOrThrow(KEY_MESSAGE))

                msg.message = message
                msg.id = id
            }
            while (cursor.moveToNext())
        }
        return msg
    }

    /**
     * Storing user details in database
     */
    fun addUser(username: String, email: String, uid: String, created_at: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_NAME, username) // Name
        values.put(KEY_USER_EMAIL, email) // Email
        values.put(KEY_USER_UID, uid) // Email

        // Inserting Row
        val id = db.insert(TABLE_USER, null, values)
        db.close() // Closing database connection
        Log.d(TAG, "New user inserted into sqlite: $id")
    }

    /**
     * Getting user data from database
     */
    fun getUserDetails(): HashMap<String, String>? {
        val user = HashMap<String, String>()
        val selectQuery = "SELECT  * FROM $TABLE_USER"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        // Move to first row
        cursor.moveToFirst()
        if (cursor.count > 0) {
            user["username"] = cursor.getString(1)
            user["email"] = cursor.getString(2)
            user["uid"] = cursor.getString(3)
        }
        cursor.close()
        db.close()
        // return user
        Log.d(TAG, "Fetching user from Sqlite: $user")
        return user
    }

    /**
     * Re crate database Delete all tables and create them again
     */
    fun deleteUsers() {
        val db = this.writableDatabase
        // Delete All Rows
        db.delete(TABLE_USER, null, null)
        db.close()
        Log.d(TAG, "Deleted all user info from sqlite")
    }
}