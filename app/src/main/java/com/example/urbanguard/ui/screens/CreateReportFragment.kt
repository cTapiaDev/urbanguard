package com.example.urbanguard.ui.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.urbanguard.R
import com.example.urbanguard.core.BaseFragment
import com.example.urbanguard.databinding.FragmentCreateReportBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateReportFragment : BaseFragment<FragmentCreateReportBinding>(FragmentCreateReportBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}