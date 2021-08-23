package com.example.netologydiploma.adapter

import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.content.res.AppCompatResources
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.netologydiploma.R
import com.example.netologydiploma.databinding.EventListItemBinding
import com.example.netologydiploma.dto.Event
import com.example.netologydiploma.dto.EventType
import com.example.netologydiploma.util.AndroidUtils
import com.example.netologydiploma.util.loadCircleCrop
import com.example.netologydiploma.util.loadImage
import me.saket.bettermovementmethod.BetterLinkMovementMethod

interface OnEventButtonInteractionListener {
    fun onEventLike(event: Event)
    fun onEventEdit(event: Event)
    fun onEventRemove(event: Event)
    fun onEventParticipate(event: Event)
    fun onAvatarClicked(event: Event)
    fun onLinkClicked(url: String)
    fun onSeeParticipantsClicked(event: Event)
}

class EventAdapter(private val interactionListener: OnEventButtonInteractionListener) :
    PagingDataAdapter<Event, EventViewHolder>(EventDiffCallback) {

    companion object EventDiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean =
            oldItem.id == newItem.id


        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val eventBinding =
            EventListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return EventViewHolder(eventBinding, interactionListener)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val item = getItem(position) ?: return
        holder.bind(item)
    }

}


class EventViewHolder(
    private val eventBinding: EventListItemBinding,
    private val interactionListener: OnEventButtonInteractionListener
) :
    RecyclerView.ViewHolder(eventBinding.root) {


    fun bind(event: Event) {
        with(eventBinding) {
            tVUserName.text = event.author
            tVPublished.text =
                AndroidUtils.formatMillisToDateTimeString(event.published.toEpochMilli())
            tvContent.text = event.content
            BetterLinkMovementMethod.linkify(Linkify.WEB_URLS, tvContent)
                .setOnLinkClickListener { textView, url ->
                    interactionListener.onLinkClicked(url)
                    true
                }

            tvEventDueDate.text =
                AndroidUtils.formatMillisToDateTimeString(event.datetime.toEpochMilli())


            event.authorAvatar?.let {
                iVAvatar.loadCircleCrop(it)
            } ?: iVAvatar.setImageDrawable(
                AppCompatResources.getDrawable(
                    itemView.context,
                    R.drawable.ic_no_avatar_user
                )
            )

            btParticipate.isChecked = event.participatedByMe
            btParticipate.text = event.participantsCount.toString()
            btParticipate.setOnClickListener {
                interactionListener.onEventParticipate(event)
            }


            tVActionSeeParticipants.setOnClickListener {
                interactionListener.onSeeParticipantsClicked(event)
            }

            if (event.participantsIds.isEmpty()) tVActionSeeParticipants.visibility = View.GONE
            else tVActionSeeParticipants.visibility = View.VISIBLE

            event.attachment?.let {
                imageAttachment.loadImage(it.url)
            }
            if (event.attachment == null) {
                mediaContainer.visibility = View.GONE
            } else {
                mediaContainer.visibility = View.VISIBLE
            }

            btLike.isChecked = event.likedByMe
            btLike.text = event.likeCount.toString()
            btLike.setOnClickListener {
                interactionListener.onEventLike(event)
            }

            iVEventType.setBackgroundResource(
                when (event.type) {
                    EventType.OFFLINE -> R.drawable.ic_event_type_offline
                    EventType.ONLINE -> R.drawable.ic_event_type_online
                }
            )

            tvEventType.text = when (event.type) {
                EventType.OFFLINE -> itemView.context.getString(R.string.event_type_offline)
                EventType.ONLINE -> itemView.context.getString(R.string.event_type_online)
            }

            iVAvatar.setOnClickListener {
                interactionListener.onAvatarClicked(event)
            }



            if (!event.ownedByMe) {
                btEventOptions.visibility = View.GONE
            } else {
                btEventOptions.visibility = View.VISIBLE
                btEventOptions.setOnClickListener {
                    PopupMenu(it.context, it).apply {
                        inflate(R.menu.list_item_menu)
                        menu.setGroupVisible(R.id.list_item_modification, event.ownedByMe)
                        setOnMenuItemClickListener { menuItem ->
                            when (menuItem.itemId) {
                                R.id.action_delete -> {
                                    interactionListener.onEventRemove(event)
                                    true
                                }
                                R.id.action_edit -> {
                                    interactionListener.onEventEdit(event)
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