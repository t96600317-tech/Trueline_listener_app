package com.example.trueline_listener.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

data class OnlineCallerAvatar(
    val userId: String,
    val userName: String,
    val avatarText: String,
    val labelText: String
)

data class ChatDisplayItem(
    val userId: String,
    val avatarText: String,
    val callerTitle: String,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int = 0,
    val isRegular: Boolean = false,
    val filterType: ChatFilter
)

@Composable
fun ListenerChatScreen(viewModel: MainPortalViewModel) {
    val scrollState = rememberScrollState()
    val onlineScrollState = rememberScrollState()
    val filterScrollState = rememberScrollState()
    val currentFilter = viewModel.selectedChatFilter

    // Periodically refresh conversation list in background
    LaunchedEffect(Unit) {
        viewModel.fetchConversations()
        while (true) {
            delay(8000)
            viewModel.fetchConversations()
        }
    }

    val onlineCallers = listOf(
        OnlineCallerAvatar("user_8f21", "Caller 8f21", "8F", "8f"),
        OnlineCallerAvatar("user_2c07", "Caller 2c07", "2C", "2c"),
        OnlineCallerAvatar("user_6b93", "Caller 6b93", "6B", "6b"),
        OnlineCallerAvatar("user_d1f8", "Caller d1f8", "D1", "d1"),
        OnlineCallerAvatar("user_7e20", "Caller 7e20", "7E", "7e")
    )

    // Build chat items from server conversations or default mock conversations
    val serverChats = viewModel.conversations.map { conv ->
        val name = conv.user_name.ifBlank { "Caller ${conv.user_id.takeLast(4)}" }
        ChatDisplayItem(
            userId = conv.user_id,
            avatarText = name.take(2).uppercase().ifBlank { "CL" },
            callerTitle = name,
            lastMessage = conv.last_message.ifBlank { "Tap to chat" },
            timestamp = conv.last_message_time.ifBlank { "Just now" },
            unreadCount = conv.unread_count,
            isRegular = true,
            filterType = if (conv.unread_count > 0) ChatFilter.UNREAD else ChatFilter.ALL
        )
    }

    val fallbackChats = listOf(
        ChatDisplayItem(
            userId = "user_8f21",
            avatarText = "8F",
            callerTitle = "caller 8f21 · regular",
            lastMessage = "Voice note · 0:14",
            timestamp = "9:14 PM",
            unreadCount = 2,
            isRegular = true,
            filterType = ChatFilter.UNREAD
        ),
        ChatDisplayItem(
            userId = "user_2c07",
            avatarText = "2C",
            callerTitle = "caller 2c07",
            lastMessage = "thank you for today, felt lighter",
            timestamp = "9:12 PM",
            unreadCount = 0,
            filterType = ChatFilter.ALL
        ),
        ChatDisplayItem(
            userId = "user_6b93",
            avatarText = "6B",
            callerTitle = "caller 6b93",
            lastMessage = "call me tomorrow same time?",
            timestamp = "9:11 PM",
            unreadCount = 1,
            filterType = ChatFilter.NEEDS_REPLY
        ),
        ChatDisplayItem(
            userId = "user_d1f8",
            avatarText = "D1",
            callerTitle = "caller d1f8 · regular",
            lastMessage = "Voice note · 0:38",
            timestamp = "8:52 PM",
            unreadCount = 0,
            isRegular = true,
            filterType = ChatFilter.REGULARS
        ),
        ChatDisplayItem(
            userId = "user_4a55",
            avatarText = "4A",
            callerTitle = "caller 4a55",
            lastMessage = "sorry i missed the call",
            timestamp = "7:20 PM",
            unreadCount = 0,
            filterType = ChatFilter.ALL
        ),
        ChatDisplayItem(
            userId = "user_7e20",
            avatarText = "7E",
            callerTitle = "caller 7e20",
            lastMessage = "ok didi, good night",
            timestamp = "6:15 PM",
            unreadCount = 0,
            filterType = ChatFilter.ALL
        )
    )

    val combinedChats = if (serverChats.isNotEmpty()) serverChats else fallbackChats

    // Apply Search Query filter
    val searchFiltered = if (viewModel.chatSearchQuery.isBlank()) {
        combinedChats
    } else {
        val query = viewModel.chatSearchQuery.lowercase()
        combinedChats.filter {
            it.callerTitle.lowercase().contains(query) ||
            it.lastMessage.lowercase().contains(query)
        }
    }

    // Apply Category Tab filter
    val filteredChats = when (currentFilter) {
        ChatFilter.ALL -> searchFiltered
        ChatFilter.UNREAD -> searchFiltered.filter { it.unreadCount > 0 }
        ChatFilter.REGULARS -> searchFiltered.filter { it.isRegular }
        ChatFilter.NEEDS_REPLY -> searchFiltered.filter { it.unreadCount > 0 || it.filterType == ChatFilter.NEEDS_REPLY }
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
                        // Open chat with first caller or new contact
                        viewModel.openChat("user_8f21", "Caller 8f21")
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

        // 2. Search Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (viewModel.chatSearchQuery.isBlank()) "Search chats" else viewModel.chatSearchQuery,
                    fontSize = 14.sp,
                    color = if (viewModel.chatSearchQuery.isBlank()) Color(0xFF94A3B8) else Color(0xFF0F172A),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. ONLINE NOW Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "• ONLINE NOW",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F766E),
                letterSpacing = 0.8.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ONLINE NOW Avatars Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(onlineScrollState),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            onlineCallers.forEach { caller ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        viewModel.openChat(caller.userId, caller.userName)
                    }
                ) {
                    Box {
                        Surface(
                            modifier = Modifier.size(46.dp),
                            shape = CircleShape,
                            color = Color(0xFFE2ECE9)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = caller.avatarText,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF334155)
                                )
                            }
                        }
                        // Green Online Dot Badge
                        Surface(
                            modifier = Modifier
                                .size(12.dp)
                                .align(Alignment.BottomEnd),
                            shape = CircleShape,
                            color = Color(0xFF0F766E),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White)
                        ) {}
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = caller.labelText,
                        fontSize = 11.5.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Filter Chips Row
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
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No messages in this category",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
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
