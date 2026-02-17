package com.example.urbanguard.ui.screens

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
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

@AndroidEntryPoint
class CreateReportFragment : BaseFragment<FragmentCreateReportBinding>(FragmentCreateReportBinding::inflate) {

    private val viewModel: CreateReportViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        initObservers()
    }

    private fun initListeners() {
        with(binding) {
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

            cvPhotoContainer.setOnClickListener {
                viewModel.onPhotoSelected("https://i.pinimg.com/736x/7f/af/b9/7fafb9d4b589a67f9115f9a258a2b4a4.jpg".toUri())
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