@file:OptIn(ExperimentalMaterialApi::class)

package com.sinasamaki.swipedismiss

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue

@Composable
fun App() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "E-mail")
                },
            )
        }
    ) {

        val emailsList = remember {
            mutableStateListOf<Email>().apply {
                emails.forEach { email ->
                    add(email)
                }
            }
        }

        val animatedList by updateAnimatedItemsState(newList = emailsList.map { it })

        LazyColumn(
            modifier = Modifier
                .background(color = Color.Black)
                .padding(it)
        ) {
            animatedItemsIndexed(
                state = animatedList,
                key = { email ->
                    email.subject
                }
            ) { index, email ->
                SwipeActions(
                    startActionsConfig = SwipeActionsConfig(
                        threshold = 0.4f,
                        background = Color(0xFFECEC2B),
                        iconTint = Color.Black,
                        icon = Icons.Default.Star,
                        stayDismissed = false,
                        onDismiss = {

                        }
                    ),
                    endActionsConfig = SwipeActionsConfig(
                        threshold = 0.4f,
                        background = Color(0xffFF4444),
                        iconTint = Color.Black,
                        icon = Icons.Default.Delete,
                        stayDismissed = true,
                        onDismiss = {
                            emailsList.removeIf {
                                it.subject == email.subject
                            }
                        }
                    ),
                    showTutorial = index == 0
                ) { state ->
                    val animateCorners by remember {
                        derivedStateOf {
                            state.offset.value.absoluteValue > 30
                        }
                    }
                    val startCorners by animateDpAsState(
                        targetValue = when {
                            state.dismissDirection == DismissDirection.StartToEnd &&
                                    animateCorners -> 8.dp
                            else -> 0.dp
                        }
                    )
                    val endCorners by animateDpAsState(
                        targetValue = when {
                            state.dismissDirection == DismissDirection.EndToStart &&
                                    animateCorners -> 8.dp
                            else -> 0.dp
                        }
                    )
                    val elevation by animateDpAsState(
                        targetValue = when {
                            animateCorners -> 6.dp
                            else -> 2.dp
                        }
                    )
                    Card(
                        shape = RoundedCornerShape(
                            topStart = startCorners,
                            bottomStart = startCorners,
                            topEnd = endCorners,
                            bottomEnd = endCorners,
                        ),
                        elevation = elevation,
                    ) {
                        ListItem(title = email.subject, subtitle = email.text)
                    }
                }
            }
        }
    }
}
