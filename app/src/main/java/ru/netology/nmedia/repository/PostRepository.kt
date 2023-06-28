package ru.netology.nmedia.repository


import ru.netology.nmedia.dto.Post

interface PostRepository {



    fun getAllAsync(callback: RepositoryCallback<List<Post>>)
    fun likeByIdAsync(id: Long, callback: RepositoryCallback<Post>)
    fun unlikeByIDAsync(id: Long, callback: RepositoryCallback<Post>)
    fun removeByIdAsync(id: Long, callback: RepositoryCallback<Unit>)
    fun saveAsync(post: Post, callback: RepositoryCallback<Post>)




    interface RepositoryCallback<T> {
        fun onSuccess(value: T) {}
        fun onError(e: Exception) {}
    }
}
