package test.dapuk.dicodingstory.data.remote.paging

import android.content.SharedPreferences
import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import test.dapuk.dicodingstory.data.remote.response.ListStoryItem
import test.dapuk.dicodingstory.data.remote.retrofit.ApiService

class storyPagingSource(private val apiService: ApiService, private val sharedPref: SharedPreferences) : PagingSource<Int, ListStoryItem>() {

    override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {
        return try {
            val token = sharedPref.getString("token", null)
            val position = params.key ?: INITIAL_PAGE_INDEX
            val responseData = apiService.getStoriesPaging("Bearer $token", position, params.loadSize)
            val response = responseData.listStory
            LoadResult.Page(
                data = response,
                prevKey = if (position == INITIAL_PAGE_INDEX) null else position - 1,
                nextKey = if (response.isNullOrEmpty()) null else position + 1
            )
        } catch (exception: Exception) {
            return LoadResult.Error(exception)
        }
    }


    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }
}