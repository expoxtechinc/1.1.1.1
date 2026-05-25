package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.local.ChatMessage
import com.example.data.local.ChatSession
import com.example.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

// Image resources specified by user
private const val APP_LOGO_URL = "https://www.image2url.com/r2/default/images/1779709348867-d16338fb-19e0-4e1c-8909-d2c8128a2d55.png"
private const val CREATOR_PICTURE_URL = "https://www.image2url.com/r2/default/images/1779709278276-997a686a-5f7d-46d7-993a-0d829a1c9488.png"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val isDark = isSystemInDarkTheme()

    val sessions by viewModel.filteredSessions.collectAsState()
    val currentSessionId by viewModel.currentSessionId.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val error by viewModel.error.collectAsState()
    val showCreatorInfo by viewModel.showCreatorInfo.collectAsState()

    // Glass Background gradient matching requested #F0F4F8 palette
    val backgroundBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0F1419),
                Color(0xFF151B22),
                Color(0xFF1B222B)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF0F4F8),
                Color(0xFFE5ECF4),
                Color(0xFFDCE5F0)
            )
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(320.dp)
                    .fillMaxHeight(),
                drawerContainerColor = if (isDark) Color(0xE6161A20) else Color(0xD9FFFFFF),
                drawerShape = RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp)
            ) {
                // Drawer Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                color = if (isDark) Color(0x33FFFFFF) else Color(0x1F0061A4),
                                shape = CircleShape
                            )
                            .padding(4.dp)
                    ) {
                        AsyncImage(
                            model = APP_LOGO_URL,
                            contentDescription = "AkinAI Logo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "AkinAI Chat History",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Liberian AI Innovation",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // New Chat Button
                Button(
                    onClick = {
                        viewModel.selectSession(null)
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .height(50.dp)
                        .testTag("new_chat_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Chat",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("New Chat Session", fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 20.dp),
                    color = if (isDark) Color(0x22FFFFFF) else Color(0xFFDDE2EA)
                )

                // Search session bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .testTag("search_session_input"),
                    placeholder = { Text("Search chats...", fontSize = 14.sp) },
                    leadingIcon = {
                        AsyncImage(
                            model = APP_LOGO_URL,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (isDark) Color(0x33FFFFFF) else Color(0xFFDDE2EA),
                        focusedContainerColor = if (isDark) Color(0x33000000) else Color(0x11000000),
                        unfocusedContainerColor = if (isDark) Color(0x22000000) else Color(0x0A000000)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Sessions List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    if (sessions.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (searchQuery.isEmpty()) "No prior chats.\nStart a new conversation!" else "No matching chats.",
                                    textAlign = TextAlign.Center,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    } else {
                        items(sessions, key = { it.id }) { session ->
                            val isSelected = session.id == currentSessionId
                            NavigationDrawerItem(
                                label = {
                                    Text(
                                        text = session.title,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 14.sp
                                    )
                                },
                                selected = isSelected,
                                onClick = {
                                    viewModel.selectSession(session.id)
                                    coroutineScope.launch { drawerState.close() }
                                },
                                modifier = Modifier
                                    .padding(vertical = 2.dp)
                                    .testTag("session_item_${session.id}"),
                                shape = RoundedCornerShape(14.dp),
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = if (isDark) Color(0x400061A4) else Color(0xFFD3E3FD),
                                    unselectedContainerColor = Color.Transparent,
                                    selectedTextColor = if (isDark) Color(0xFFD3E3FD) else Color(0xFF041E49)
                                ),
                                badge = {
                                    IconButton(
                                        onClick = { viewModel.deleteSession(session.id) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = "Delete Session",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                // Drawer Footer showing creator button - Styled to look like sleek Frosted Glass
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(
                            BorderStroke(
                                1.dp,
                                if (isDark) Color(0x1AFFFFFF) else Color(0x66FFFFFF)
                            ),
                            RoundedCornerShape(20.dp)
                        )
                        .background(
                            color = if (isDark) Color(0x1F000000) else Color(0x40FFFFFF)
                        )
                        .clickable { viewModel.setShowCreatorInfo(true) }
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = CREATOR_PICTURE_URL,
                            contentDescription = "Akin S. Sokpah Picture",
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Akin S. Sokpah",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Liberia • Bio",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "View Bio",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                // Glassified sticky TopAppBar
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            AsyncImage(
                                model = APP_LOGO_URL,
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "AkinAI",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    letterSpacing = (-0.5).sp,
                                    color = if (isDark) Color.White else Color(0xFF001D35)
                                )
                                Text(
                                    text = "POWERED BY GEMINI",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = if (isDark) Color(0xFF9ECAFF) else Color(0xFF44474E)
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { coroutineScope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("menu_button")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Open History Menu")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.setShowCreatorInfo(true) },
                            modifier = Modifier.testTag("creator_info_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "Creator Information",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = if (isDark) Color(0x99111418) else Color(0x99FFFFFF)
                    ),
                    modifier = Modifier.shadow(1.dp)
                )
            },
            modifier = modifier
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(backgroundBrush)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Chat messages list or vacant welcome screen
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        if (currentSessionId == null && messages.isEmpty()) {
                            WelcomeScreen(
                                onPromptSelected = { selectedPrompt ->
                                    viewModel.updateInputText(selectedPrompt)
                                    viewModel.sendMessage()
                                },
                                onShowProfile = { viewModel.setShowCreatorInfo(true) }
                            )
                        } else {
                            val listState = rememberLazyListState()
                            
                            // Scroll to bottom when new messages come in
                            LaunchedEffect(messages.size) {
                                if (messages.isNotEmpty()) {
                                    listState.animateScrollToItem(messages.size - 1)
                                }
                            }

                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
                            ) {
                                // Creator Spotlight Glass Card at top of active conversation list to preserve absolute visual context
                                item {
                                    CreatorSpotlightCard(onShowProfile = { viewModel.setShowCreatorInfo(true) })
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                items(messages, key = { it.id }) { msg ->
                                    MessageBubble(message = msg)
                                }

                                if (isGenerating) {
                                    item {
                                        TypingIndicator()
                                    }
                                }
                            }
                        }
                    }

                    // Error Banner
                    AnimatedVisibility(
                        visible = error != null,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        error?.let { errText ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Warning",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = errText,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { viewModel.clearError() },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Dismiss error",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bottom chat input panel
                    ChatInputPanel(
                        value = inputText,
                        onValueChange = { viewModel.updateInputText(it) },
                        onSend = { viewModel.sendMessage() },
                        enabled = !isGenerating
                    )
                }

                // Floating glass indicator reminding about creator & Liberia origin
                if (currentSessionId != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 12.dp, end = 20.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (isDark) Color(0x33FFFFFF) else Color(0x80FFFFFF)
                                ),
                                RoundedCornerShape(20.dp)
                            )
                            .background(
                                color = if (isDark) Color(0x7F161A20) else Color(0x99FFFFFF)
                            )
                            .clickable { viewModel.setShowCreatorInfo(true) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AsyncImage(
                                model = APP_LOGO_URL,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                            )
                            Text(
                                text = "Akin S. Sokpah • Liberia",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    // Detail Creator Info Dialog
    if (showCreatorInfo) {
        CreatorInfoDialog(onDismiss = { viewModel.setShowCreatorInfo(false) })
    }
}

// Creator Spotlight Card component as requested by the HTML design specifications
@Composable
fun CreatorSpotlightCard(
    onShowProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    
    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0x331E2125) else Color(0x66FFFFFF)
        ),
        border = BorderStroke(
            1.dp, 
            if (isDark) Color(0x22FFFFFF) else Color(0x99FFFFFF)
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onShowProfile() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(modifier = Modifier.size(64.dp)) {
                // Main profile photo
                AsyncImage(
                    model = CREATOR_PICTURE_URL,
                    contentDescription = "Akin S. Sokpah Picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            2.dp,
                            Color(0xFFA8C7FA),
                            RoundedCornerShape(16.dp)
                        ),
                    contentScale = ContentScale.Crop
                )
                // Verified Badge positioned beautifully like HTML Mockup
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 6.dp, y = 6.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD3E3FD))
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified Creator",
                        tint = Color(0xFF041E49),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Akin S. Sokpah",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF1B1B1F)
                )
                Text(
                    text = "Liberia • Montserrado County",
                    fontSize = 12.sp,
                    color = if (isDark) Color(0xFFC4C6D0) else Color(0xFF44474E),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Founder & Chief Developer",
                    fontSize = 11.sp,
                    color = Color(0xFF0061A4),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun WelcomeScreen(
    onPromptSelected: (String) -> Unit,
    onShowProfile: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Large app logo in glowing ring
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(
                            listOf(
                                Color(0xFF0061A4),
                                Color(0xFF9ECAFF),
                                Color(0xFFD3E3FD),
                                Color(0xFF0061A4)
                            )
                        )
                    )
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF111418) else Color.White)
                        .padding(6.dp)
                ) {
                    AsyncImage(
                        model = APP_LOGO_URL,
                        contentDescription = "AkinAI Central Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome to AkinAI",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                color = if (isDark) Color.White else Color(0xFF1B1B1F)
            )

            Text(
                text = "Secure assistance engine using high performance models.\nCreated by Akin S. Sokpah in Montserrado County, Liberia.",
                fontSize = 13.sp,
                color = if (isDark) Color(0xFFC4C6D0) else Color(0xFF44474E),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            )

            // Permanent sleek Spotlight card integrated into welcome area
            CreatorSpotlightCard(
                onShowProfile = onShowProfile,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Glass stats summary panel from HTML mockup
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    icon = Icons.Default.Lock,
                    title = "API Status",
                    statusText = "Secure & Active",
                    statusColor = Color(0xFF1B873F), // Green indicator
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Default.Speed,
                    title = "Rate Limit",
                    statusText = "Optimized",
                    statusColor = if (isDark) Color(0xFF9ECAFF) else Color(0xFF0061A4),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Suggestion heading
            Text(
                text = "SUGGESTED PROMPTS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        val promptChips = listOf(
            "Tell me about Akin S. Sokpah and Liberia's tech growth.",
            "Who created AkinAI and where are they from?",
            "What is Montserrado County, Liberia known for?",
            "How can I set secure environment config variables?"
        )

        items(promptChips) { prompt ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0x331E2125) else Color(0x80FFFFFF)
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    1.dp,
                    if (isDark) Color(0x1AFFFFFF) else Color(0xFFE1E2EC)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clickable { onPromptSelected(prompt) }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = APP_LOGO_URL,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = prompt,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color(0xFFE3E2E6) else Color(0xFF1B1B1F),
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun StatCard(
    icon: ImageVector,
    title: String,
    statusText: String,
    statusColor: Color,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0x2B1E2125) else Color(0x66FFFFFF)
        ),
        border = BorderStroke(
            1.dp,
            if (isDark) Color(0x1AFFFFFF) else Color(0xFFDDE2EA)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF0061A4),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title.uppercase(),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color(0xFFC4C6D0) else Color(0xFF44474E),
                letterSpacing = 0.5.sp
            )
            Text(
                text = statusText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor,
                modifier = Modifier.padding(top = 1.dp)
            )
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == "user"
    val isDark = isSystemInDarkTheme()
    
    // Custom transparent bubbles adhering to spec
    val bubbleColor = if (isUser) {
        if (isDark) Color(0x4D0061A4) else Color(0xFFD3E3FD) // Light blue glass mapping
    } else {
        if (isDark) Color(0x2BFFFFFF) else Color(0xFFF2F0F4) // Soft neutral matching Mockup
    }
    
    val textColor = if (isUser) {
        if (isDark) Color(0xFFD3E3FD) else Color(0xFF041E49)
    } else {
        if (isDark) Color(0xFFE3E2E6) else Color(0xFF1B1B1F)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            AsyncImage(
                model = APP_LOGO_URL,
                contentDescription = "AkinAI Logo",
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD3E3FD))
                    .padding(2.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.weight(1f, fill = false),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Text(
                text = if (isUser) "You" else "AkinAI",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color(0xFFC4C6D0) else Color(0xFF44474E),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = bubbleColor),
                shape = RoundedCornerShape(
                    topStart = if (isUser) 16.dp else 2.dp,
                    topEnd = if (isUser) 2.dp else 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                border = BorderStroke(
                    1.dp,
                    if (isUser) {
                        if (isDark) Color(0x339ECAFF) else Color(0x1A000000)
                    } else {
                        if (isDark) Color(0x1AFFFFFF) else Color(0x80FFFFFF)
                    }
                )
            ) {
                Text(
                    text = message.text,
                    fontSize = 14.sp,
                    color = textColor,
                    lineHeight = 19.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "User avatar",
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
fun TypingIndicator() {
    val isDark = isSystemInDarkTheme()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = APP_LOGO_URL,
            contentDescription = null,
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .padding(2.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color(0x2BFFFFFF) else Color(0xFFF2F0F4)
            ),
            shape = RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp),
            modifier = Modifier.width(140.dp),
            border = BorderStroke(1.dp, if (isDark) Color(0x16FFFFFF) else Color(0x80FFFFFF))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "AkinAI typing",
                    fontSize = 11.sp,
                    color = if (isDark) Color(0xFFC4C6D0) else Color(0xFF44474E),
                    lineHeight = 14.sp
                )
                // Linear progress mimicking loading state
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = Color(0xFF0061A4),
                    trackColor = Color(0xFF0061A4).copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
fun ChatInputPanel(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val isDark = isSystemInDarkTheme()

    Surface(
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth(),
        color = if (isDark) Color(0xE61A2027) else Color(0xD9FFFFFF)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Frosted Text Input design with app logo icon inside
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("message_input_field"),
                    placeholder = { Text("Ask AkinAI...", fontSize = 14.sp) },
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = if (isDark) Color(0x33000000) else Color(0x1F000000),
                        unfocusedContainerColor = if (isDark) Color(0x22000000) else Color(0x0F000000),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    leadingIcon = {
                        AsyncImage(
                            model = APP_LOGO_URL,
                            contentDescription = null,
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                        )
                    },
                    enabled = enabled
                )

                IconButton(
                    onClick = {
                        onSend()
                        keyboardController?.hide()
                    },
                    enabled = enabled && value.isNotBlank(),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (enabled && value.isNotBlank()) Color(0xFF0061A4) else (if (isDark) Color(0x2BFFFFFF) else Color(0xFFE1E2EC))
                        )
                        .testTag("send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send message",
                        tint = if (enabled && value.isNotBlank()) Color.White else (if (isDark) Color(0x3DFFFFFF) else Color(0x61000000))
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            // Static disclaimer notice pointing to creators and system limits
            Text(
                text = "AkinAI may hallucinate. Created by Akin S. Sokpah • Liberia. Powered by Gemini.",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color(0x80C4C6D0) else Color(0x8044474E),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CreatorInfoDialog(onDismiss: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color(0xF2161A21) else Color(0xF2FBFDFD)
            ),
            border = BorderStroke(
                1.dp,
                if (isDark) Color(0x33FFFFFF) else Color(0x99FFFFFF)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circle Creator image with premium golden outline
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF0061A4),
                                    Color(0xFFFFD700)
                                )
                            )
                        )
                        .padding(3.dp)
                ) {
                    AsyncImage(
                        model = CREATOR_PICTURE_URL,
                        contentDescription = "Akin S. Sokpah Picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Akin S. Sokpah",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isDark) Color.White else Color(0xFF1B1B1F)
                )

                Text(
                    text = "Montserrado County, Liberia",
                    fontSize = 13.sp,
                    color = Color(0xFF0061A4),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                HorizontalDivider(color = if (isDark) Color(0x22FFFFFF) else Color(0xFFE1E2EC))

                Spacer(modifier = Modifier.height(14.dp))

                // Bio Description texts
                Text(
                    text = "Akin is a visionary developer and technologist originating from Liberia. Driven by a deep commitment to expand technical capability and human capital, Akin created 'AkinAI' as an accessible portal for advanced AI. It stands as a testament to the surging potential of Liberia's digital sector and the talent growing in Montserrado County. Powered under the hood by Gemini's powerful server-side API.",
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    color = if (isDark) Color(0xFFC4C6D0) else Color(0xFF44474E),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = APP_LOGO_URL,
                        contentDescription = null,
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AkinAI v1.1 • Premium Glass",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0061A4)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0061A4)
                    )
                ) {
                    Text("Close biography", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
