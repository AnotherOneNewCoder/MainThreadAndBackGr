package ru.netology.nmedia.repository






import androidx.lifecycle.*
import androidx.paging.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody


import ru.netology.nmedia.api.*
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.*
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError

import java.io.IOException
import javax.inject.Inject


class PostRepositoryImpl @Inject constructor(
    private val dao: PostDao,
    private val apiService: PostApiService,
    postRemoteKeyDao: PostRemoteKeyDao,
    appDb: AppDb,
) : PostRepository {

    // before paging and done it in order to newCount could work
    // пришлось создать для работы newCount
    override val dataForNewCountPosts = dao.getAllVisible()
        .map(List<PostEntity>::toDto)
        .flowOn(Dispatchers.Default)

    // возможно здесь нужно будет сделать saved = true
    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<Post>> = Pager(
        config = PagingConfig(
            pageSize = 10,

            enablePlaceholders = false,
        ), pagingSourceFactory = {
            dao.getAllVisiblePagingSource()
        },
        remoteMediator = PostRemoteMediator(
            api = apiService,
            postDao = dao,
            postRemoteKeyDao = postRemoteKeyDao,
            appDb = appDb
            )
    ).flow
        .map { it.map(PostEntity::toDto)}

    override suspend fun getAll() {
        try {
            val response = apiService.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            val savedPosts = body.map { it.copy(saved = true) }
            dao.insert(savedPosts.toEntity(hidden = false))

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }

    }

    override fun getNewerCount(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000L)
            val response = apiService.getNewer(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.toEntity(hidden = true))
            //emit(body.size)
            emit(dao.count())
        }
    }
        .catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)

    override suspend fun save(post: Post) {
        val newPostId = post.copy(id = 9_223_372_036_854_775_800)
        try {
            dao.insert(PostEntity.fromDto(newPostId))
            val response = apiService.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            val savedOnServerPost = body.copy(saved = true)
            //dao.deleteAll()

            dao.insert(PostEntity.fromDto(savedOnServerPost))




        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }

        dao.removeById(newPostId.id)
    }

    override suspend fun saveWithAttachment(post: Post, upload: MediaUpload) {

        try {

//            val media = uploadMedia(upload)
//            val response = PostApi.retrofitService.save(post.copy(attachment = Attachments(url = media.id, TypeAttachment.IMAGE)))
//            if (!response.isSuccessful) {
//                throw ApiError(response.code(), response.message())
//            }
//            val body = response.body() ?: throw ApiError(response.code(), response.message())
//            val savedOnServerPost = body.copy(saved = true)
//            //dao.deleteAll()
//
//            dao.insert(PostEntity.fromDto(savedOnServerPost))

            val media = uploadMedia(upload)
            // TODO: add support for other types
            val postWithAttachment = post.copy(attachment = Attachments(media.id, TypeAttachment.IMAGE))
            save(postWithAttachment)


        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }



    }
override suspend fun uploadMedia(upload: MediaUpload): Media {
        try {


            val media = MultipartBody.Part.createFormData(
                "file", upload.file.name, upload.file.asRequestBody()
            )

            val response = apiService.uploadMedia(media)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
    override suspend fun removeById(id: Long) {
        try {
            dao.removeById(id)
            val response = apiService.removeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }


    }

    override suspend fun unlikeByID(id: Long) {
        try {
            dao.likeById(id)
            val response = apiService.unlikeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            val savedOnServerPost = body.copy(saved = true)

            dao.insert(PostEntity.fromDto(savedOnServerPost))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }

    }


    override suspend fun likeById(id: Long) {
        try {
            dao.likeById(id)
            val response = apiService.likeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            val savedOnServerPost = body.copy(saved = true)

            dao.insert(PostEntity.fromDto(savedOnServerPost))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
    override suspend fun getAllUnhide() {
        CoroutineScope(Dispatchers.Default).launch {
            dao.getAllUnhide()
        }

    }


}



