package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
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
  fun `viewmodel updates speed request`() {
    val viewModel = DikshaSpeedViewModel()
    viewModel.requestSpeed(10.0)
    assertEquals(10.0, viewModel.uiState.value.requestedSpeed, 0.001)
  }

  @Test
  fun `viewmodel parses successful 10x speed response`() {
    val viewModel = DikshaSpeedViewModel()
    val testJson = """{"success":true,"requested":10.0,"actual":10.0,"videoCount":1,"isPlaying":true}"""
    viewModel.handleSpeedBridgeResult(testJson)

    assertEquals(10.0, viewModel.uiState.value.requestedSpeed, 0.001)
    assertEquals(10.0, viewModel.uiState.value.actualSpeed, 0.001)
    assertEquals(SpeedStatus.ACTIVE, viewModel.uiState.value.status)
    assertTrue(viewModel.uiState.value.isActive)
  }

  @Test
  fun `viewmodel handles no video response`() {
    val viewModel = DikshaSpeedViewModel()
    val testJson = """{"success":false,"reason":"NO_VIDEO","requested":10.0,"actual":1.0,"videoCount":0}"""
    viewModel.handleSpeedBridgeResult(testJson)

    assertEquals(SpeedStatus.NO_VIDEO, viewModel.uiState.value.status)
  }

  @Test
  fun `viewmodel handles cross origin iframe response`() {
    val viewModel = DikshaSpeedViewModel()
    val testJson = """{"success":false,"reason":"CROSS_ORIGIN_IFRAME","requested":10.0,"actual":1.0,"videoCount":0}"""
    viewModel.handleSpeedBridgeResult(testJson)

    assertEquals(SpeedStatus.CROSS_ORIGIN_IFRAME, viewModel.uiState.value.status)
    assertEquals("Video is inside a cross-origin iframe.", viewModel.uiState.value.statusMessage)
  }
}
