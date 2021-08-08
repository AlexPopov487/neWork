package com.example.netologydiploma.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.netologydiploma.databinding.ItemLoadStateBinding

class PagingLoadStateAdapter(private val onRetryListener: () -> Unit) : LoadStateAdapter<LoadStateViewHolder>() {
    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateViewHolder {
            val binding = ItemLoadStateBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
            return LoadStateViewHolder(binding, onRetryListener )
        }
}

class LoadStateViewHolder(
    private val binding: ItemLoadStateBinding,
    private val onRetryListener: () -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(loadState: LoadState) {
        binding.apply {
            progressBar.isVisible = loadState is LoadState.Loading
            tVError.isVisible = loadState is LoadState.Error
            btRetry.isVisible = loadState is LoadState.Error

            if (loadState is LoadState.Error) {
                tVError.text = loadState.error.localizedMessage
            }

            btRetry.setOnClickListener {
                onRetryListener()
            }
        }
    }

}
