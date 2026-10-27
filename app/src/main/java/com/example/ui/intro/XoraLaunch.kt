package com.example.ui.intro

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.R
import com.example.ui.theme.*
import kotlinx.coroutines.delay

/** A brief welcome on a fresh launch; returning to the app keeps the current screen. */
@Composable
fun XoraLaunch(
    animationsEnabled: Boolean = systemAnimationsEnabled(LocalContext.current),
    content: @Composable () -> Unit
) {
    var finished by rememberSaveable { mutableStateOf(!animationsEnabled) }

    LaunchedEffect(animationsEnabled) {
        if (!animationsEnabled) finished = true
        else if (!finished) {
            delay(1400)
            finished = true
        }
    }

    AnimatedContent(
        targetState = finished,
        modifier = Modifier.fillMaxSize().background(Forest700),
        transitionSpec = {
            fadeIn(tween(if (animationsEnabled) 200 else 0)) togetherWith
                fadeOut(tween(if (animationsEnabled) 160 else 0))
        },
        label = "Xora opening transition"
    ) { showApp ->
        if (showApp) content() else XoraIntroScreen()
    }
}

@Composable
private fun XoraIntroScreen() {
    val window = LocalContext.current.findActivity()?.window
    DisposableEffect(window) {
        val controller = window?.let { WindowCompat.getInsetsController(it, it.decorView) }
        val previousStatusBar = controller?.isAppearanceLightStatusBars
        val previousNavigationBar = controller?.isAppearanceLightNavigationBars
        controller?.isAppearanceLightStatusBars = false
        controller?.isAppearanceLightNavigationBars = false
        onDispose {
            previousStatusBar?.let { controller?.isAppearanceLightStatusBars = it }
            previousNavigationBar?.let { controller?.isAppearanceLightNavigationBars = it }
        }
    }
    var entered by remember { mutableStateOf(false) }
    val logoProgress by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(650, easing = FastOutSlowInEasing),
        label = "Xora logo reveal"
    )
    val textProgress by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(500, delayMillis = 180, easing = FastOutSlowInEasing),
        label = "Xora wordmark reveal"
    )
    LaunchedEffect(Unit) { entered = true }

    Box(Modifier.fillMaxSize().background(Forest700).safeDrawingPadding().padding(24.dp)) {
        Column(
            modifier = Modifier.align(Alignment.Center).widthIn(max = 480.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(Modifier.size(164.dp), contentAlignment = Alignment.Center) {
                Box(
                    Modifier.fillMaxSize().graphicsLayer {
                        alpha = logoProgress
                        scaleX = 0.8f + logoProgress * 0.2f
                        scaleY = scaleX
                    }.border(1.dp, Color.White.copy(alpha = 0.28f), CircleShape)
                )
                Surface(
                    modifier = Modifier.size(116.dp).graphicsLayer {
                        alpha = logoProgress
                        scaleX = 0.76f + logoProgress * 0.24f
                        scaleY = scaleX
                        rotationZ = -8f * (1f - logoProgress)
                    },
                    color = Forest800,
                    shape = RoundedCornerShape(32.dp),
                    shadowElevation = 6.dp
                ) {
                    Icon(
                        painter = painterResource(R.drawable.xora_mark_white),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.padding(22.dp)
                    )
                }
            }
            Column(
                modifier = Modifier.graphicsLayer {
                    alpha = textProgress
                    translationY = 18.dp.toPx() * (1f - textProgress)
                },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    stringResource(R.string.app_name),
                    color = Color.White,
                    fontSize = 52.sp,
                    lineHeight = 60.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1.5).sp
                )
                Text("Good food. Good mood.", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.82f), textAlign = TextAlign.Center)
            }
        }
    }
}

private fun systemAnimationsEnabled(context: Context): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ValueAnimator.areAnimatorsEnabled()
    else Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
