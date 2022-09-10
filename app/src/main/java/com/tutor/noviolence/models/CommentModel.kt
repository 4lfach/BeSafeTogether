package com.tutor.noviolence.models

import com.google.android.libraries.places.api.model.Place
import java.time.Instant
import java.util.*

class CommentModel(val id: Int, val userId: Int, val content: String, val place:Place?, val rating: Float, val date: Instant) {

}
