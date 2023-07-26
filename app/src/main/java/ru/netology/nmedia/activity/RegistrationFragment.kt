package ru.netology.nmedia.activity

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentRegistrationBinding
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.handler.loadImage
import ru.netology.nmedia.util.AndroidUtils

import ru.netology.nmedia.viewmodel.RegistrViewModel

class RegistrationFragment : Fragment() {
    private val viewModel: RegistrViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )
    private val avatarLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = it.data?.data
            viewModel.changeAvatar(uri, uri?.toFile())

        } else {
            Toast.makeText(
                requireContext(),
                R.string.error_failed_pick_gallery_image,
                Toast.LENGTH_SHORT
            ).show()
        }

    }


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
        if (binding.enableAvatar.isActivated) {
            binding.avatarsGroup.isVisible = false
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
                            val file = viewModel.avatar.value?.file
                            if (file != null)
                                viewModel.registerWithPhoto(
                                    login = binding.login.text.toString(),
                                    passwd = binding.password.text.toString(),
                                    name = binding.name.text.toString(),
                                    upload = MediaUpload(file)
                                )
                        }
                        .show()
                }

            }

            binding.apply {
                avatarsGroup.isVisible = true
                takePhoto.setOnClickListener {
                    ImagePicker.with(requireActivity())
                        .cameraOnly()
                        .crop()
                        .compress(2048)
                        .createIntent(avatarLauncher::launch)
                }
                chooseFromGallery.setOnClickListener {
                    ImagePicker.with(requireActivity())
                        .galleryOnly()
                        .crop()
                        .compress(2048)
                        .createIntent(avatarLauncher::launch)
                }
                clearPhoto.setOnClickListener {
                    viewModel.clearPhoto()
                }
                viewModel.avatar.observe(viewLifecycleOwner) { avatar ->

                    binding.avatar.loadImage(avatar?.uri.toString())

                }
                name.requestFocus()
                enter.setOnClickListener {
                    if (password.text.toString() == confirmPassword.text.toString()
//                    && !password.text.isNullOrBlank()
//                    && !login.text.isNullOrBlank()
//                    && !name.text.isNullOrBlank()
                    ) {
                        val file = viewModel.avatar.value?.file
                        if (file != null) {
                            viewModel.registerWithPhoto(
                                login = login.text.toString(),
                                passwd = password.text.toString(),
                                name = name.text.toString(),
                                upload = MediaUpload(file)
                            )}

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

            }
            return binding.root
        }
    }

