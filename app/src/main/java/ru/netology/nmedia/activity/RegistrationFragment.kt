package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentRegistrationBinding
import ru.netology.nmedia.util.AndroidUtils

import ru.netology.nmedia.viewmodel.RegistrViewModel

class RegistrationFragment : Fragment() {
    private val viewModel: RegistrViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentRegistrationBinding.inflate(inflater, container, false)

        viewModel.data.observe(viewLifecycleOwner) {
            AppAuth.getInstance().setAuth(it.id, it.token)
            findNavController().navigateUp()
        }
        if (!binding.enableAvatar.isActivated) {
            viewModel.state.observe(viewLifecycleOwner) { state ->
                if (state.registrationError) {
                    Snackbar.make(binding.root, "Registration error", Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.retry_loading) {
                            viewModel.register(
                                login = binding.login.text.toString(),
                                passwd = binding.password.text.toString(),
                                name = binding.name.text.toString()
                            )
                        }
                        .show()
                }

            }


            binding.apply {
                name.requestFocus()
                enter.setOnClickListener {
                    if (password.text.toString() == confirmPassword.text.toString()
//                    && !password.text.isNullOrBlank()
//                    && !login.text.isNullOrBlank()
//                    && !name.text.isNullOrBlank()
                    ) {
                        viewModel.register(
                            login = login.text.toString(),
                            passwd = password.text.toString(),
                            name = name.text.toString()
                        )

                        AndroidUtils.hideKeyboard(requireView())
                        findNavController().navigateUp()
                    } else
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.fill_all_fields),
                            Toast.LENGTH_SHORT
                        ).show()
                }
            }
        } else {
            viewModel.state.observe(viewLifecycleOwner) { state ->
                if (state.registrationError) {
                    Snackbar.make(binding.root, "Registration error", Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.retry_loading) {
                            viewModel.registerWithPhoto(
                                login = binding.login.text.toString(),
                                passwd = binding.password.text.toString(),
                                name = binding.name.text.toString(),
                                upload = binding.avatar.
                            )
                        }
                        .show()
                }

            }
        }
        return binding.root
    }
}

