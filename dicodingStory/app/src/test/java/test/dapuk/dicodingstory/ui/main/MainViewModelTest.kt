package test.dapuk.dicodingstory.ui.main

import MainDispatcherRule
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import test.dapuk.dicodingstory.data.remote.repository.StoryRepository
import test.dapuk.dicodingstory.data.remote.response.ListStoryItem
import test.dapuk.dicodingstory.DataDummy
import test.dapuk.dicodingstory.getOrAwaitValue
import kotlinx.coroutines.test.runTest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.ListUpdateCallback
import test.dapuk.dicodingstory.data.adapter.ListStoriesAdapter
import kotlinx.coroutines.Dispatchers
import org.junit.Assert
import test.dapuk.dicodingstory.data.local.room.ListStoryItemLocal

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest{
@get:Rule
val instantExecutorRule = InstantTaskExecutorRule()
@get:Rule
val mainDispatcherRules = MainDispatcherRule()
@Mock
private lateinit var storyRepository: StoryRepository
    @Test
    fun `when Get Story Should Not Null and Return Data`() = runTest {
        val dummyQuote = DataDummy.generateDummyQuoteResponse()
        val data: PagingData<ListStoryItemLocal> = QuotePagingSource.snapshot(dummyQuote)
        val expectedQuote = MutableLiveData<PagingData<ListStoryItemLocal>>()
        expectedQuote.value = data
        Mockito.`when`(storyRepository.getStoriesPaging()).thenReturn(expectedQuote)

        val mainViewModel = MainViewModel(storyRepository)
        val actualQuote: PagingData<ListStoryItemLocal> = mainViewModel.story.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = ListStoriesAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualQuote)

        Assert.assertNotNull(differ.snapshot())
        Assert.assertEquals(dummyQuote.size, differ.snapshot().size)
        Assert.assertEquals(dummyQuote[0], differ.snapshot()[0])
    }

    @Test
    fun `when Get Story Empty Should Return No Data`() = runTest {
        val data: PagingData<ListStoryItemLocal> = PagingData.from(emptyList())
        val expectedQuote = MutableLiveData<PagingData<ListStoryItemLocal>>()
        expectedQuote.value = data
        Mockito.`when`(storyRepository.getStoriesPaging()).thenReturn(expectedQuote)
        val mainViewModel = MainViewModel(storyRepository)
        val actualQuote: PagingData<ListStoryItemLocal> = mainViewModel.story.getOrAwaitValue()
        val differ = AsyncPagingDataDiffer(
            diffCallback = ListStoriesAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualQuote)
        Assert.assertEquals(0, differ.snapshot().size)
    }
}

class QuotePagingSource : PagingSource<Int, LiveData<List<ListStoryItemLocal>>>() {
    companion object {
        fun snapshot(items: List<ListStoryItemLocal>): PagingData<ListStoryItemLocal> {
            return PagingData.from(items)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, LiveData<List<ListStoryItemLocal>>>): Int {
        return 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LiveData<List<ListStoryItemLocal>>> {
        return LoadResult.Page(emptyList(), 0, 1)
    }
}

val noopListUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}