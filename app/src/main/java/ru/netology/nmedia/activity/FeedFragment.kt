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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState

import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostLoadingStateAdapter
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
    private val authViewModel by viewModels<AuthViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)
        //val postRemoteKeyDao: PostRemoteKeyDao
//        authViewModel.data.observe(viewLifecycleOwner)
//        {
//            val authenticated = authViewModel.isAuthenticated

        val adapter = PostsAdapter(object : OnInteractionListener {

            override fun onEdit(post: Post) {

                viewModel.edit(post)
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment,
                    Bundle().apply
                    { textArg = post.content })
            }


            override fun onLike(post: Post) {
                if (authViewModel.isAuthenticated) {
                    if (!post.likedByMe) {
                        viewModel.likeById(post.id)
                    } else {
                        viewModel.unlikeByID(post.id)
                    }
                } else {
                    binding.list.findNavController().navigate(R.id.loginDialog)
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
                if (authViewModel.isAuthenticated) {
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, post.content)
                        type = "text/plain"
                    }

                    val shareIntent =
                        Intent.createChooser(intent, getString(R.string.chooser_share_post))
                    startActivity(shareIntent)
                } else {
                    Snackbar.make(
                        binding.root,
                        getString(R.string.snak_auth),
                        BaseTransientBottomBar.LENGTH_SHORT,
                    )
                        .setAction(getString(R.string.confirm)) {
                            findNavController().navigate(R.id.action_feedFragment_to_logInFragment)
                        }
                        .show()
                }
            }
        })
        fun scrolltoTop() {
            lifecycleScope.launch {
                val scrollingTop = adapter
                    .loadStateFlow
                    .distinctUntilChangedBy {
                        it.source.refresh
                    }
                    .map {
                        it.source.refresh is LoadState.NotLoading
                    }

                scrollingTop.collectLatest { scrolling ->
                    if (scrolling) binding.list.scrollToPosition(0)
                }
            }
        }

        binding.list.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PostLoadingStateAdapter { adapter.retry() },
            footer = PostLoadingStateAdapter { adapter.retry() },
        )

        lifecycleScope.launchWhenCreated {
            viewModel.data.collectLatest {
                adapter.submitData(it)
            }
        }

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


                            else -> //viewModel.loadPosts()
                                adapter.refresh()

                        }
                    }
                    .show()
            }

        }
        authViewModel.data.observe(viewLifecycleOwner) {
            adapter.refresh()
            scrolltoTop()
        }

        // before paging
//            viewModel.data.observe(viewLifecycleOwner) { state ->
//                adapter.submitList(state.posts)
//                binding.emptyText.isVisible = state.empty
//            }


        binding.retryButton.setOnClickListener {
            //viewModel.loadPosts()
            adapter.refresh()
        }

        viewModel.newCount.observe(viewLifecycleOwner) {
            if (it > 0) {
                binding.apply {
                    newPosts.visibility = View.VISIBLE
                    newPosts.text = "New posts: $it"
                    newPosts.setOnClickListener {
                        viewModel.getAllUnhide()
                        //viewModel.loadPosts()

                        viewModel.clearPostRemoteKeyDao()
                        adapter.refresh()
                        newPosts.visibility = View.INVISIBLE
                        scrolltoTop()

                    }

                }
            } else {
                binding.newPosts.visibility = View.INVISIBLE
            }
        }
        // отключил автоскролл
//        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
//            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
//                if (positionStart == 0) {
//                    binding.list.smoothScrollToPosition(0)
//
//                }
//            }
//        })

        // новый вариант псле вебинара
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { state ->
                    binding.refresher.isRefreshing =
                        state.refresh is LoadState.Loading
                }
            }
        }
        // вариант до вебинара
//        lifecycleScope.launchWhenCreated {
//            adapter.loadStateFlow.collectLatest {
//                binding.refresher.isRefreshing =
//                    it.refresh is LoadState.Loading || it.append is LoadState.Loading ||
//                            it.prepend is LoadState.Loading
//            }
//        }

        binding.refresher.setColorSchemeResources(R.color.colorAccent)
        binding.refresher.setOnRefreshListener {
            //viewModel.refreshPosts()
            viewModel.getAllUnhide()
            adapter.refresh()
            scrolltoTop()
        }

        binding.fab.setOnClickListener {
            if (authViewModel.isAuthenticated) {
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



//        }

        return binding.root
    }
}
