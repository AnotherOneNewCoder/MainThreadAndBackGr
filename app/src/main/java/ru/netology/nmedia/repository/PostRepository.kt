package ru.netology.nmedia.repository


import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import java.io.File

interface PostRepository {
    val data: Flow<PagingData<Post>>

    // пришлось создать для работы newCount
    val dataForNewCountPosts: Flow<List<Post>>
    suspend fun getAll()

    fun getNewerCount(id: Long) : Flow<Int>
    suspend fun save(post: Post)
    suspend fun saveWithAttachment(post: Post, upload: MediaUpload)
    suspend fun uploadMedia(upload: MediaUpload): Media

    suspend fun likeById(id: Long)
    suspend fun removeById(id: Long)

    suspend fun unlikeByID(id: Long)
    suspend fun getAllUnhide()
}
