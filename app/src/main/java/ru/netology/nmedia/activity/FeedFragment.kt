package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg

import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.RetryTypes
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class FeedFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)
        val authViewModel by viewModels<AuthViewModel>()
        authViewModel.data.observe(viewLifecycleOwner)
        {
            val authenticated = authViewModel.isAuthenticated
            val adapter = PostsAdapter(object : OnInteractionListener {

                override fun onEdit(post: Post) {

                    viewModel.edit(post)
                    findNavController().navigate(R.id.action_feedFragment_to_newPostFragment,
                        Bundle().apply
                        { textArg = post.content })
                }


                override fun onLike(post: Post) {
                    if (authenticated) {
                        if (!post.likedByMe) {
                            viewModel.likeById(post.id)
                        } else {
                            viewModel.unlikeByID(post.id)
                        }
                    } else {
                        binding.list.
                        findNavController().navigate(R.id.loginDialog)
//                        Snackbar.make(binding.root, getString(R.string.snak_auth), BaseTransientBottomBar.LENGTH_SHORT,
//                            )
//                            .setAction(getString(R.string.confirm)) {
//                                findNavController().navigate(R.id.action_feedFragment_to_logInFragment)
//                            }
//                            .show()

                    }


                }

                override fun onImageClicked(uri: String) {
                    findNavController().navigate(R.id.action_feedFragment_to_viewPhotoFragment,
                        Bundle().apply
                        { textArg = uri })
                }

                override fun onRemove(post: Post) {
                    viewModel.removeById(post.id)
                }

                override fun onShare(post: Post) {
                    if (authenticated) {
                        val intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, post.content)
                            type = "text/plain"
                        }

                        val shareIntent =
                            Intent.createChooser(intent, getString(R.string.chooser_share_post))
                        startActivity(shareIntent)
                    } else {
                        Snackbar.make(binding.root, getString(R.string.snak_auth), BaseTransientBottomBar.LENGTH_SHORT,
                        )
                            .setAction(getString(R.string.confirm)) {
                                findNavController().navigate(R.id.action_feedFragment_to_logInFragment)
                            }
                            .show()
                    }
                }
            })



            binding.list.adapter = adapter
            viewModel.dataState.observe(viewLifecycleOwner) { state ->
                binding.progress.isVisible = state.loading
                binding.errorGroup.isVisible = state.error
                binding.refresher.isRefreshing = state.refreshing


                if (state.error) {
                    Snackbar.make(
                        binding.root,
                        R.string.error_loading,
                        BaseTransientBottomBar.LENGTH_SHORT,

                        )
                        .setAction("Retry") {
                            when (state.retryType) {
                                RetryTypes.REMOVE -> viewModel.removeById(state.retryId)
                                RetryTypes.LIKE -> viewModel.likeById(state.retryId)
                                RetryTypes.UNLIKE -> viewModel.unlikeByID(state.retryId)
                                RetryTypes.SAVE -> viewModel.retrySave(state.retryPost)


                                else -> viewModel.loadPosts()

                            }
                        }
                        .show()
                }

            }


            viewModel.data.observe(viewLifecycleOwner) { state ->
                adapter.submitList(state.posts)
                binding.emptyText.isVisible = state.empty
            }



            binding.retryButton.setOnClickListener {
                viewModel.loadPosts()
            }

            viewModel.newCount.observe(viewLifecycleOwner) {
                if (it > 0) {
                    binding.apply {
                        newPosts.visibility = View.VISIBLE
                        newPosts.text = "New posts: " + it.toString()
                        newPosts.setOnClickListener {
                            viewModel.getAllUnhide()
                            viewModel.loadPosts()
                            newPosts.visibility = View.INVISIBLE

                        }

                    }
                } else {
                    binding.newPosts.visibility = View.INVISIBLE
                }
            }
            adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    if (positionStart == 0) {
                        binding.list.smoothScrollToPosition(0)

                    }
                }
            })

            binding.refresher.setColorSchemeResources(R.color.colorAccent)
            binding.refresher.setOnRefreshListener {
                viewModel.refreshPosts()
            }

            binding.fab.setOnClickListener {
                if (authenticated) {
                    findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
                } else {
                    findNavController().navigate(R.id.loginDialog)
//                    Snackbar.make(binding.root, getString(R.string.snak_auth), BaseTransientBottomBar.LENGTH_SHORT,
//                    )
//                        .setAction(getString(R.string.confirm)) {
//                            findNavController().navigate(R.id.action_feedFragment_to_logInFragment)
//                        }
//                        .show()
                }
            }
        }
        return binding.root
    }
}
