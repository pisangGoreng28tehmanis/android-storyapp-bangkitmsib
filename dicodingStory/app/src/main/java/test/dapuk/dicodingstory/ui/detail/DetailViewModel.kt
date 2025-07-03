package test.dapuk.dicodingstory.ui.detail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import test.dapuk.dicodingstory.data.remote.repository.StoryRepository
import test.dapuk.dicodingstory.data.remote.response.Story

class DetailViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    private val _detailStory = MutableLiveData<Story>()
    val detailStory: LiveData<Story> = _detailStory
    private val _eventDetailId = MutableLiveData<String>()
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _isSuccess = MutableLiveData<Boolean>()
    val isSuccess: LiveData<Boolean> = _isSuccess

    private val _isErr = MutableLiveData<String>()
    val isErr: LiveData<String> = _isErr


    fun setDetailId(id: String) {
        _eventDetailId.value = id
    }

    init {
        _eventDetailId.observeForever { id ->
            setStoryDetail(id)
        }
    }

    fun setStoryDetail(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _isSuccess.value = false
            try {
                val response = storyRepository.getStoriesDetail(id)
                if (response != null) {
                    _detailStory.value = response.story
                    _isSuccess.value = true
                } else {
                    _isErr.value = "Gagal Fetch Story"
                    Log.e("Failed getStories", "respons null")
                }
            } catch (e: Exception) {
                _isErr.value = "No internet"
                Log.e("MainViewmodel", "invaldi")
            }
        }
        _isLoading.value = false
    }

    override fun onCleared() {
        super.onCleared()
        _eventDetailId.removeObserver { id -> setStoryDetail(id) }
    }
}