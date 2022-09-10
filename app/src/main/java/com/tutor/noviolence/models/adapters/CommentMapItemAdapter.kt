package com.tutor.noviolence.models.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tutor.noviolence.R
import com.tutor.noviolence.fragments.GoogleMapFragment
import com.tutor.noviolence.models.CommentModel
import kotlinx.android.synthetic.main.comment_map_item.view.*
import kotlin.collections.ArrayList

class CommentMapItemAdapter(val items: ArrayList<CommentModel>, val requestFragment: GoogleMapFragment) : RecyclerView.Adapter<CommentMapItemAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(requestFragment.requireContext()).inflate(
                R.layout.comment_map_item, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items.get(position)

        val date = item.date
        holder.ratingBar.rating = item.rating
        holder.tvUsername.text = item.userId.toString()
        holder.tvComment.text = item.content
        holder.tvDate.text = date.toString()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val llCommentItem : LinearLayout = view.llCommentMapItem
        val ratingBar : RatingBar = view.ratingDanger
        val tvComment : TextView = view.tvComment
        val tvDate : TextView = view.tvDate
        val tvUsername : TextView = view.tvUsername
    }

}
