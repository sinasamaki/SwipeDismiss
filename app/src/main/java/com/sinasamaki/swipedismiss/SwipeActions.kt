package com.sinasamaki.swipedismiss

import android.view.MotionEvent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class SwipeActionsConfig(
    val threshold: Float,
    val icon: ImageVector,
    val iconTint: Color,
    val background: Color,
    val stayDismissed: Boolean,
    val onDismiss: () -> Unit,
)

val DefaultSwipeActionsConfig = SwipeActionsConfig(
    threshold = 0.4f,
    icon = Icons.Default.Menu,
    iconTint = Color.Transparent,
    background = Color.Transparent,
    stayDismissed = false,
    onDismiss = {},
)

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class,
    ExperimentalAnimationApi::class)
@Composable
fun SwipeActions(
    modifier: Modifier = Modifier,
    startActionsConfig: SwipeActionsConfig = DefaultSwipeActionsConfig,
    endActionsConfig: SwipeActionsConfig = DefaultSwipeActionsConfig,
    showTutorial: Boolean = false,
    content: @Composable (DismissState) -> Unit,
) = BoxWithConstraints(modifier) {
    val width = constraints.maxWidth.toFloat()
    val height = constraints.maxHeight.toFloat()

    var willDismissDirection: DismissDirection? by remember {
        mutableStateOf(null)
    }

    val state = rememberDismissState(
        confirmStateChange = {
            if (willDismissDirection == DismissDirection.StartToEnd
                && it == DismissValue.DismissedToEnd
            ) {
                startActionsConfig.onDismiss()
                startActionsConfig.stayDismissed
            } else if (willDismissDirection == DismissDirection.EndToStart &&
                it == DismissValue.DismissedToStart
            ) {
                endActionsConfig.onDismiss()
                endActionsConfig.stayDismissed
            } else {
                false
            }
        }
    )

    var showingTutorial by remember {
        mutableStateOf(showTutorial)
    }

    if (showingTutorial) {
        val infiniteTransition = rememberInfiniteTransition()
        val x by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = width * (startActionsConfig.threshold) / 2f,
            animationSpec = infiniteRepeatable(
                animation = tween(500, easing = FastOutSlowInEasing, delayMillis = 1000),
                repeatMode = RepeatMode.Reverse
            )
        )

        LaunchedEffect(key1 = x, block = {
            state.performDrag(x - state.offset.value)
        })
    }

    LaunchedEffect(key1 = Unit, block = {
        snapshotFlow { state.offset.value }
            .collect {
                willDismissDirection = when {
                    it > width * startActionsConfig.threshold -> DismissDirection.StartToEnd
                    it < -width * endActionsConfig.threshold -> DismissDirection.EndToStart
                    else -> null
                }
            }
    })

    val haptic = LocalHapticFeedback.current
    LaunchedEffect(key1 = willDismissDirection, block = {
        if (willDismissDirection != null) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    })

    val dismissDirections by remember(startActionsConfig, endActionsConfig) {
        derivedStateOf {
            mutableSetOf<DismissDirection>().apply {
                if (startActionsConfig != DefaultSwipeActionsConfig) add(DismissDirection.StartToEnd)
                if (endActionsConfig != DefaultSwipeActionsConfig) add(DismissDirection.EndToStart)
            }
        }
    }

    SwipeToDismiss(
        state = state,
        modifier = Modifier
            .pointerInteropFilter {
                if (it.action == MotionEvent.ACTION_DOWN) {
                    showingTutorial = false
                }
                false
            },
        directions = dismissDirections,
        dismissThresholds = {
            if (it == DismissDirection.StartToEnd)
                FractionalThreshold(startActionsConfig.threshold)
            else FractionalThreshold(endActionsConfig.threshold)
        },
        background = {
            AnimatedContent(
                targetState = Pair(state.dismissDirection, willDismissDirection != null),
                transitionSpec = {
                    fadeIn(
                        tween(0),
                        initialAlpha = if (targetState.second) 1f else 0f,
                    ) with fadeOut(
                        tween(0),
                        targetAlpha = if (targetState.second) .7f else 0f,
                    )
                }
            ) { (direction, willDismiss) ->
                val revealSize = remember { Animatable(if (willDismiss) 0f else 1f) }
                val iconSize = remember { Animatable(if (willDismiss) .8f else 1f) }
                LaunchedEffect(key1 = Unit, block = {
                    if (willDismiss) {
                        revealSize.snapTo(0f)
                        launch {
                            revealSize.animateTo(1f, animationSpec = tween(400))
                        }
                        iconSize.snapTo(.8f)
                        iconSize.animateTo(
                            1.45f,
                            spring(
                                dampingRatio = Spring.DampingRatioHighBouncy,
                            )
                        )
                        iconSize.animateTo(
                            1f,
                            spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                            )
                        )
                    }
                })
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CirclePath(
                            revealSize.value,
                            direction == DismissDirection.StartToEnd
                        ))
                        .background(
                            color = when (direction) {
                                DismissDirection.StartToEnd -> if (willDismiss) startActionsConfig.background else startActionsConfig.iconTint
                                DismissDirection.EndToStart -> if (willDismiss) endActionsConfig.background else endActionsConfig.iconTint
                                else -> Color.Transparent
                            },
                        )
                ) {
                    Box(modifier = Modifier
                        .align(
                            when (direction) {
                                DismissDirection.StartToEnd -> Alignment.CenterStart
                                else -> Alignment.CenterEnd
                            }
                        )
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .scale(iconSize.value)
                        .offset { IntOffset(x = 0, y = (10 * (1f - iconSize.value)).roundToInt()) },
                        contentAlignment = Alignment.Center
                    ) {
                        when (direction) {
                            DismissDirection.StartToEnd -> {
                                Image(
                                    painter = rememberVectorPainter(image = startActionsConfig.icon),
                                    colorFilter = ColorFilter.tint(if (willDismiss) startActionsConfig.iconTint else startActionsConfig.background),
                                    contentDescription = null
                                )
                            }
                            DismissDirection.EndToStart -> {
                                Image(
                                    painter = rememberVectorPainter(image = endActionsConfig.icon),
                                    colorFilter = ColorFilter.tint(if (willDismiss) endActionsConfig.iconTint else endActionsConfig.background),
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }
            }
        }
    ) {
        content(state)
    }
}


class CirclePath(private val progress: Float, private val start: Boolean) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {

        val origin = Offset(
            x = if (start) 0f else size.width,
            y = size.center.y,
        )

        val radius = (sqrt(
            size.height * size.height + size.width * size.width
        ) * 1f) * progress

        return Outline.Generic(
            Path().apply {
                addOval(
                    Rect(
                        center = origin,
                        radius = radius,
                    )
                )
            }
        )
    }
}