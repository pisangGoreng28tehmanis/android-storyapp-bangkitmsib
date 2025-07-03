package test.dapuk.dicodingstory.data.remote.repository

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import test.dapuk.dicodingstory.data.local.room.ListStoryItemLocal
import test.dapuk.dicodingstory.data.local.room.StoryDatabase
import test.dapuk.dicodingstory.data.remote.StoryRemoteMediator
import test.dapuk.dicodingstory.data.remote.paging.storyPagingSource
import test.dapuk.dicodingstory.data.remote.response.ListStoryItem
import test.dapuk.dicodingstory.data.remote.response.RegisterResponse
import test.dapuk.dicodingstory.data.remote.response.StoryDetailResponse
import test.dapuk.dicodingstory.data.remote.response.StoryResponse
import test.dapuk.dicodingstory.data.remote.retrofit.ApiService

class StoryRepository(
    private val apiServide: ApiService,
    private val sharedPref: SharedPreferences,
    private val storyDatabase: StoryDatabase
) {

    suspend fun getStories(): StoryResponse? {
        val token = sharedPref.getString("token", null)
        if (token != null) {
            return apiServide.getStories("Bearer $token")
        } else {
            Log.e("StoryRepo", "tokennya null cok")
            return null
        }
    }


  fun getStoriesPaging() : LiveData<PagingData<ListStoryItemLocal>>{
      @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = StoryRemoteMediator(storyDatabase, apiServide, sharedPref),
            pagingSourceFactory = {
                storyDatabase.storyDao().getAllStory()
            }
        ).liveData
    }



    suspend fun getStoriesLocation(): StoryResponse? {
        val token = sharedPref.getString("token", null)
        if (token != null) {
            return apiServide.getStoriesLocation("Bearer $token")
        } else {
            Log.e("StoryRepo", "tokennya null cok")
            return null
        }
    }

    suspend fun getStoriesDetail(id: String): StoryDetailResponse? {
        val token = sharedPref.getString("token", null)
        if (token != null) {
            return apiServide.getStoriesDetail("Bearer $token", id)
        } else {
            Log.e("StoryRepo", "tokennya null cok")
            return null
        }
    }

    suspend fun addStory(file: MultipartBody.Part, description: RequestBody): RegisterResponse? {
        val token = sharedPref.getString("token", null)
        if (token != null) {
            return apiServide.addStory("Bearer $token", file, description)
        } else {
            Log.e("StoryRepo", "tokennya null cok")
            return null
        }
    }
    suspend fun addStoryLocation(file: MultipartBody.Part, description: RequestBody, lat: Double, lon: Double): RegisterResponse? {
        val token = sharedPref.getString("token", null)
        if (token != null) {
            return apiServide.addStoryLocation("Bearer $token", file, description, lat, lon)
        } else {
            Log.e("StoryRepo", "tokennya null cok")
            return null
        }
    }
}