package ru.netology.nmedia.dto

import java.time.LocalDateTime

sealed interface FeedItem{
    val id: Long
}

data class Post(
    override val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: LocalDateTime,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val saved: Boolean = false,
    val attachment: Attachments? = null,
    val ownedByMe:Boolean = false
): FeedItem
//в готовом коде здесь еще url: String
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

data class DateSeparator(
    val type: Type,

): FeedItem {
    override val id: Long = type.ordinal.toLong()
    enum class Type {
        TODAY,
        YESTERDAY,
        WEEK_AGO
    }



}