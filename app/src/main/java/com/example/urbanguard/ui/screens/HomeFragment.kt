package com.example.urbanguard.ui.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.urbanguard.R
import com.example.urbanguard.core.BaseFragment
import com.example.urbanguard.core.extension.collectFlow
import com.example.urbanguard.databinding.FragmentHomeBinding
import com.example.urbanguard.ui.adapter.ReportAdapter
import com.example.urbanguard.ui.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var reportAdapter: ReportAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        initListeners()
        initObservers()
    }

    private fun setupRecyclerView() {
        reportAdapter = ReportAdapter { report ->
            Timber.d("Click en: ${report.title}")
        }
        binding.rvReports.adapter = reportAdapter
    }

    private fun initListeners() {
        binding.fabAddReport.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_createReportFragment2)
        }
    }

    private fun initObservers() {
        collectFlow(viewModel.uiState) { state ->
            when (state) {
                is HomeViewModel.HomeUiState.Loading -> { /* AGREGAR UN PROGRESS ANIMADO */ }
                is HomeViewModel.HomeUiState.Empty -> {
                    binding.llEmptyState.isVisible = true
                    binding.rvReports.isVisible = false
                }
                is HomeViewModel.HomeUiState.Success -> {
                    binding.llEmptyState.isVisible = false
                    binding.rvReports.isVisible = true
                    reportAdapter.submitList(state.reports)
                }
                is HomeViewModel.HomeUiState.Error -> { /* MENSAJE DE ERROR O ANIMACIÓN */ }
            }
        }
    }

}