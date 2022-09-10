package com.tutor.noviolence.models

import java.sql.Date

class UserModel {
    var id : Int = 0
    lateinit var username : String
    lateinit var email : String
    lateinit var password : String
    lateinit var comments : List<CommentModel>

    constructor(id: Int, email : String, username : String){
        this.id = id
        this.email = email
        this.username = username
    }
}