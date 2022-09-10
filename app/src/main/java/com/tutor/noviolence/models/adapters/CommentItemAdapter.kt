package com.tutor.noviolence.models.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tutor.noviolence.R
import com.tutor.noviolence.fragments.LoginFragment
import com.tutor.noviolence.models.CommentModel
import kotlinx.android.synthetic.main.comment_item.view.*
import java.util.*

class CommentItemAdapter(val context: Context, val items: ArrayList<CommentModel>, val requestFragment: LoginFragment) : RecyclerView.Adapter<CommentItemAdapter.ViewHolder>() {

    private val TAG = "CommentItemAdapter"
    private val DATE_FORMAT = "dd.MM.yyyy HH:mm"

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.comment_item, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items.get(position)

        val rating = item.rating
        val content = item.content
        val date = item.date
        val place = item.place
        val userId = item.userId
        val id= item.id

        holder.ratingBar.rating = rating
        holder.tvComment.text = content
        holder.tvDate.text = Date.from(date).toString()
        holder.tvPlace.text = place!!.name

        holder.llCommentItem.setOnClickListener {
            requestFragment.showCommentDetails(CommentModel(id,userId, content, place, rating, date))
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val llCommentItem : LinearLayout = view.llCommentItem
        val ratingBar : RatingBar = view.ratingDanger
        val tvComment : TextView = view.tvComment
        val tvPlace : TextView = view.tvPlaceName
        val tvDate : TextView = view.tvDate
    }
}