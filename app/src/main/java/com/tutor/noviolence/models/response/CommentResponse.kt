package com.tutor.noviolence.models.response

import java.time.Instant
import java.util.*

class CommentResponse {
    var id = 0
    var user_id = 0
    var place_id = ""
    var content = ""
    var rating = 0f
    var pub_date : String

    constructor(
        id: Int,
        user_id: Int,
        place_id: String,
        content: String,
        rating: Float,
        date: String
    ) {
        this.id = id
        this.user_id = user_id
        this.place_id = place_id
        this.content = content
        this.rating = rating
        this.pub_date = date
    }
}