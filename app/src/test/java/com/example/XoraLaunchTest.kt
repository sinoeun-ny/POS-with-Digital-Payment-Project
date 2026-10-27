package com.example

import androidx.compose.material3.Text
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.ui.intro.XoraLaunch
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w360dp-h800dp-xhdpi")
class XoraLaunchTest {
    @get:Rule val compose = createComposeRule()

    @Test fun introFinishesAndDoesNotReplayAfterRestoration() {
        compose.mainClock.autoAdvance = false
        val restoration = StateRestorationTester(compose)
        restoration.setContent { MyApplicationTheme { XoraLaunch(animationsEnabled = true) { Text("Discover is ready") } } }
        compose.mainClock.advanceTimeBy(800)
        compose.onNodeWithText("Xora").assertIsDisplayed()
        compose.onNodeWithText("Skip intro").assertDoesNotExist()
        compose.onNodeWithText("Discover is ready").assertDoesNotExist()
        File("build/ui-previews").mkdirs()
        compose.onRoot().captureRoboImage(filePath = "build/ui-previews/xora-intro.png")
        compose.mainClock.advanceTimeBy(1400)
        compose.onNodeWithText("Discover is ready").assertIsDisplayed()
        compose.onNodeWithText("Skip intro").assertDoesNotExist()
        restoration.emulateSavedInstanceStateRestore()
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithText("Discover is ready").assertIsDisplayed()
        compose.onNodeWithText("Skip intro").assertDoesNotExist()
    }

    @Test fun disabledSystemAnimationsSkipIntro() {
        compose.setContent { MyApplicationTheme { XoraLaunch(animationsEnabled = false) { Text("Discover is ready") } } }
        compose.onNodeWithText("Discover is ready").assertIsDisplayed()
        compose.onNodeWithText("Skip intro").assertDoesNotExist()
    }
}
