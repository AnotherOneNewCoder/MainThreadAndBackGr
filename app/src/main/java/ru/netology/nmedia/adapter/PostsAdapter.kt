package ru.netology.nmedia.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter


import androidx.recyclerview.widget.DiffUtil

import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardAdBinding
import ru.netology.nmedia.databinding.CardDateBinding
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Ad
import ru.netology.nmedia.dto.DateSeparator
import ru.netology.nmedia.dto.FeedItem
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
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostDiffCallback()) {
    private val typeAd = 0
    private val typePost = 1
    private val typeDate = 2
    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Ad -> typeAd
            is Post -> typePost
            is DateSeparator -> typeDate
            // иногда на этом месте приходит -1 и выбрасывается ошибка, но я ее не могу поймать
            null -> error("unknown item type ${getItemId(position)}")
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            typeAd -> AdViewHolder(
                CardAdBinding.inflate(layoutInflater, parent, false),

            )

            typePost -> PostViewHolder(
                CardPostBinding.inflate(layoutInflater, parent, false),
                onInteractionListener
            )

            typeDate -> DateViewHolder(
                CardDateBinding.inflate(layoutInflater, parent, false),
            )

            else -> throw IllegalArgumentException("unknown view type: $viewType")
        }
    }
//        when (viewType) {
//            R.layout.card_post -> {
//                val binding =
//                    CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//                PostViewHolder(binding, onInteractionListener)
//            }
//            R.layout.card_ad -> {
//                val binding =
//                    CardAdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//                AdViewHolder(binding)
//            }
//            R.layout.card_date -> {
//                val binding = CardDateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//                DateViewHolder(binding)
//            }
//            else -> error("unknow view type: $viewType")
//        }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(val item = getItem(position)) {
            is Ad -> (holder as? AdViewHolder)?.bind(item)
            is Post -> (holder as? PostViewHolder)?.bind(item)
            is DateSeparator -> (holder as? DateViewHolder)?.bind(item)
            null -> error("unknown item type")
        }
    }
}

class AdViewHolder(
    private val binding: CardAdBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(ad: Ad) {
        binding.apply {
            val pathToImage = "${BuildConfig.BASE_URL}/media/${ad.image}"
            image.loadImage(pathToImage)
        }
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,

    ) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {
        binding.apply {
            val pathToAvatarImage = "${BuildConfig.BASE_URL}/avatars/${post.authorAvatar}"
            val pathToAttachmentImage = "${BuildConfig.BASE_URL}/media/${post.attachment?.url}"
            author.text = post.author
            published.text = post.published.toString()
            content.text = post.content
            avatar.load(pathToAvatarImage)
            // в адаптере
            like.isCheckable = true
            like.isChecked = post.likedByMe
            like.isCheckable = false
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
            // отключил для отображения кнопи лайка
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
class DateViewHolder(
    private val binding: CardDateBinding
): RecyclerView.ViewHolder(binding.root) {
    fun bind(date: DateSeparator) {
        val source = when(date.type) {
            DateSeparator.Type.TODAY -> R.string.today
            DateSeparator.Type.YESTERDAY -> R.string.yesterday
            DateSeparator.Type.WEEK_AGO -> R.string.week_ago
        }
        binding.root.setText(source)
    }
}
class PostDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }
}
