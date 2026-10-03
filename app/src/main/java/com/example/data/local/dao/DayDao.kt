package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.BlockOutcomeEntity
import com.example.data.local.entity.DayRecordEntity
import com.example.data.local.entity.JournalEntryEntity
import com.example.data.local.entity.SystemBlockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DayDao {

  // Day Records
  @Query("SELECT * FROM day_records WHERE dateIso = :dateIso")
  fun observeDayRecord(dateIso: String): Flow<DayRecordEntity?>

  @Query("SELECT * FROM day_records WHERE dateIso = :dateIso")
  suspend fun getDayRecord(dateIso: String): DayRecordEntity?

  @Query("SELECT * FROM day_records")
  fun observeAllDayRecords(): Flow<List<DayRecordEntity>>

  @Query("SELECT * FROM day_records")
  suspend fun getAllDayRecords(): List<DayRecordEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertDayRecord(day: DayRecordEntity)

  // Block Outcomes
  @Query("SELECT * FROM block_outcomes WHERE dateIso = :dateIso ORDER BY intendedTime ASC")
  fun observeBlockOutcomes(dateIso: String): Flow<List<BlockOutcomeEntity>>

  @Query("SELECT * FROM block_outcomes WHERE dateIso = :dateIso ORDER BY intendedTime ASC")
  suspend fun getBlockOutcomes(dateIso: String): List<BlockOutcomeEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertBlockOutcome(outcome: BlockOutcomeEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertBlockOutcomes(outcomes: List<BlockOutcomeEntity>)

  // Journal Entries
  @Query("SELECT * FROM journal_entries WHERE dateIso = :dateIso")
  fun observeJournalEntry(dateIso: String): Flow<JournalEntryEntity?>

  @Query("SELECT * FROM journal_entries WHERE dateIso = :dateIso")
  suspend fun getJournalEntry(dateIso: String): JournalEntryEntity?

  @Query("SELECT * FROM journal_entries ORDER BY dateIso DESC")
  fun observeAllJournalEntries(): Flow<List<JournalEntryEntity>>

  @Query("SELECT * FROM journal_entries ORDER BY dateIso DESC")
  suspend fun getAllJournalEntries(): List<JournalEntryEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertJournalEntry(entry: JournalEntryEntity)

  // System Blocks
  @Query("SELECT * FROM system_blocks ORDER BY sortOrder ASC")
  fun observeSystemBlocks(): Flow<List<SystemBlockEntity>>

  @Query("SELECT * FROM system_blocks ORDER BY sortOrder ASC")
  suspend fun getSystemBlocks(): List<SystemBlockEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertSystemBlocks(blocks: List<SystemBlockEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertSystemBlock(block: SystemBlockEntity)

  @Query("DELETE FROM system_blocks WHERE id = :id")
  suspend fun deleteSystemBlock(id: String)
}
