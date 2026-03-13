package com.example.urbanguard.ui.screens

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.urbanguard.R
import com.example.urbanguard.core.BaseFragment
import com.example.urbanguard.core.extension.collectFlow
import com.example.urbanguard.core.utils.MemoryCrusher
import com.example.urbanguard.databinding.FragmentHomeBinding
import com.example.urbanguard.domain.model.Report
import com.example.urbanguard.ui.adapter.ReportAdapter
import com.example.urbanguard.ui.viewmodel.HomeViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate), OnMapReadyCallback {

    private val viewModel: HomeViewModel by viewModels()

    private var googleMap: GoogleMap? = null
    private var isShowingMap = false
    private lateinit var reportAdapter: ReportAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMap()
        setupRecyclerView()
        initListeners()
        initObservers()
    }

    private fun initMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_container) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
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
        binding.fabToggleView.setOnClickListener {
            isShowingMap = !isShowingMap
            updateUiMode()
        }
    }

    private fun updateUiMode() {
        if (isShowingMap) {
            binding.rvReports.visibility = View.GONE
            binding.llEmptyState.visibility = View.GONE
            binding.mapContainer.visibility = View.VISIBLE
            binding.fabToggleView.setImageResource(R.drawable.ic_view_list_24)
        } else {
            binding.rvReports.visibility = View.VISIBLE
            binding.mapContainer.visibility = View.GONE
            binding.fabToggleView.setImageResource(R.drawable.ic_map_24)

            if (viewModel.uiState.value is HomeViewModel.HomeUiState.Empty) {
                binding.llEmptyState.visibility = View.VISIBLE
            }
        }
    }



    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is HomeViewModel.HomeUiState.Loading -> { /* AGREGAR UN PROGRESS ANIMADO */ }
                        is HomeViewModel.HomeUiState.Empty -> {
                            binding.rvReports.isVisible = false
                            googleMap?.clear()
                            if (!isShowingMap) {
                                binding.llEmptyState.visibility = View.VISIBLE
                            }
                        }
                        is HomeViewModel.HomeUiState.Success -> {
                            binding.llEmptyState.visibility = View.GONE
                            binding.rvReports.isVisible = true
                            reportAdapter.submitList(state.reports)

                            renderMapMarkers(state.reports)
                        }
                        is HomeViewModel.HomeUiState.Error -> { /* MENSAJE DE ERROR O ANIMACIÓN */ }
                    }
                }
            }
        }

        /* collectFlow(viewModel.uiState) { state ->
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
        } */
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.uiSettings.isZoomControlsEnabled = true
        renderMapMarkersIfAvailable()
    }

    private fun renderMapMarkers(reports: List<Report>) {
        googleMap?.let { map ->
            map.clear()
            reports.forEach { report ->
                if (report.latitude != null && report.longitude != null) {
                    val position = LatLng(report.latitude, report.longitude)
                    map.addMarker(
                        MarkerOptions()
                            .position(position)
                            .title(report.title)
                    )
                }
            }
        }
    }

    private fun renderMapMarkersIfAvailable() {
        val currentState = viewModel.uiState.value
        if (currentState is HomeViewModel.HomeUiState.Success) {
            renderMapMarkers(currentState.reports)
        }
    }


}