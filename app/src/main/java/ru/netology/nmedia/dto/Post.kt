package ru.netology.nmedia.dto

sealed interface FeedItem{
    val id: Long
}

data class Post(
    override val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val saved: Boolean = false,
    val attachment: Attachments? = null,
    val ownedByMe:Boolean = false
): FeedItem
data class Ad(
    override val id: Long,
    val image: String,
): FeedItem
data class Attachments(
    val url: String,

    val type: TypeAttachment

)
enum class TypeAttachment{
    IMAGE
}