package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nmedia.BuildConfig.BASE_URL
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg


import ru.netology.nmedia.databinding.FragmentViewPhotoBinding
import ru.netology.nmedia.handler.loadImage

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class ViewPhotoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentViewPhotoBinding.inflate(inflater, container, false)



        val imageUri = arguments?.textArg

        // Получаем из bundle uri и сразу в глайд
        binding.apply {
            imageUri?.let {
                val uri = "${BASE_URL}/media/${it}"
                bigPhoto.loadImage(uri)
            }


            bigPhoto.setOnClickListener {
                findNavController().popBackStack()
            }

        }





        return binding.root
    }
}