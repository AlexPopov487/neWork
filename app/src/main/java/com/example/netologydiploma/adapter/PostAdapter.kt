package com.example.netologydiploma.adapter

import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.PopupMenu
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.netologydiploma.R
import com.example.netologydiploma.databinding.PostListItemBinding
import com.example.netologydiploma.dto.AttachmentType
import com.example.netologydiploma.dto.Post
import com.example.netologydiploma.util.AndroidUtils
import com.example.netologydiploma.util.loadCircleCrop
import com.example.netologydiploma.util.loadImage
import com.google.android.exoplayer2.MediaItem
import me.saket.bettermovementmethod.BetterLinkMovementMethod


interface OnPostButtonInteractionListener {
    fun onPostLike(post: Post)
    fun onPostRemove(post: Post)
    fun onPostEdit(post: Post)
    fun onAvatarClicked(post: Post)
    fun onLinkClicked(url: String)
}

class PostAdapter(
    private val interactionListener: OnPostButtonInteractionListener,
) :
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
    private val interactionListener: OnPostButtonInteractionListener,

    ) :
    RecyclerView.ViewHolder(postBinding.root) {

    private val parentView = postBinding.root
    val videoThumbnail = postBinding.videoThumbnail
    val videoContainer = postBinding.videoContainer
    val videoProgressBar = postBinding.videoProgressbar
    var videoPreview: MediaItem? = null
    val videoPlayIcon: ImageView = postBinding.iVVideoPlayIcon



    fun bind(post: Post) {
        parentView.tag = this

        with(postBinding) {

            tVUserName.text = post.author
            tVPublished.text = AndroidUtils.formatMillisToDateTimeString(post.published.toEpochMilli())
            tvContent.text = post.content
            BetterLinkMovementMethod.linkify(Linkify.WEB_URLS, tvContent)
                .setOnLinkClickListener { textView, url ->
                    interactionListener.onLinkClicked(url)
                    true
                }
            

            post.authorAvatar?.let {
                iVAvatar.loadCircleCrop(it)
            } ?: iVAvatar.setImageDrawable(
                AppCompatResources.getDrawable(
                    itemView.context,
                    R.drawable.ic_no_avatar_user
                )
            )

            iVAvatar.setOnClickListener {
                interactionListener.onAvatarClicked(post)
            }

            btLike.isChecked = post.likedByMe
            btLike.text = post.likeCount.toString()

            btLike.setOnClickListener {
                interactionListener.onPostLike(post)
            }


            if (post.attachment == null) {
                imageAttachment.visibility = View.GONE

                videoContainer.visibility = View.GONE

                videoPreview = null

            } else {
                when (post.attachment!!.type) {
                    AttachmentType.IMAGE -> {
                        videoPreview = null
                        videoContainer.visibility = View.GONE
                        imageAttachment.visibility = View.VISIBLE
                        imageAttachment.loadImage(post.attachment!!.url)
                    }
                    AttachmentType.VIDEO -> {
                        imageAttachment.visibility = View.GONE
                        videoContainer.visibility = View.VISIBLE
                        videoPreview = MediaItem.fromUri(post.attachment!!.url)
                        Glide.with(parentView).load(post.attachment!!.url).into(videoThumbnail)
                    }
                    AttachmentType.AUDIO -> {
                        imageAttachment.visibility = View.GONE
                        videoContainer.visibility = View.VISIBLE
                        videoPreview = MediaItem.fromUri(post.attachment!!.url)
                        videoThumbnail.setImageDrawable(
                            AppCompatResources.getDrawable(
                                itemView.context,
                                R.drawable.ic_audiotrack_24
                            )
                        )
                    }
                }
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

data class PostPayload(
    val liked: Boolean? = null
)
