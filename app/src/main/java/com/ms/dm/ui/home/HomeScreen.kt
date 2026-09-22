package com.ms.dm.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ms.dm.data.MoodLevel
import com.ms.dm.ui.MainViewModel
import com.ms.dm.ui.auth.EmailAuthCard
import com.ms.dm.ui.profile.ProfileScreen
import com.ms.dm.ui.progress.ProgressScreen
import com.ms.dm.ui.theme.DMTheme

@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    var selectedItem by remember { mutableIntStateOf(0) }

    val userProfile by viewModel.userProfile.collectAsState()
    val dailyMoods by viewModel.dailyMoods.collectAsState()
    val currentUserEmail by viewModel.currentUserEmail.collectAsState()
    val isEmailVerified by viewModel.isEmailVerified.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    val items = listOf("Home", "Progress", "Tools", "Profile")
    val icons = listOf(
        Icons.Default.Home,
        Icons.AutoMirrored.Filled.ShowChart,
        Icons.Default.Build,
        Icons.Default.Person
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedItem) {
            0 -> HomeContent(
                innerPadding = innerPadding,
                userName = userProfile.name,
                currentEmail = currentUserEmail,
                isEmailVerified = isEmailVerified,
                todayMood = dailyMoods.lastOrNull()?.mood,
                onSignIn = { email, pass -> viewModel.signInWithEmail(email, pass) },
                onSignUp = { email, pass -> viewModel.signUpWithEmail(email, pass) },
                onSignInWithGmail = { gmail -> viewModel.signInWithGmail(gmail) },
                onSendVerification = { viewModel.sendVerificationEmail() },
                onCheckVerification = { viewModel.refreshAuthState() },
                onMoodSelected = { newMood ->
                    viewModel.recordMoodForToday(newMood)
                }
            )
            1 -> ProgressScreen(
                dailyMoods = dailyMoods,
                innerPadding = innerPadding
            )
            2 -> SimplePlaceholderScreen("Self-Help Tools", innerPadding)
            3 -> ProfileScreen(
                userProfile = userProfile,
                onSaveProfile = { updated -> viewModel.saveProfile(updated) },
                onSignOut = { viewModel.signOut() },
                innerPadding = innerPadding
            )
            else -> HomeContent(
                innerPadding = innerPadding,
                userName = userProfile.name,
                currentEmail = currentUserEmail,
                isEmailVerified = isEmailVerified,
                todayMood = dailyMoods.lastOrNull()?.mood,
                onSignIn = { _, _ -> },
                onSignUp = { _, _ -> },
                onSignInWithGmail = {},
                onSendVerification = {},
                onCheckVerification = {},
                onMoodSelected = {}
            )
        }
    }
}

@Composable
fun SimplePlaceholderScreen(title: String, innerPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Coming soon...",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            )
        }
    }
}

@Composable
fun HomeContent(
    innerPadding: PaddingValues,
    userName: String,
    currentEmail: String,
    isEmailVerified: Boolean,
    todayMood: MoodLevel?,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String) -> Unit,
    onSignInWithGmail: (String) -> Unit,
    onSendVerification: () -> Unit,
    onCheckVerification: () -> Unit,
    onMoodSelected: (MoodLevel) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            HeaderSection(userName = userName)
        }

        // Email Verification Card / Gate
        if (!isEmailVerified || currentEmail.isEmpty()) {
            item {
                EmailAuthCard(
                    currentEmail = currentEmail,
                    isEmailVerified = isEmailVerified,
                    onSignIn = onSignIn,
                    onSignUp = onSignUp,
                    onSignInWithGmail = onSignInWithGmail,
                    onSendVerification = onSendVerification,
                    onCheckVerification = onCheckVerification
                )
            }
        }

        item {
            CrisisCard()
        }

        item {
            MoodTrackerSection(
                selectedMood = todayMood,
                onMoodSelected = onMoodSelected
            )
        }
    }
}

@Composable
fun HeaderSection(userName: String) {
    val initials = getInitials(userName)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Good Morning.",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Text(
                text = "Take it one step at a time.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            )
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CrisisCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = "Crisis Help Card: I need help right now. Connect to a crisis counselor."
            }
            .clickable { /* Handle crisis action */ },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "I need help right now",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                )
                Text(
                    text = "Connect to a crisis counselor",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                    )
                )
            }
        }
    }
}

@Composable
fun MoodTrackerSection(
    selectedMood: MoodLevel?,
    onMoodSelected: (MoodLevel) -> Unit
) {
    val isAlreadyLogged = selectedMood != null

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "How is your energy today?",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
        }

        if (isAlreadyLogged) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Recorded for today: ${selectedMood.emoji} ${selectedMood.label} (Selection locked for today)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MoodLevel.entries.forEach { level ->
                MoodButton(
                    emoji = level.emoji,
                    label = level.label,
                    isSelected = selectedMood == level,
                    isEnabled = !isAlreadyLogged, // Disabled for entire day once logged!
                    onClick = { onMoodSelected(level) }
                )
            }
        }
    }
}

@Composable
fun MoodButton(
    emoji: String,
    label: String,
    isSelected: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = "Mood: $label. ${if (isSelected) "Selected for today" else if (!isEnabled) "Disabled for today" else ""}"
        }
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isSelected -> primaryColor.copy(alpha = 0.15f)
                        !isEnabled -> Color.Gray.copy(alpha = 0.08f)
                        else -> Color.White
                    }
                )
                .then(
                    if (isSelected) Modifier.border(2.dp, primaryColor, CircleShape)
                    else Modifier
                )
                .clickable(enabled = isEnabled) { onClick() }
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                fontSize = 28.sp,
                modifier = Modifier.graphicsLayer {
                    alpha = if (!isEnabled && !isSelected) 0.4f else 1.0f
                }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isSelected -> primaryColor
                    !isEnabled -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f)
                    else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                }
            )
        )
    }
}

private fun getInitials(fullName: String): String {
    val parts = fullName.trim().split("\\s+".toRegex())
    return when {
        parts.isEmpty() || parts[0].isEmpty() -> "JD"
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> "${parts[0].first()}${parts.last().first()}".uppercase()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
fun MainScreenPreview() {
    DMTheme {
        MainScreen()
    }
}
