package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.PRESET_SPEEDS
import com.example.model.SpeedStatus
import com.example.viewmodel.DikshaSpeedViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("DIKSHA Speed", appName)
  }

  @Test
  fun `preset speeds list contains all required speeds`() {
    val requiredSpeeds = listOf(1.0, 1.25, 1.5, 1.75, 2.0, 2.5, 3.0, 4.0, 5.0, 7.5, 10.0)
    for (speed in requiredSpeeds) {
      assertTrue("Speed $speed should be in preset speeds list", PRESET_SPEEDS.contains(speed))
    }
  }

  @Test
  fun `diksha viewmodel updates speed request`() {
    val viewModel = DikshaSpeedViewModel()
    viewModel.requestSpeed(10.0)
    assertEquals(10.0, viewModel.uiState.value.requestedSpeed, 0.001)
  }

  @Test
  fun `diksha viewmodel parses successful 10x speed response`() {
    val viewModel = DikshaSpeedViewModel()
    val testJson = """{"success":true,"requested":10.0,"actual":10.0,"videoCount":1,"isPlaying":true}"""
    viewModel.handleSpeedBridgeResult(testJson)

    assertEquals(10.0, viewModel.uiState.value.requestedSpeed, 0.001)
    assertEquals(10.0, viewModel.uiState.value.actualSpeed, 0.001)
    assertEquals(SpeedStatus.ACTIVE, viewModel.uiState.value.status)
    assertTrue(viewModel.uiState.value.isActive)
  }

  @Test
  fun `diksha viewmodel parses 5x and 7_5x speed responses`() {
    val viewModel = DikshaSpeedViewModel()
    
    val test5x = """{"success":true,"requested":5.0,"actual":5.0,"videoCount":1,"isPlaying":true}"""
    viewModel.handleSpeedBridgeResult(test5x)
    assertEquals(5.0, viewModel.uiState.value.actualSpeed, 0.001)
    assertTrue(viewModel.uiState.value.isActive)

    val test7_5x = """{"success":true,"requested":7.5,"actual":7.5,"videoCount":1,"isPlaying":true}"""
    viewModel.handleSpeedBridgeResult(test7_5x)
    assertEquals(7.5, viewModel.uiState.value.actualSpeed, 0.001)
    assertTrue(viewModel.uiState.value.isActive)
  }

  @Test
  fun `diksha viewmodel handles no video detected`() {
    val viewModel = DikshaSpeedViewModel()
    val testNoVideo = """{"success":false,"requested":2.0,"actual":1.0,"videoCount":0,"reason":"NO_VIDEO"}"""
    viewModel.handleSpeedBridgeResult(testNoVideo)

    assertEquals(SpeedStatus.NO_VIDEO, viewModel.uiState.value.status)
  }
}
