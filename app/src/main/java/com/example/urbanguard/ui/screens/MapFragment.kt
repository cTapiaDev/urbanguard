package com.example.urbanguard.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.urbanguard.R
import com.example.urbanguard.core.BaseFragment
import com.example.urbanguard.core.extension.collectFlow
import com.example.urbanguard.databinding.FragmentMapBinding
import com.example.urbanguard.domain.model.Report
import com.example.urbanguard.ui.viewmodel.HomeViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber


@AndroidEntryPoint
class MapFragment : BaseFragment<FragmentMapBinding>(FragmentMapBinding::inflate), OnMapReadyCallback {

    private val viewModel: HomeViewModel by viewModels()
    private var googleMap: GoogleMap? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMap()
        initObservers()
    }

    private fun initMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // 🟢 Habilitamos los controles de zoom (+ y -)
        map.uiSettings.isZoomControlsEnabled = true

        // 🟢 Habilitamos la ubicación actual (Punto Azul) solo si el usuario dio permisos
        enableMyLocationIfPermitted()

        // 🟢 Forzamos el renderizado en caso de que la data haya llegado antes de que el mapa cargara
        renderMapMarkersIfAvailable()
    }

    private fun enableMyLocationIfPermitted() {
        val context = requireContext()
        val hasFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocation) {
            // 🟢 Activa el punto azul y el botón de centrar cámara
            googleMap?.isMyLocationEnabled = true
            googleMap?.uiSettings?.isMyLocationButtonEnabled = true
        }
    }

    private fun initObservers() {
        collectFlow(viewModel.uiState) { state ->
            if (state is HomeViewModel.HomeUiState.Success) {
                renderMarkersAndZoom(state.reports)
            }
        }
    }

    private fun renderMapMarkersIfAvailable() {
        val currentState = viewModel.uiState.value
        if (currentState is HomeViewModel.HomeUiState.Success) {
            renderMarkersAndZoom(currentState.reports)
        }
    }

    private fun renderMarkersAndZoom(reports: List<Report>) {
        val map = googleMap ?: return
        map.clear()

        if (reports.isEmpty()) return

        val boundsBuilder = LatLngBounds.Builder()
        var validMarkersCount = 0
        var lastPosition: LatLng? = null

        // 🟢 Inyectamos todos los marcadores en el mapa
        reports.forEach { report ->
            Timber.d("🟢 REPORTE: ${report.title} | LAT: ${report.latitude} | LNG: ${report.longitude}")
            // Nota vital: Asegúrate de que en tu Base de Datos 'latitude' y 'longitude' no vengan nulos
            if (report.latitude != null && report.longitude != null) {
                val position = LatLng(report.latitude, report.longitude)
                map.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title(report.title)
                )
                boundsBuilder.include(position)
                lastPosition = position
                validMarkersCount++
            }
        }

        // 🟢 Si no hay marcadores válidos (porque las coordenadas venían nulas), abortamos el movimiento
        if (validMarkersCount == 0) return

        // 🟢 Usamos view?.post para asegurar que el mapa ya tiene dimensiones en la pantalla antes de mover la cámara
        view?.post {
            try {
                if (validMarkersCount == 1 && lastPosition != null) {
                    // 🟢 Si solo hay 1 marcador (Ej: Alaska), hacemos zoom directo al punto
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(lastPosition!!, 12f))
                } else {
                    // 🟢 Si hay varios marcadores, calculamos el encuadre general (Bounds)
                    val bounds = boundsBuilder.build()
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))
                }
            } catch (e: Exception) {
                // Previene caídas si la vista no estaba 100% lista
                e.printStackTrace()
            }
        }
    }
}