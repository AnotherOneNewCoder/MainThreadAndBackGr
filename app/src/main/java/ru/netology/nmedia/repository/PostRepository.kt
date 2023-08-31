package ru.netology.nmedia.repository



import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post


interface PostRepository {
    val data: Flow<PagingData<FeedItem>>

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

    fun cleanPostRemoteKeyDao()
}
