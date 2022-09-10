package com.tutor.noviolence.models.response

import com.tutor.noviolence.models.UserModel

class LoginResponseModel {
    private var error:Boolean = false
    private var message : String = ""
    private var user : UserModel? = null

    constructor(error: Boolean, message: String, user: UserModel?) {
        this.error = error
        this.message = message
        this.user = user
    }
    public fun isError() : Boolean{
        return error
    }
    public fun getMessage() : String{
        return message
    }
    public fun getUser(): UserModel?{
        return user
    }
}