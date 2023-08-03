package ru.netology.nmedia.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible


import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.handler.load
import ru.netology.nmedia.handler.loadImage


interface OnInteractionListener {

    fun onImageClicked(uri: String) {}
    fun onLike(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onShare(post: Post) {}
}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener,
) : ListAdapter<Post, PostViewHolder>(PostDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,

) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {
        binding.apply {
            val pathToAvatarImage = "http://10.0.2.2:9999/avatars/${post.authorAvatar}"
            val pathToAttachmentImage = "http://10.0.2.2:9999/media/${post.attachment?.url}"
            author.text = post.author
            published.text = post.published
            content.text = post.content
            avatar.load(pathToAvatarImage)
            // в адаптере
            like.isCheckable = false
            //like.isChecked = post.likedByMe
            like.text = "${post.likes}"
            if (post.attachment != null) {
                postImage.visibility = View.VISIBLE
                postImage.loadImage(pathToAttachmentImage)
                postImage.setOnClickListener {
                    onInteractionListener.onImageClicked(post.attachment.url)
                }
            } else {
                postImage.visibility = View.GONE
            }
            if (!post.saved) {
                published.setText(R.string.waiting)
                like.visibility = View.INVISIBLE
            } else {
                like.visibility = View.VISIBLE
            }

            menu.isVisible = post.ownedByMe
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }
                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            like.setOnClickListener {
                like.isCheckable = true
                like.isChecked = post.likedByMe
                onInteractionListener.onLike(post)
            }

            share.setOnClickListener {
                onInteractionListener.onShare(post)
            }
            // попробую другой вариант, вариант выше почему-то сначала не срабатывал...
//            postImage.setOnClickListener {
//                post.attachment?.let { attachments ->
//                    onInteractionListener.onImageClicked(attachments.url)
//                }
//            }
        }
    }

}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}
