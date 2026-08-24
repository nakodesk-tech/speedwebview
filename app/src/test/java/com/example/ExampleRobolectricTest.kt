package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.engine2.model.SunbirdSpeedStatus
import com.example.engine2.viewmodel.SunbirdEngineViewModel
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
  fun `engine1 viewmodel updates speed request`() {
    val viewModel = DikshaSpeedViewModel()
    viewModel.requestSpeed(10.0)
    assertEquals(10.0, viewModel.uiState.value.requestedSpeed, 0.001)
  }

  @Test
  fun `engine1 viewmodel parses successful 10x speed response`() {
    val viewModel = DikshaSpeedViewModel()
    val testJson = """{"success":true,"requested":10.0,"actual":10.0,"videoCount":1,"isPlaying":true}"""
    viewModel.handleSpeedBridgeResult(testJson)

    assertEquals(10.0, viewModel.uiState.value.requestedSpeed, 0.001)
    assertEquals(10.0, viewModel.uiState.value.actualSpeed, 0.001)
    assertEquals(SpeedStatus.ACTIVE, viewModel.uiState.value.status)
    assertTrue(viewModel.uiState.value.isActive)
  }

  @Test
  fun `engine2 sunbird viewmodel requests speed 1x 2x 5x 10x`() {
    val viewModel = SunbirdEngineViewModel()
    
    viewModel.requestSpeed(1.0)
    assertEquals(1.0, viewModel.uiState.value.requestedSpeed, 0.001)

    viewModel.requestSpeed(2.0)
    assertEquals(2.0, viewModel.uiState.value.requestedSpeed, 0.001)

    viewModel.requestSpeed(5.0)
    assertEquals(5.0, viewModel.uiState.value.requestedSpeed, 0.001)

    viewModel.requestSpeed(10.0)
    assertEquals(10.0, viewModel.uiState.value.requestedSpeed, 0.001)
  }

  @Test
  fun `engine2 sunbird viewmodel parses 10x speed verification successfully`() {
    val viewModel = SunbirdEngineViewModel()
    val speedJson = """{"requested":10.0,"actual":10.0,"success":true,"method":"Direct HTML5 <video>.playbackRate","videoId":"video-player_html5_api"}"""
    
    viewModel.handleSpeedVerification(speedJson)

    assertEquals(10.0, viewModel.uiState.value.requestedSpeed, 0.001)
    assertEquals(10.0, viewModel.uiState.value.actualSpeed, 0.001)
    assertEquals(SunbirdSpeedStatus.SUCCESS, viewModel.uiState.value.status)
    assertEquals("Requested: 10x | Actual: 10x | Status: SUCCESS", viewModel.uiState.value.statusMessage)
    assertEquals("video-player_html5_api", viewModel.uiState.value.diagnostics.videoElementId)
  }

  @Test
  fun `engine2 sunbird viewmodel parses diagnostics json`() {
    val viewModel = SunbirdEngineViewModel()
    val diagJson = """{"customElementRegistered":true,"playerElementFound":true,"underlyingVideoFound":true,"videoElementId":"video-player_html5_api","videoJsInstanceFound":true,"videoJsPlayerId":"video-player","isPlaying":true,"currentTime":12.5,"duration":600.0,"requestedSpeed":5.0,"actualPlaybackRate":5.0,"accessMethodUsed":"Video.js API + DOM"}"""
    
    viewModel.handleDiagnostics(diagJson)

    assertTrue(viewModel.uiState.value.diagnostics.customElementRegistered)
    assertTrue(viewModel.uiState.value.diagnostics.underlyingVideoFound)
    assertTrue(viewModel.uiState.value.diagnostics.videoJsInstanceFound)
    assertEquals(5.0, viewModel.uiState.value.actualSpeed, 0.001)
  }

  @Test
  fun `engine2 sunbird handles player and telemetry events`() {
    val viewModel = SunbirdEngineViewModel()
    viewModel.handlePlayerEvent("""{"event":"PLAY","detail":{"time":123}}""")
    viewModel.handleTelemetryEvent("""{"eid":"INTERACT","ets":1710000000}""")

    assertEquals(1, viewModel.uiState.value.eventLogs.size)
    assertEquals(1, viewModel.uiState.value.telemetryLogs.size)
  }
}
