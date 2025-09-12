package com.wes.truthdare.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

/**
 * An animated counter component that animates between number changes
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedCounter(
    count: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current
) {
    Row(modifier = modifier) {
        val countString = count.toString()
        
        // Animate each digit individually
        for (i in countString.indices) {
            val digit = countString[i]
            
            AnimatedContent(
                targetState = digit,
                transitionSpec = {
                    // Slide up when incrementing, slide down when decrementing
                    if (targetState.code > initialState.code) {
                        slideInVertically(animationSpec = tween(durationMillis = 300)) { height -> height } + 
                        fadeIn(animationSpec = tween(durationMillis = 300)) togetherWith
                        slideOutVertically(animationSpec = tween(durationMillis = 300)) { height -> -height } + 
                        fadeOut(animationSpec = tween(durationMillis = 300))
                    } else {
                        slideInVertically(animationSpec = tween(durationMillis = 300)) { height -> -height } + 
                        fadeIn(animationSpec = tween(durationMillis = 300)) togetherWith
                        slideOutVertically(animationSpec = tween(durationMillis = 300)) { height -> height } + 
                        fadeOut(animationSpec = tween(durationMillis = 300))
                    }.using(
                        SizeTransform(clip = false)
                    )
                }
            ) { targetDigit ->
                Text(
                    text = targetDigit.toString(),
                    style = style
                )
            }
        }
    }
}