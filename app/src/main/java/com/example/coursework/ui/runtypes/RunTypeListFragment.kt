package com.example.coursework.ui.runtypes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coursework.databinding.FragmentRunTypeListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RunTypeListFragment : Fragment() {

    private var _binding: FragmentRunTypeListBinding? = null
    private val binding: FragmentRunTypeListBinding
        get() = _binding!!
    private val viewModel: RunTypeViewModel by viewModels()
    private val adapter = RunTypeAdapter()
    private var lastClearToken: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRunTypeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.runTypeRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.runTypeRecycler.adapter = adapter

        binding.addButton.setOnClickListener {
            val name = binding.nameInput.text?.toString().orEmpty()
            val distanceText = binding.distanceInput.text?.toString().orEmpty()
            val distance = distanceText.toIntOrNull() ?: 0
            viewModel.addRunType(name, distance)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.submitList(state.runTypes)

                    if (state.errorMessage.isNullOrBlank()) {
                        binding.errorText.visibility = View.GONE
                        binding.errorText.text = ""
                    } else {
                        binding.errorText.visibility = View.VISIBLE
                        binding.errorText.text = state.errorMessage
                    }

                    if (state.clearInputsToken != lastClearToken) {
                        lastClearToken = state.clearInputsToken
                        binding.nameInput.text?.clear()
                        binding.distanceInput.text?.clear()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
