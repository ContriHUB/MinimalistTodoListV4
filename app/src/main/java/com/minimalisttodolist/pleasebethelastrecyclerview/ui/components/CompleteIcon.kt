package com.minimalisttodolist.pleasebethelastrecyclerview.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.minimalisttodolist.pleasebethelastrecyclerview.util.vibrate
import kotlinx.coroutines.launch

@Composable
fun CompleteIcon(modifier: Modifier = Modifier, onDelete: () -> Unit) {
    var isChecked by remember { mutableStateOf(false) }
    val scale = remember { Animatable(initialValue = 1f) }
    var selected by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(key1 = selected) {
        if(selected) {
            launch {
                scale.animateTo(
                    targetValue = 0.6f,
                    animationSpec = tween(
                        durationMillis = 50
                    )
                )
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
                isChecked = false
                selected = false
            }
        }
    }

    Box(
        modifier = modifier
//            .background(Color.Red)
            .scale(scale.value)
            .clickable {
                selected = !selected
                isChecked = !isChecked
                vibrate(context = context, strength = 1)
                onDelete()
            }
            .padding(6.dp)
    ) {
        AnimatedContent(
            targetState = isChecked,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300))
            },
            label = "Complete Task Icon"
        ) { targetChecked ->
            if (targetChecked) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = "Checked",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = "Unchecked",
                    tint = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun CompleteIconWithoutDelay(isChecked: Boolean) {
    Box(
        modifier = Modifier
            .padding(8.dp) // Increase padding for a larger touch area
    ) {
        if (isChecked) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = "Checked",
                tint = MaterialTheme.colorScheme.primary,
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.RadioButtonUnchecked,
                contentDescription = "Unchecked",
                tint = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}
