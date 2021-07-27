package com.example.netologydiploma.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.netologydiploma.R
import com.example.netologydiploma.databinding.PostListItemBinding
import com.example.netologydiploma.db.PostEntity

class PostAdapter : ListAdapter<PostEntity, PostViewHolder>(PostDiffCallback) {

    companion object PostDiffCallback : DiffUtil.ItemCallback<PostEntity>() {
        override fun areItemsTheSame(oldItem: PostEntity, newItem: PostEntity): Boolean =
            oldItem.id == newItem.id


        override fun areContentsTheSame(oldItem: PostEntity, newItem: PostEntity): Boolean =
            oldItem == newItem

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val postBinding =
            PostListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return PostViewHolder(postBinding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
        }

}


class PostViewHolder(private val postBinding: PostListItemBinding) :
    RecyclerView.ViewHolder(postBinding.root) {
    fun bind(post: PostEntity) {
        with(postBinding){
            tVUserName.text = post.author
            tVPublished.text = post.published.toString()
            tvContent.text = post.content

            btLike.isChecked = post.isLiked
            btLike.text = post.likeCount.toString()

            btLike.setOnClickListener {
                // TODO set button interaction
            }

            btPostOptions.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.post_list_item_menu)
                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.action_delete -> {
                                Toast.makeText(it.context, "Clicked", Toast.LENGTH_SHORT).show()
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }
        }
    }
}
