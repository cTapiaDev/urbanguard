package com.example.urbanguard.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.example.urbanguard.core.BaseFragment
import com.example.urbanguard.databinding.FragmentCreateReportBinding
import com.example.urbanguard.ui.viewmodel.CreateReportViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.example.urbanguard.core.extension.collectFlow
import com.example.urbanguard.ui.viewmodel.state.CreateReportUiEvent
import com.example.urbanguard.ui.viewmodel.state.CreateReportUiState
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import timber.log.Timber
import java.io.File
import java.util.Locale

@AndroidEntryPoint
class CreateReportFragment : BaseFragment<FragmentCreateReportBinding>(FragmentCreateReportBinding::inflate) {

    private val viewModel: CreateReportViewModel by viewModels()
    private var imageCapture: ImageCapture? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkAllPermissions()

        //checkCameraPermission()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        initListeners()
        initObservers()

    }



    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(requireContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted || coarseGranted) {
            fetchDeviceLocation()
        } else {
            Toast.makeText(requireContext(), "Sin ubicación no se puede generar el reporte", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchDeviceLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                viewModel.onLocationCaptured(location.latitude, location.longitude)
                getAddressFromCoords(location.latitude, location.longitude)
            } else {
                val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100).build()

                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { freshLocation: Location? ->
                        freshLocation?.let {
                            viewModel.onLocationCaptured(it.latitude, it.longitude)
                            getAddressFromCoords(it.latitude, it.longitude)
                        } ?: run {
                            Toast.makeText(requireContext(), "GPS apagado o sin señal. Intentalo de nuevo", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    private fun checkDeviceSettingsAndGetLocation() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build()
        val builder = LocationSettingsRequest.Builder().addAllLocationRequests(locationRequest as Collection<LocationRequest?>)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val task = settingsClient.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            fetchDeviceLocation()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(requireActivity(), 1001)
                } catch (sendEx: IntentSender.SendIntentException) {

                }
            }
        }
    }

    private fun getAddressFromCoords(lat: Double, lng: Double) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Esto es para Android 13+ (API 33)
            geocoder.getFromLocation(lat, lng, 1) { addresses ->
                val address = addresses.firstOrNull()?.getAddressLine(0)
                activity?.runOnUiThread {
                    viewModel.onAddressUpdate(address ?: "Dirección no encontrada")
                }
            }
        } else {
            try {
                @Suppress("DEPRECATION")
                val address = geocoder.getFromLocation(lat, lng, 1)?.firstOrNull()?.getAddressLine(0)
                viewModel.onAddressUpdate(address ?: "Dirección no encontrada")
            } catch (e: Exception) {
                viewModel.onAddressUpdate("Error al obtener la dirección")
            }
        }
    }

    private val pickMediaLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onPhotoSelected(requireContext(), uri)
            binding.ivReportPhoto.setImageURI(uri)
            binding.ivReportPhoto.visibility = View.VISIBLE
            binding.viewFinder.visibility = View.GONE
        }
    }



    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }
            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(binding.viewFinder.surfaceProvider) }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Timber.e("Error al iniciar la cámara: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = File(
            requireContext().externalCacheDir,
            "URBAN_REPORT_${System.currentTimeMillis()}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    viewModel.onPhotoSelected(requireContext(), savedUri)
                    binding.ivReportPhoto.setImageURI(savedUri)
                    binding.ivReportPhoto.visibility = View.VISIBLE
                    binding.viewFinder.visibility = View.GONE
                }

                override fun onError(exception: ImageCaptureException) {
                    Timber.e("Error de captura: ${exception.message}")
                }

            }
        )
    }



    private fun initListeners() {
        with(binding) {
            btnCapturePhoto.setOnClickListener { takePhoto() }

            etTitle.doAfterTextChanged { text ->
                viewModel.onTitleChanged(text.toString())
            }

            etDescription.doAfterTextChanged { text ->
                viewModel.onDescriptionChanged(text.toString())
            }

            btnSubmitReport.setOnClickListener {
                viewModel.submitReport()
                hideKeyboard()
            }

            binding.llPhotoPlaceholder.setOnClickListener {
                pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }
    }

    private fun initObservers() {
        collectFlow(viewModel.uiState) { state ->
            renderState(state)
        }

        collectFlow(viewModel.uiEvent) { event ->
            handleEvent(event)
        }
    }

    private fun renderState(state: CreateReportUiState) {
        with(binding) {
            etLocation.setText(state.address)
            btnSubmitReport.isEnabled = !state.isLoading && state.isFormValid
            btnSubmitReport.text = if (state.isLoading) "Enviando..." else "Enviar Reporte"

            tilLocation.helperText = if (state.latitude == null) "Buscando satélites..." else "Ubicación fijada"

            tilTitle.error = state.titleError?.let { getString(it) }

            if (state.photoUri != null) {
                ivReportPhoto.isVisible = true
                llPhotoPlaceholder.isVisible = false
            }
        }
    }

    private fun handleEvent(event: CreateReportUiEvent) {
        when(event) {
            is CreateReportUiEvent.ShowError -> {
                Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
            }
            CreateReportUiEvent.SuccessNavigation -> {
                Toast.makeText(requireContext(), "¡Reporte creado!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun checkAllPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
}