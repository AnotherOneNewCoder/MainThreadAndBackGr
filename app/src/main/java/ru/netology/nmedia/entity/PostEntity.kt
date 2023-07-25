package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Attachments
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.TypeAttachment

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val saved: Boolean,
    val hidden: Boolean = false,

    @Embedded
    val attachments: AttachmentEmbeddable?

) {
    fun toDto() = Post(id, authorId ,author,authorAvatar ,content, published, likedByMe, likes, saved, attachments?.toDto())

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(dto.id, dto.authorId ,dto.author,dto.authorAvatar, dto.content, dto.published, dto.likedByMe, dto.likes, dto.saved, false, AttachmentEmbeddable.fromDto(dto.attachment) )

    }
}
data class AttachmentEmbeddable(
    var url: String,
    var type: TypeAttachment,
) {
    fun toDto() = Attachments(url,type)

    companion object {
        fun fromDto(dto: Attachments?) = dto?.let {
            AttachmentEmbeddable(it.url, it.type)
        }
    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(hidden: Boolean = false): List<PostEntity> = map(PostEntity::fromDto).map {
    it.copy(hidden = hidden)
}
