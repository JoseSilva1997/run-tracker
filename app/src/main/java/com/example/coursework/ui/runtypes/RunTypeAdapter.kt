package com.example.coursework.ui.runtypes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.coursework.databinding.ItemRunTypeBinding
import com.example.coursework.domain.model.RunType

class RunTypeAdapter : ListAdapter<RunType, RunTypeAdapter.RunTypeViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunTypeViewHolder {
        val binding = ItemRunTypeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RunTypeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RunTypeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RunTypeViewHolder(
        private val binding: ItemRunTypeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(runType: RunType) {
            binding.runTypeName.text = runType.name
            binding.runTypeDistance.text = "${runType.targetDistanceMeters} meters"
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<RunType>() {
        override fun areItemsTheSame(oldItem: RunType, newItem: RunType): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RunType, newItem: RunType): Boolean {
            return oldItem == newItem
        }
    }
}
