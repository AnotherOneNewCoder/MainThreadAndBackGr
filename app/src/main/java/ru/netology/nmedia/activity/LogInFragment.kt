package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentLogInBinding
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.viewmodel.LogInViewModel
import javax.inject.Inject

@AndroidEntryPoint
class LogInFragment: Fragment() {
    private val viewModel : LogInViewModel by viewModels()
    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentLogInBinding.inflate(inflater, container, false)

        viewModel.data.observe(viewLifecycleOwner) {
            appAuth.setAuth(it.id, it.token)
            Toast.makeText(requireContext(),getString(R.string.succ_log_in), Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }

        viewModel.state.observe(viewLifecycleOwner) {state ->
            if (state.loggingError) {
                Toast.makeText(requireContext(), getString(R.string.incorrect_passwd), Toast.LENGTH_LONG).show()
            }

        }


        binding.apply {
            login.requestFocus()
            enter.setOnClickListener {
                viewModel.login(
                    login.text.toString(),
                    password.text.toString()
                )
                AndroidUtils.hideKeyboard(requireView())
            }
        }
        return binding.root
    }
}