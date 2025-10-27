package com.satlock.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.satlock.domain.User
import com.satlock.usersyncapp.R

class UserAdapter(
    private val onFavoriteClick: (User) -> Unit
) : ListAdapter<User, UserAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view, onFavoriteClick)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class UserViewHolder(
        itemView: View,
        private val onFavoriteClick: (User) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val nameTextView: TextView = itemView.findViewById(R.id.tvUserName)
        private val emailTextView: TextView = itemView.findViewById(R.id.tvUserEmail)
        private val cityTextView: TextView = itemView.findViewById(R.id.tvUserCity)
        private val phoneTextView: TextView = itemView.findViewById(R.id.tvUserPhone)
        private val favoriteIcon: ImageView = itemView.findViewById(R.id.ivFavorite)
        private val syncIndicator: View = itemView.findViewById(R.id.vSyncIndicator)

        fun bind(user: User) {
            nameTextView.text = user.name
            emailTextView.text = user.email
            cityTextView.text = " ${user.address.city}"
            phoneTextView.text = " ${user.phone}"

            val favoriteIconRes = if (user.isFavorite) {
                R.drawable.ic_star_filled
            } else {
                R.drawable.ic_star_outline
            }
            favoriteIcon.setImageResource(favoriteIconRes)

            syncIndicator.visibility = if (user.pendingSync) View.VISIBLE else View.GONE

            favoriteIcon.setOnClickListener {
                onFavoriteClick(user)
            }
        }
    }

    class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}