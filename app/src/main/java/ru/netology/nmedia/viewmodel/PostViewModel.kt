package ru.netology.nmedia.viewmodel


import android.net.Uri
import androidx.lifecycle.*
import androidx.lifecycle.switchMap
import androidx.paging.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.*
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.RetryTypes
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.random.Random


private val empty = Post(
    id = 0,
    authorId = 0,
    content = "",
    author = "",
    authorAvatar = "",
    likedByMe = false,
    likes = 0,
    published = LocalDateTime.now(),
    ownedByMe = false,
)

private val today = LocalDateTime.now()

private val yesterday = today.minusDays(1)
//private val yesterday = today.minusSeconds(2)


private val weekAgo = today.minusDays(2)
//private val weekAgo = today.minusSeconds(4)

fun Post?.isToday(): Boolean {
    if (this == null) return false
    return published > yesterday

}

fun Post?.isYesterday(): Boolean {
    if (this == null) return false
    return today.year == published.year && published.dayOfYear == yesterday.dayOfYear
    //return published.second == yesterday.second

}

fun Post?.isWeekAgo(): Boolean {
    if (this == null) return false
    return published < weekAgo
    //return published.second == weekAgo.second
}

@HiltViewModel
@ExperimentalCoroutinesApi
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    auth: AppAuth
) : ViewModel() {
    //    private val dateseparatorterminal: TerminalSeparatorType =
//        TerminalSeparatorType()
    private val cached: Flow<PagingData<FeedItem>> = repository
        .data
        .map { pagingData ->
            pagingData.insertSeparators(
                terminalSeparatorType = TerminalSeparatorType.SOURCE_COMPLETE,
                //generator = ::insertDateSeparators
                generator = ::insertFeedSeparators
            )
//            pagingData.insertSeparators(
//                generator = { before, after ->
//                    if (before?.id?.rem(5) != 0L) null else
//                        Ad(Random.nextLong(),
//                        "figma.jpg")
//                }
//            )
        }
        .cachedIn(viewModelScope)

    //    private val cached: Flow<PagingData<FeedItem>> = repository
//        .data
//        .map {
//        pagingData: PagingData<Post> ->
//        pagingData.insertSeparators(
//            generator = { before: Post?, after: Post? ->
//                if (before == null && before.isToday()) {
//                        DateSeparator(DateSeparator.Type.TODAY)
//                    } else if (before == null || (before.isToday() && after.isYesterday())) {
//                    DateSeparator(DateSeparator.Type.YESTERDAY)
//                } else if ((before.isYesterday() && after.isWeekAgo())) {
//                    DateSeparator(DateSeparator.Type.WEEK_AGO)
//                } else {
//                    null
//                }
//            }
//        )
//    }
//    private fun insertDateSeparators(before: Post?, after: Post?): DateSeparator? {
//        return when {
//            before == null && after.isToday() -> {
//                DateSeparator(DateSeparator.Type.TODAY)
//            }
//
//            (before == null && after.isYesterday()) || (before.isToday() && after.isYesterday()) -> {
//                DateSeparator(DateSeparator.Type.YESTERDAY)
//            }
//
//            before.isYesterday() && after.isWeekAgo() -> {
//                DateSeparator(DateSeparator.Type.WEEK_AGO)
//            }
//
//
//            else -> {
//                null
//            }
//        }
//    }

    private fun insertFeedSeparators(before: Post?, after: Post?): FeedItem? {
        return when {



            before == null && after.isToday() -> {
                DateSeparator(DateSeparator.Type.TODAY)
            }

            (before == null && after.isYesterday()) || (before.isToday() && after.isYesterday()) -> {
                DateSeparator(DateSeparator.Type.YESTERDAY)
            }


            before.isYesterday() && after.isWeekAgo() -> {
                DateSeparator(DateSeparator.Type.WEEK_AGO)
            }
            (before?.id?.rem(5) == 0L) -> {
                Ad(Random.nextLong(), "figma.jpg")

            }




            else -> {
                null
            }
        }
    }


    //    val data: Flow<PagingData<FeedItem>> = auth.state.flatMapLatest { token ->
//        repository.data
//            .map { posts ->
//                posts.map { post ->
//                    if (post is Post) {
//                        post.copy(ownedByMe = post.authorId == token?.id)
//                    } else {
//                        post
//                    }
//                }
//            }
//    }.flowOn(Dispatchers.Default)
//    val data: Flow<PagingData<FeedItem>> = auth.state
//        .flatMapLatest { (myId, _) ->
//            cached
//                .map { pagingData ->
//                pagingData.map { item ->
//                    if (item !is Post) item else item.copy(ownedByMe = item.authorId == myId)
//                }
//            }
//        }

    // переписал из лекции
    val data: Flow<PagingData<FeedItem>> = auth
        .state
        .flatMapLatest { value: Token? ->
            cached
                .map { pagingData ->
                    pagingData.map { item ->
                        if (item !is Post) item else item.copy(ownedByMe = item.authorId == value?.id)
                    }
                }
        }

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState


    // пришлось создать для работы newCount
    val dataForNewCount: LiveData<FeedModel> = auth.state.flatMapLatest { token ->
        repository.dataForNewCountPosts
            .map { posts ->
                FeedModel(
                    posts.map { it.copy(ownedByMe = it.authorId == token?.id) },
                    posts.isEmpty()
                )
            }
    }.asLiveData(Dispatchers.Default)

    // не получилось переписать, нужна помощь
    val newCount: LiveData<Int> = dataForNewCount.switchMap {
        //it.map { repository.getNewerCount(it.id) }
        repository.getNewerCount(it.posts.firstOrNull()?.id ?: 0L)
            .catch { e -> e.printStackTrace() }
            .asLiveData(Dispatchers.Default)
    }

    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _photo = MutableLiveData<PhotoModel?>(null)
    val photo: LiveData<PhotoModel?>
        get() = _photo

    init {
        loadPosts()
    }


    fun clearPhoto() {
        _photo.value = null
    }

    fun loadPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            //repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }

    }

    fun clearPostRemoteKeyDao() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.cleanPostRemoteKeyDao()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }


    // больше эта функция не нужна v
