package ru.netology.nmedia.dto

data class Post(
    val id: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val attachment: Attachments? = null
)

data class Attachments(
    val url: String,
    val description: String,
    val type: TypeAttachment

)
enum class TypeAttachment{
    IMAGE
}