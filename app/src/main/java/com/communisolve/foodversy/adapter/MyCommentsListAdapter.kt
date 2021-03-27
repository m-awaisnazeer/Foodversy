package com.communisolve.foodversy.adapter

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.communisolve.foodversy.databinding.LayoutCommentItemBinding
import com.communisolve.foodversy.model.CommentModel

class MyCommentsListAdapter(
    internal var context: Context,
    internal var commentsList: List<CommentModel>
) : RecyclerView.Adapter<MyCommentsListAdapter.ViewHolder>() {
    lateinit var binding: LayoutCommentItemBinding

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding =
            LayoutCommentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var commentModel:CommentModel = commentsList.get(position)

        val timestamp = commentModel!!.commentTimeStamp!!["timeStamp"]!!.toString().toLong()
        binding.txtCommentDate.text = DateUtils.getRelativeTimeSpanString(timestamp)
        commentModel.apply {
            binding.txtComment.setText(this.comment)
            binding.txtCommentName.setText(this.name)
//            binding.txtCommentDate.setText(commentTimeStamp?.get("timestamp")!!.toString())
            binding.ratingBar.rating = ratingValue

        }
    }

    override fun getItemCount(): Int {
        return commentsList.size
    }
}