package test.dapuk.dicodingstory.data.local.room

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy
import test.dapuk.dicodingstory.data.remote.response.ListStoryItem
@Dao
interface StoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: List<ListStoryItemLocal>)

    @Query("SELECT * FROM story")
    fun getAllStory(): PagingSource<Int, ListStoryItemLocal>

    @Query("DELETE FROM story")
    suspend fun deleteAll()
}