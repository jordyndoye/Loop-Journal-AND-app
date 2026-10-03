package com.example

import com.example.data.service.InsightsDraftService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class InsightsDraftServiceTest {

  @Test
  fun testOfflineOrMissingKeyDoesNotCrash() = runBlocking {
    // If call fails, times out, or the phone is offline, keep the last note and do not crash
    val result = InsightsDraftService.generateSilentDraft(emptyList())
    // With empty list or default test environment without network, returns null safely
    assertNull(result)
  }
}
