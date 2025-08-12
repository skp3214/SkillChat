package com.skp3214.skillchat.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.skp3214.skillchat.data.ChatMessage
import com.skp3214.skillchat.data.Participant
import com.skp3214.skillchat.utils.parseCodeBlock

@Composable
fun MessageBubble(message: ChatMessage) {
    val alignment = when (message.participant) {
        Participant.USER -> Alignment.CenterEnd
        Participant.AI, Participant.ERROR -> Alignment.CenterStart
    }

    val codeBlock = remember(message.text) { message.text.parseCodeBlock() }

    Box(
        contentAlignment = alignment,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (codeBlock != null && message.participant == Participant.AI) {
            CodeBlock(language = codeBlock.first, code = codeBlock.second)
        } else {
            val backgroundColor = when (message.participant) {
                Participant.USER -> MaterialTheme.colorScheme.primary
                Participant.AI -> MaterialTheme.colorScheme.surface
                Participant.ERROR -> MaterialTheme.colorScheme.error
            }
            val textColor = when (message.participant) {
                Participant.USER -> MaterialTheme.colorScheme.onPrimary
                Participant.AI -> MaterialTheme.colorScheme.onSurface
                Participant.ERROR -> MaterialTheme.colorScheme.onError
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(
                        topStart = if (message.participant == Participant.USER) 20.dp else 4.dp,
                        topEnd = if (message.participant == Participant.USER) 4.dp else 20.dp,
                        bottomStart = 20.dp,
                        bottomEnd = 20.dp
                    ))
                    .background(backgroundColor)
                    .padding(16.dp)
            ) {
                if (message.isLoading) {
                    TypingIndicator()
                } else {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                        exit = fadeOut(animationSpec = tween(durationMillis = 300))
                    ) {
                        Text(
                            text = message.text,
                            color = textColor,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}
