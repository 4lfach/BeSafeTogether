package com.tutor.noviolence.models.response

import com.google.gson.annotations.SerializedName

class DefaultResponseModel {
    @SerializedName("error")
    private var err : Boolean = false

    @SerializedName("message")
    private var msg : String = ""

    constructor(err: Boolean, msg: String) {
        this.err = err
        this.msg = msg
    }

    public fun isErr() : Boolean{
        return err
    }

    public fun getMsg() : String{
        return msg
    }
}