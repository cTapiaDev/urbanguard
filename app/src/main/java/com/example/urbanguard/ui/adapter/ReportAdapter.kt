package com.example.urbanguard.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.urbanguard.databinding.ItemReportBinding
import com.example.urbanguard.domain.model.Report

class ReportAdapter(
    private val onReportClick: (Report) -> Unit
) : ListAdapter<Report, ReportAdapter.ReportViewHolder>(ReportDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReportAdapter.ReportViewHolder {
        val binding = ItemReportBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportAdapter.ReportViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ReportViewHolder(private val binding: ItemReportBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(report: Report) {
            with(binding) {
                tvTitle.text = report.title
                tvDescription.text = report.description
                tvStatus.text = report.status.name

                root.setOnClickListener {  }
            }
        }
    }

    class ReportDiffCallback : DiffUtil.ItemCallback<Report>() {
        override fun areItemsTheSame(
            oldItem: Report,
            newItem: Report
        ): Boolean {
            TODO("Not yet implemented")
        }

        override fun areContentsTheSame(
            oldItem: Report,
            newItem: Report
        ): Boolean {
            TODO("Not yet implemented")
        }

    }

}