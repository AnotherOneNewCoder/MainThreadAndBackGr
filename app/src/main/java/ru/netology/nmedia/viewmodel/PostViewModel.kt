package ru.netology.nmedia.viewmodel

import android.app.Application

import androidx.lifecycle.*
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.SingleLiveEvent


private val empty = Post(
    id = 0,
    content = "",
    author = "",
    authorAvatar = "",
    likedByMe = false,
    likes = 0,
    published = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    // упрощённый вариант
    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }

    fun loadPosts() {
        _data.value = FeedModel(loading = true)
        repository.getAllAsync(object : PostRepository.RepositoryCallback<List<Post>> {
            override fun onSuccess(posts: List<Post>) {
                _data.value = FeedModel(posts = posts, empty = posts.isEmpty())
            }

            override fun onError(e: Exception) {
                _data.value = FeedModel(error = true, errorCodeMessage = e.message.toString())
            }
        })
    }

    fun save() {
        edited.value?.let {

            repository.saveAsync(it, object : PostRepository.RepositoryCallback<Post> {
                override fun onSuccess(value: Post) {
                    _postCreated.postValue(Unit)
                }

                override fun onError(e: Exception) {
                    _data.value = FeedModel(errorCodeMessage = e.message.toString())
                }
            })


        }
        edited.value = empty


    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }



    fun likeById(id: Long) {
        val oldList = _data.value?.posts.orEmpty()
        val updatedList = _data.value?.posts.orEmpty().map {
            if (it.id == id){
                it.copy(likedByMe = !it.likedByMe, likes = if(it.likedByMe) it.likes -1 else it.likes + 1)
            } else it
        }
        _data.value = FeedModel(posts = updatedList, empty = updatedList.isEmpty())
        repository.likeByIdAsync(id, object : PostRepository.RepositoryCallback<Post> {
            override fun onSuccess(post: Post) {

            }

            override fun onError(e: Exception) {
                _data.value = FeedModel(posts = oldList, empty = oldList.isEmpty(), errorCodeMessage = e.message.toString())
            }
        })

    }
    fun unlikeByID(id: Long) {
        val oldList = _data.value?.posts.orEmpty()
        val updatedList = _data.value?.posts.orEmpty().map {
            if (it.id == id){
                it.copy(likedByMe = !it.likedByMe, likes = if(it.likedByMe) it.likes -1 else it.likes + 1)
            } else it
        }
        _data.value = FeedModel(posts = updatedList, empty = updatedList.isEmpty())
        repository.unlikeByIDAsync(id, object : PostRepository.RepositoryCallback<Post>{
            override fun onSuccess(post: Post) {

            }

            override fun onError(e: Exception) {
                _data.value = FeedModel(posts = oldList, empty = oldList.isEmpty() , errorCodeMessage = e.message.toString())
            }
        })
    }


    fun removeById(id: Long) {
        val oldList = _data.value?.posts.orEmpty()
        val updatedList = _data.value?.posts.orEmpty().filter {
            it.id != id
        }
        _data.value = FeedModel(posts = updatedList, empty = updatedList.isEmpty())
        repository.removeByIdAsync(id, object : PostRepository.RepositoryCallback<Unit> {
            override fun onSuccess(posts: Unit) {

            }

            override fun onError(e: Exception) {

                _data.value = FeedModel(posts = oldList, empty = oldList.isEmpty(),errorCodeMessage = e.message.toString())
            }
        })

    }

}
