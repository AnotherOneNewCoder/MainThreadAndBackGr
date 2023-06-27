package ru.netology.nmedia.repository


import retrofit2.Callback
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dto.Post



class PostRepositoryImpl : PostRepository {
    override fun getAllAsync(callback: PostRepository.RepositoryCallback<List<Post>>) {
        PostApi.retrofitService.getAll().enqueue(object : retrofit2.Callback<List<Post>> {
            override fun onResponse(
                call: retrofit2.Call<List<Post>>,
                response: retrofit2.Response<List<Post>>
            ) {
                if (!response.isSuccessful){
                    callback.onError(java.lang.RuntimeException(response.message() + response.code().toString()))

                    return
                }
                callback.onSuccess(response.body() ?: throw java.lang.RuntimeException("body is null"))
            }

            override fun onFailure(call: retrofit2.Call<List<Post>>, t: Throwable) {
                callback.onError(Exception(t))
            }

        })
    }
    override fun saveAsync(post: Post, callback: PostRepository.RepositoryCallback<Post>) {
        PostApi.retrofitService.save(post).enqueue(object : retrofit2.Callback<Post> {
            override fun onResponse(
                call: retrofit2.Call<Post>,
                response: retrofit2.Response<Post>
            ) {
                if (!response.isSuccessful){
                    callback.onError(java.lang.RuntimeException(response.message()+ response.code().toString()))
                    return
                }
                callback.onSuccess(response.body() ?: throw java.lang.RuntimeException("body is null"))
            }

            override fun onFailure(call: retrofit2.Call<Post>, t: Throwable) {
                callback.onError(Exception(t))
            }

        })
    }

    override fun removeByIdAsync(id: Long, callback: PostRepository.RepositoryCallback<Unit>) {
        PostApi.retrofitService.removeById(id).enqueue(object : Callback<Unit> {
            override fun onResponse(
                call: retrofit2.Call<Unit>,
                response: retrofit2.Response<Unit>
            ) {
                if (!response.isSuccessful) {
                    callback.onError(java.lang.RuntimeException(response.message()+ response.code().toString()))
                    return
                }
                callback.onSuccess(response.body() ?: throw java.lang.RuntimeException("body is null"))
            }

            override fun onFailure(call: retrofit2.Call<Unit>, t: Throwable) {
                callback.onError(Exception(t))
            }

        })
    }

    override fun unlikeByIDAsync(id: Long, callback: PostRepository.RepositoryCallback<Post>) {
        PostApi.retrofitService.unlikeById(id).enqueue(object : retrofit2.Callback<Post> {
            override fun onResponse(
                call: retrofit2.Call<Post>,
                response: retrofit2.Response<Post>
            ) {
                if (!response.isSuccessful) {
                    callback.onError(java.lang.RuntimeException(response.message()+ response.code().toString()))
                    return
                }
                callback.onSuccess(response.body() ?: throw java.lang.RuntimeException("body is null"))
            }

            override fun onFailure(call: retrofit2.Call<Post>, t: Throwable) {
                callback.onError(Exception(t))
            }

        })
    }


    override fun likeByIdAsync(id: Long, callback: PostRepository.RepositoryCallback<Post>) {
        PostApi.retrofitService.likeById(id).enqueue(object : retrofit2.Callback<Post> {
            override fun onResponse(
                call: retrofit2.Call<Post>,
                response: retrofit2.Response<Post>
            ) {
                if (!response.isSuccessful) {
                    callback.onError(java.lang.RuntimeException(response.message()+ response.code().toString()))
                    return
                }
                callback.onSuccess(response.body() ?: throw java.lang.RuntimeException("body is null"))
            }

            override fun onFailure(call: retrofit2.Call<Post>, t: Throwable) {
                callback.onError(Exception(t))
            }

        })
    }


}



