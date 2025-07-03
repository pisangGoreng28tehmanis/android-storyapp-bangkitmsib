package test.dapuk.dicodingstory.ui.maps

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import test.dapuk.dicodingstory.data.remote.repository.StoryRepository
import test.dapuk.dicodingstory.data.remote.response.ListStoryItem

class MapsViewModel(private val storyRepository: StoryRepository) : ViewModel() {


    private val _listStories = MutableLiveData<List<ListStoryItem>>()
    val listStories: LiveData<List<ListStoryItem>> = _listStories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isErr = MutableLiveData<String>()
    val isErr: LiveData<String> = _isErr

    init {
        getStories()
    }

    fun getStories() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = storyRepository.getStoriesLocation()
                if (response != null) {
                    _listStories.value = response.listStory
                    Log.d("success", "respons ada")
                } else {
                    _isErr.value = "Gagal Fetch Story"
                    Log.e("Failed getStories", "respons null")
                }
            } catch (e: Exception) {
                _isErr.value = "Tidak ada internet"
                Log.e("MainViewmodel", "invaldi")
            }
            _isLoading.value = false
        }
    }
}
