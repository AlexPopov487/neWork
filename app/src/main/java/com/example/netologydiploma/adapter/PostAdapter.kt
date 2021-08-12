package com.example.netologydiploma.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.netologydiploma.R
import com.example.netologydiploma.api.ApiServiceModule
import com.example.netologydiploma.databinding.PostListItemBinding
import com.example.netologydiploma.dto.Post
import com.example.netologydiploma.util.AndroidUtils
import com.example.netologydiploma.util.loadAvatar
import com.example.netologydiploma.util.loadCircleCrop
import com.example.netologydiploma.util.loadImage

const val BASE_AVATAR_URL = "${ApiServiceModule.BASE_URL}/media/"

interface OnPostButtonInteractionListener {
    fun onPostLike(post: Post)
    fun onPostRemove(post: Post)
    fun onPostEdit(post: Post)
    fun onAvatarClicked(post: Post)
}

class PostAdapter(private val interactionListener: OnPostButtonInteractionListener) :
    PagingDataAdapter<Post, PostViewHolder>(PostDiffCallback) {

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
        val item = getItem(position) ?: return
        holder.bind(item)
    }

}


class PostViewHolder(
    private val postBinding: PostListItemBinding,
    private val interactionListener: OnPostButtonInteractionListener
) :
    RecyclerView.ViewHolder(postBinding.root) {


    fun bind(post: Post) {
        with(postBinding) {
            tVUserName.text = post.author
            tVPublished.text = AndroidUtils.formatMillisToDateTimeString(post.published)
            tvContent.text = post.content

            iVAvatar.loadCircleCrop(post.authorAvatar)

            iVAvatar.setOnClickListener {
                interactionListener.onAvatarClicked(post)
            }

            btLike.isChecked = post.likedByMe
            btLike.text = post.likeCount.toString()


            btLike.setOnClickListener {
                interactionListener.onPostLike(post)
            }
            post.attachment?.let {
                imageAttachment.loadImage(it.url)
            }
            if (post.attachment == null) {
                mediaContainer.visibility = View.GONE
            } else {
                mediaContainer.visibility = View.VISIBLE
            }


            if (!post.ownedByMe) {
                btPostOptions.visibility = View.GONE
            } else {
                btPostOptions.visibility = View.VISIBLE
                btPostOptions.setOnClickListener {
                    PopupMenu(it.context, it).apply {
                        inflate(R.menu.list_item_menu)
                        menu.setGroupVisible(R.id.list_item_modification, post.ownedByMe)
                        setOnMenuItemClickListener { menuItem ->
                            when (menuItem.itemId) {
                                R.id.action_delete -> {
                                    interactionListener.onPostRemove(post)
                                    true
                                }
                                R.id.action_edit -> {
                                    interactionListener.onPostEdit(post)
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
