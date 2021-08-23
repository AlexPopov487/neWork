package com.example.netologydiploma.adapter

import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.netologydiploma.R
import com.example.netologydiploma.databinding.JobListItemBinding
import com.example.netologydiploma.dto.Job
import com.example.netologydiploma.util.AndroidUtils
import me.saket.bettermovementmethod.BetterLinkMovementMethod

interface OnJobButtonInteractionListener {
    fun onDeleteJob(job: Job)
    fun onLinkClicked(url: String)
}

class JobAdapter(private val onJobButtonInteractionListener: OnJobButtonInteractionListener) :
    ListAdapter<Job, JobViewHolder>(JobDiffItemCallback) {
    object JobDiffItemCallback : DiffUtil.ItemCallback<Job>() {
        override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = JobListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding, onJobButtonInteractionListener)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class JobViewHolder(
    private val binding: JobListItemBinding,
    private val onJobButtonInteractionListener: OnJobButtonInteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(job: Job) {
        val startJob = AndroidUtils.formatMillisToDateString(job.start)
        val endJob = AndroidUtils.formatMillisToDateString(job.finish)
            ?: itemView.context.getString(R.string.job_blank_end_date_text)
        with(binding) {
            jobOptions.visibility = View.GONE
            jobOverView.visibility = View.VISIBLE

            tvJobCompany.text = job.name
            tvJobPosition.text = job.position
            tvJobPeriod.text =
                itemView.context.getString(R.string.job_employment_term, startJob, endJob)
            tvJobLink.text = job.link
            BetterLinkMovementMethod.linkify(Linkify.WEB_URLS, tvJobLink)
                .setOnLinkClickListener { textView, url ->
                    onJobButtonInteractionListener.onLinkClicked(url)
                    true
                }

            root.setOnLongClickListener {
                jobOptions.visibility = View.VISIBLE
                jobOverView.visibility = View.INVISIBLE
                true
            }

            btDeleteJob.setOnClickListener {
                onJobButtonInteractionListener.onDeleteJob(job)
            }

            btCancelJobDeletion.setOnClickListener {
                jobOptions.visibility = View.GONE
                jobOverView.visibility = View.VISIBLE

            }
        }

    }
}
