package com.example.trueline_listener.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

data class ChatDisplayItem(
    val userId: String,
    val avatarText: String,
    val callerTitle: String,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int = 0,
    val isRegular: Boolean = false,
    val lastMessageSender: String = "",
    val filterType: ChatFilter
)

@Composable
fun ListenerChatScreen(viewModel: MainPortalViewModel) {
    val scrollState = rememberScrollState()
    val filterScrollState = rememberScrollState()
    val currentFilter = viewModel.selectedChatFilter
    val focusManager = LocalFocusManager.current

    // Periodically refresh conversation list in background
    LaunchedEffect(Unit) {
        viewModel.fetchConversations()
        while (true) {
            delay(8000)
            viewModel.fetchConversations()
        }
    }

    // Build chat items from server conversations
    val combinedChats = viewModel.conversations.map { conv ->
        val effectiveUserId = conv.user_id.ifBlank { conv.listener_id }
        val name = conv.user_name.ifBlank { conv.listener_name.ifBlank { "user${effectiveUserId.takeLast(6)}" } }
        val isRegular = conv.is_regular
        ChatDisplayItem(
            userId = effectiveUserId,
            avatarText = name.take(2).uppercase().ifBlank { "U" },
            callerTitle = if (isRegular) "$name · regular" else name,
            lastMessage = conv.last_message.ifBlank { "Tap to chat" },
            timestamp = com.example.trueline_listener.formatTimestamp(conv.last_message_time).ifBlank { "Just now" },
            unreadCount = conv.unread_count,
            isRegular = isRegular,
            lastMessageSender = conv.last_message_sender,
            filterType = if (conv.unread_count > 0) ChatFilter.UNREAD else if (isRegular) ChatFilter.REGULARS else ChatFilter.ALL
        )
    }

    // Apply Search Query filter
    val searchFiltered = if (viewModel.chatSearchQuery.isBlank()) {
        combinedChats
    } else {
        val query = viewModel.chatSearchQuery.trim().lowercase()
        combinedChats.filter {
            it.callerTitle.lowercase().contains(query) ||
            it.lastMessage.lowercase().contains(query) ||
            it.userId.lowercase().contains(query)
        }
    }

    // Apply Category Tab filter
    val filteredChats = when (currentFilter) {
        ChatFilter.ALL -> searchFiltered
        ChatFilter.UNREAD -> searchFiltered.filter { it.unreadCount > 0 }
        ChatFilter.REGULARS -> searchFiltered.filter { it.isRegular }
        ChatFilter.NEEDS_REPLY -> searchFiltered.filter { it.unreadCount > 0 || it.lastMessageSender == "user" }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFB))
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Header: All chats Title & Add (+) Action Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "All chats",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            // Add (+) action button
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable {
                        val firstChat = combinedChats.firstOrNull()
                        if (firstChat != null) {
                            viewModel.openChat(firstChat.userId, firstChat.callerTitle)
                        }
                    },
                shape = CircleShape,
                color = Color(0xFF134E4A)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "New Chat",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Search Bar - Fully Interactive Text Input
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                BasicTextField(
                    value = viewModel.chatSearchQuery,
                    onValueChange = { viewModel.updateChatSearchQuery(it) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    textStyle = TextStyle(
                        fontSize = 14.5.sp,
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(Color(0xFF134E4A)),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (viewModel.chatSearchQuery.isEmpty()) {
                                Text(
                                    text = "Search chats",
                                    fontSize = 14.5.sp,
                                    color = Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Normal
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                if (viewModel.chatSearchQuery.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .clickable { viewModel.updateChatSearchQuery("") },
                        shape = CircleShape,
                        color = Color(0xFFE2E8F0)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Clear Search",
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(filterScrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChatFilterPill(
                title = "All",
                isSelected = currentFilter == ChatFilter.ALL,
                onClick = { viewModel.selectChatFilter(ChatFilter.ALL) }
            )
            ChatFilterPill(
                title = "Unread",
                isSelected = currentFilter == ChatFilter.UNREAD,
                onClick = { viewModel.selectChatFilter(ChatFilter.UNREAD) }
            )
            ChatFilterPill(
                title = "Regulars",
                isSelected = currentFilter == ChatFilter.REGULARS,
                onClick = { viewModel.selectChatFilter(ChatFilter.REGULARS) }
            )
            ChatFilterPill(
                title = "Needs reply",
                isSelected = currentFilter == ChatFilter.NEEDS_REPLY,
                onClick = { viewModel.selectChatFilter(ChatFilter.NEEDS_REPLY) }
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 5. Chat List Container Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column {
                if (filteredChats.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp, horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val emptyMessage = when (currentFilter) {
                            ChatFilter.ALL -> "No conversations yet"
                            ChatFilter.UNREAD -> "No unread messages"
                            ChatFilter.REGULARS -> "No regulars yet"
                            ChatFilter.NEEDS_REPLY -> "No replies needed"
                        }
                        val emptySubtext = when (currentFilter) {
                            ChatFilter.ALL -> "When users message you, their chats will appear here."
                            ChatFilter.UNREAD -> "You're all caught up with your messages."
                            ChatFilter.REGULARS -> "Callers who connect with you frequently will appear here."
                            ChatFilter.NEEDS_REPLY -> "All user queries have been answered."
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = emptyMessage,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF334155)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = emptySubtext,
                                fontSize = 12.5.sp,
                                color = Color(0xFF94A3B8),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    filteredChats.forEachIndexed { index, item ->
                        ChatRowItem(
                            item = item,
                            onClick = {
                                viewModel.openChat(item.userId, item.callerTitle)
                            }
                        )
                        if (index < filteredChats.lastIndex) {
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 6. Footer Privacy Subtext
        Text(
            text = "Chats stay inside TrueLine. Numbers are never shared.",
            fontSize = 12.sp,
            color = Color(0xFF94A3B8),
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun ChatFilterPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFF134E4A) else Color.White,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else Color(0xFF334155),
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun ChatRowItem(
    item: ChatDisplayItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Avatar Circle
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = Color(0xFFE2ECE9)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = item.avatarText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = item.callerTitle,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = item.lastMessage,
                    fontSize = 12.5.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Right side: Timestamp & Unread Badge
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = item.timestamp,
                fontSize = 11.5.sp,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Medium
            )

            if (item.unreadCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF134E4A)
                ) {
                    Text(
                        text = "${item.unreadCount}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
