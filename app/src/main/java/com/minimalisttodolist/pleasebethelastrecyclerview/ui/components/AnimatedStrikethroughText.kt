package com.minimalisttodolist.pleasebethelastrecyclerview.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration

// https://medium.com/@kappdev/animated-strikethrough-text-in-jetpack-compose-2350d0f105af

@Composable
fun AnimatedStrikethroughText(
    text: String,
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
    animateOnHide: Boolean = true,
    spec: AnimationSpec<Int> = tween(text.length * 30, easing = FastOutLinearInEasing),
    strikethroughStyle: SpanStyle = SpanStyle(),
    textStyle: TextStyle = LocalTextStyle.current
) {
    var textToDisplay by remember { mutableStateOf(AnnotatedString("")) }

    val length = remember { Animatable(initialValue = 0, typeConverter = Int.VectorConverter) }

    LaunchedEffect(length.value) {
        textToDisplay = text.buildStrikethrough(length.value, strikethroughStyle)
    }

    LaunchedEffect(isVisible) {
        when {
            isVisible -> length.animateTo(text.length, spec)
            !isVisible && animateOnHide -> length.animateTo(0, spec)
            else -> length.snapTo(0)
        }
    }

    LaunchedEffect(text) {
        when {
            isVisible && text.length == length.value -> {
                textToDisplay = text.buildStrikethrough(length.value, strikethroughStyle)
            }
            isVisible && text.length != length.value -> {
                length.snapTo(text.length)
            }
            else -> textToDisplay = AnnotatedString(text)
        }
    }

    Text(
        text = textToDisplay,
        modifier = modifier,
        style = textStyle
    )
}

private fun String.buildStrikethrough(length: Int, style: SpanStyle) = buildAnnotatedString {
    append(this@buildStrikethrough)
    val lineThroughStyle = style.copy(textDecoration = TextDecoration.LineThrough)
    addStyle(lineThroughStyle, 0, length)
}