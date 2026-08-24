package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.model.SpeedStatus
import com.example.model.SpeedUiState
import com.example.ui.components.SpeedControlPanel
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun speed_panel_screenshot() {
    val sampleState = SpeedUiState(
      requestedSpeed = 10.0,
      actualSpeed = 10.0,
      status = SpeedStatus.ACTIVE,
      statusMessage = "Speed: 10x • Status: ACTIVE",
      videoCount = 1,
      isVideoPlaying = true
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        SpeedControlPanel(
          uiState = sampleState,
          onSpeedSelected = {},
          onRefreshDiagnostics = {},
          onOpenDiagnostics = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
