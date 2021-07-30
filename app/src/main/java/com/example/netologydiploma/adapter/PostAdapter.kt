package com.example.netologydiploma.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.netologydiploma.R
import com.example.netologydiploma.databinding.PostListItemBinding
import com.example.netologydiploma.dto.Post

interface OnButtonInteractionListener {
    fun onLike(post: Post)
    fun onRemove(post: Post)
}

class PostAdapter(private val interactionListener: OnButtonInteractionListener) :
    ListAdapter<Post, PostViewHolder>(PostDiffCallback) {

    companion object PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean =
            oldItem.id == newItem.id


        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val postBinding =
            PostListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return PostViewHolder(postBinding, interactionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
        }

}


class PostViewHolder(
    private val postBinding: PostListItemBinding,
    private val interactionListener: OnButtonInteractionListener
) :
    RecyclerView.ViewHolder(postBinding.root) {
    fun bind(post: Post) {
        with(postBinding) {
            tVUserName.text = post.author
            tVPublished.text = post.published.toString()
            tvContent.text = post.content

            btLike.isChecked = post.isLiked
            btLike.text = post.likeCount.toString()

            btLike.setOnClickListener {
                interactionListener.onLike(post)
            }


            if (!post.ownedByMe) {
                btPostOptions.visibility = View.GONE
            } else {
                btPostOptions.visibility = View.VISIBLE
                btPostOptions.setOnClickListener {
                    PopupMenu(it.context, it).apply {
                        inflate(R.menu.post_list_item_menu)
                        menu.setGroupVisible(R.id.post_modification, post.ownedByMe)
                        setOnMenuItemClickListener { menuItem ->
                            when (menuItem.itemId) {
                                R.id.action_delete -> {
                                    interactionListener.onRemove(post)
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
}
