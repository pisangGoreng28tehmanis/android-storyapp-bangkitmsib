package test.dapuk.dicodingstory.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.launch
import test.dapuk.dicodingstory.data.local.room.ListStoryItemLocal
import test.dapuk.dicodingstory.data.remote.repository.StoryRepository
import test.dapuk.dicodingstory.data.remote.response.ListStoryItem
import test.dapuk.dicodingstory.data.remote.response.StoryResponse

class MainViewModel(private val storyRepository: StoryRepository) : ViewModel() {

    private val _listStories = MutableLiveData<List<ListStoryItem>>()
    val listStories: LiveData<List<ListStoryItem>> = _listStories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isErr = MutableLiveData<String>()
    val isErr: LiveData<String> = _isErr

    var story: LiveData<PagingData<ListStoryItemLocal>> = storyRepository.getStoriesPaging().cachedIn(viewModelScope)
}