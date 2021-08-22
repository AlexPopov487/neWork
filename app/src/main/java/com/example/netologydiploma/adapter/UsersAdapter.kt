package com.example.netologydiploma.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.netologydiploma.R
import com.example.netologydiploma.databinding.UserListItemBinding
import com.example.netologydiploma.dto.User
import com.example.netologydiploma.util.loadCircleCrop

interface OnUserInteractionListener {
    fun onUserClicked(user: User)
}

class UsersAdapter(private val onUserInteractionListener: OnUserInteractionListener) :
    ListAdapter<User, UsersViewHolder>(UserDiffItemCallback) {
    object UserDiffItemCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val binding =
            UserListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UsersViewHolder(binding, onUserInteractionListener)
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}

class UsersViewHolder(
    private val binding: UserListItemBinding,
    private val onUserInteractionListener: OnUserInteractionListener
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(user: User) {
        with(binding) {
            tVUserName.text = user.name
            tVUserLogin.text = user.login

            user.avatar?.let {
                iVAvatar.loadCircleCrop(it)
            } ?: iVAvatar.setImageDrawable(
                ContextCompat.getDrawable(
                    itemView.context,
                    R.drawable.ic_no_avatar_user
                )
            )

            cVIsYou.visibility = if (user.isItMe) View.VISIBLE
            else View.GONE
        }

        binding.userItemContainer.setOnClickListener {
            onUserInteractionListener.onUserClicked(user)
        }
    }

}
