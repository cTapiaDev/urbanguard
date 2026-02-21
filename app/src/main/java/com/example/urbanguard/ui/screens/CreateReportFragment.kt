package com.example.urbanguard.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.urbanguard.R
import com.example.urbanguard.core.BaseFragment
import com.example.urbanguard.databinding.FragmentCreateReportBinding
import com.example.urbanguard.ui.viewmodel.CreateReportViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.example.urbanguard.core.extension.collectFlow
import com.example.urbanguard.ui.viewmodel.state.CreateReportUiEvent
import com.example.urbanguard.ui.viewmodel.state.CreateReportUiState
import timber.log.Timber
import java.io.File

@AndroidEntryPoint
class CreateReportFragment : BaseFragment<FragmentCreateReportBinding>(FragmentCreateReportBinding::inflate) {

    private val viewModel: CreateReportViewModel by viewModels()
    private var imageCapture: ImageCapture? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(requireContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickMediaLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onPhotoSelected(uri)
            binding.ivReportPhoto.setImageURI(uri)
            binding.ivReportPhoto.visibility = View.VISIBLE
            binding.viewFinder.visibility = View.GONE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkCameraPermission()
        initListeners()
        initObservers()
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
                    viewModel.onPhotoSelected(savedUri)
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
            btnSubmitReport.isEnabled = !state.isLoading && state.isFormValid
            btnSubmitReport.text = if (state.isLoading) "Enviando..." else "Enviar Reporte"

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
}