//    fun refreshPosts() = viewModelScope.launch {
//        try {
//            _dataState.value = FeedModelState(refreshing = true)
//            repository.getAllUnhide()
//            repository.getAll()
//            _dataState.value = FeedModelState()
//        } catch (e: Exception) {
//            _dataState.value = FeedModelState(error = true)
//        }
//    }

    fun save() {
        edited.value?.let {
            _postCreated.value = Unit
            viewModelScope.launch {
                try {
                    when (_photo.value) {
                        null -> repository.save(it)
                        else -> _photo.value?.file?.let { file ->
                            repository.saveWithAttachment(it, MediaUpload(file))
                        }
                    }
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value =
                        FeedModelState(
                            error = true,
                            retryType = RetryTypes.SAVE,
                            retryPost = it
                        )
                }
            }
        }
        edited.value = empty
    }

    fun retrySave(post: Post?) {
        viewModelScope.launch {
            try {
                if (post != null) {
                    repository.save(post)
                    _dataState.value = FeedModelState()
                }

            } catch (e: Exception) {
                _dataState.value =
                    FeedModelState(
                        error = true,
                        retryType = RetryTypes.SAVE,
                        retryPost = post
                    )
            }
        }
    }

    fun removeById(id: Long) {

        viewModelScope.launch {
            try {
                repository.removeById(id)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value =
                    FeedModelState(
                        error = true,
                        retryId = id,
                        retryType = RetryTypes.REMOVE
                    )

            }

        }


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

        viewModelScope.launch {
            try {
                repository.likeById(id)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value =
                    FeedModelState(error = true, retryType = RetryTypes.LIKE, retryId = id)
            }

        }

    }

    fun unlikeByID(id: Long) {
        viewModelScope.launch {
            try {
                repository.unlikeByID(id)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value =
                    FeedModelState(
                        error = true,
                        retryType = RetryTypes.UNLIKE,
                        retryId = id
                    )
            }

        }
    }

    fun getAllUnhide() {
        viewModelScope.launch {
            try {
                repository.getAllUnhide()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }

    }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }


}
