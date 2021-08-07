package com.example.netologydiploma.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.netologydiploma.R
import com.example.netologydiploma.databinding.EventListItemBinding
import com.example.netologydiploma.dto.Event
import com.example.netologydiploma.dto.EventType
import com.example.netologydiploma.util.AndroidUtils

interface OnEventButtonInteractionListener {
    fun onEventLike(event: Event)
    fun onEventEdit(event: Event)
    fun onEventRemove(event: Event)
    fun onEventParticipate(event: Event)
}

class EventAdapter(private val interactionListener: OnEventButtonInteractionListener) :
    ListAdapter<Event, EventViewHolder>(EventDiffCallback) {

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
        holder.bind(getItem(position))
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
            tVPublished.text = AndroidUtils.formatMillisToDateString(event.published)
            tvContent.text = event.content
            tvEventDueDate.text = AndroidUtils.formatMillisToDateString(event.datetime)

            btParticipate.isChecked = event.participatedByMe
            btParticipate.text = event.participantsCount.toString()
            btParticipate.setOnClickListener {
                interactionListener.onEventParticipate(event)
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
                EventType.OFFLINE -> "Offline event"
                EventType.ONLINE -> "Online event"
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