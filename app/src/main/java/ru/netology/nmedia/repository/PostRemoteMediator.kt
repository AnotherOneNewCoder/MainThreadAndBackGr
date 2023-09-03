package ru.netology.nmedia.repository
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity
import ru.netology.nmedia.error.ApiError
import java.io.IOException


@OptIn(ExperimentalPagingApi::class)

class PostRemoteMediator(
    private val api: PostApiService,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb,
) : RemoteMediator<Int, PostEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            val result = when (loadType) {
                LoadType.REFRESH -> {
                    val max = postRemoteKeyDao.max()
                    if (max != null) {
                        api.getAfter(max, state.config.pageSize)
                    } else {
//                        api.getLatest(state.lastItemOrNull()?.id!!.toInt())
                        //api.getLatest(state.config.jumpThreshold)
//                        val max = Int.MAX_VALUE
//                        api.getLatest(max)
                        // все равно этот метод только 30 новых постов покажет, а не все
                        api.getLatest(state.config.pageSize)
                    }
                }
                LoadType.PREPEND -> return MediatorResult.Success(true)


                LoadType.APPEND -> {
                    val id = postRemoteKeyDao.min() ?: return MediatorResult.Success(false)
                    api.getBefore(id, state.config.pageSize)
                }

            }
            if (!result.isSuccessful) {
                throw ApiError(result.code(), result.message())
            }
            val data = result.body() ?: throw ApiError(result.code(), result.message())

            appDb.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        if (postRemoteKeyDao.isEmpty()) {
                            postRemoteKeyDao.insert(
                                listOf(
                                    PostRemoteKeyEntity(
                                        PostRemoteKeyEntity.KeyType.AFTER,
                                        data.first().id,
                                    ),
                                    PostRemoteKeyEntity(
                                        PostRemoteKeyEntity.KeyType.BEFORE,
                                        data.last().id,
                                    )
                                )
                            )
                            postDao.deleteAll()
                        }
                        else {
                            postRemoteKeyDao.insert(PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.AFTER,
                                data.first().id,
                            ))
                        }
                    }

                    LoadType.APPEND -> {
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.BEFORE,
                                data.last().id,
                            )
                        )
                    }
                    else -> Unit
                }


                // попробую вернуть скрытие/отображение лайков
                postDao.insert(data.map { PostEntity.fromDto(it.copy(saved = true)) })
            }
            return MediatorResult.Success(data.isEmpty())
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        }

    }


}