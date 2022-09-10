package com.tutor.noviolence.models.response


class CommentsResponseModel {
    var error = false
    var message = ""
    val comments : ArrayList<CommentResponse>

    constructor(
        error: Boolean,
        message: String,
        comments: ArrayList<CommentResponse>,

        ) {
        this.error = error
        this.message = message
        this.comments = comments
    }
}