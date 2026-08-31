@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ai.GeminiClient
import com.example.ai.LocalAIIntelligenceEngine
import com.example.data.models.SupportedLanguage
import com.example.data.repository.KarigarRepository
import com.example.ui.components.AudioPlayButton
import com.example.ui.theme.*
import com.example.utils.AudioVoiceHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

data class SaathiMessage(
    val id: String = UUID.randomUUID().toString(),
    val senderIsSaathi: Boolean,
    val text: String,
    val timestamp: String = "Just now",
    val actionSuggestion: String? = null,
    val isVoiceGenerated: Boolean = false
)

@Composable
fun SaathiScreen(
    currentLanguage: SupportedLanguage,
    repository: KarigarRepository,
    audioHelper: AudioVoiceHelper?,
    initialQuery: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateMarketPulse: () -> Unit,
    onNavigateOneTapStudio: () -> Unit,
    onNavigateOrders: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val currentUser by repository.currentUser.collectAsState()
    val artisanDisplayName = currentUser?.name?.ifBlank { "Artisan Partner" } ?: "Artisan Partner"
    val craftSpecialty = currentUser?.craftSpecialty?.ifBlank { "Handmade Master Crafts" } ?: "Handmade Master Crafts"

    var inputQuery by remember { mutableStateOf("") }
    var isRecordingVoice by remember { mutableStateOf(false) }
    var isLoadingSaathi by remember { mutableStateOf(false) }
    var autoSpeakResponses by remember { mutableStateOf(true) }

    val welcomeGreeting = when (currentLanguage) {
        SupportedLanguage.TAMIL -> "வணக்கம் $artisanDisplayName! நான் உங்கள் சாதி (Saathi) - கைவினை வணிகத் தோழன். விலை நிர்ணயம், புதிய போக்குகள், ஆர்டர்கள் மற்றும் உற்பத்தி பற்றி என்னிடம் தயங்காமல் கேளுங்கள்."
        SupportedLanguage.HINDI -> "नमस्ते $artisanDisplayName जी! मैं हूँ आपका साथी (Saathi) - आपका शिल्प व्यापार सलाहकार। कीमत तय करने, बाज़ार की मांग, या ऑर्डर की सलाह के लिए मुझसे पूछें।"
        SupportedLanguage.TELUGU -> "నమస్కారం $artisanDisplayName గారు! నేను మీ సాథీ (Saathi) - మీ చేతివృత్తుల వ్యాపార సహాయకుడిని. ధర నిర్ణయం, మార్కెట్ ట్రెండ్స్ లేదా ఆర్డర్ల వివరాలు అడగండి."
        else -> "Welcome $artisanDisplayName! I am Saathi, your craft business companion. Ask me anything about fair pricing, festival demand, bulk orders, or what to create next."
    }

    val conversation = remember(artisanDisplayName) {
        mutableStateListOf(
            SaathiMessage(
                senderIsSaathi = true,
                text = welcomeGreeting
            )
        )
    }

    val suggestedQuestions = listOf(
        "What price should I keep?",
        "What products are trending?",
        "How are my sales?",
        "What should I make next?",
        "Do I have pending orders?",
        "How can I improve my sales?"
    )

    fun sendQuestionToSaathi(questionText: String, isVoice: Boolean = false) {
        if (questionText.isBlank()) return
        val userMsg = SaathiMessage(
            senderIsSaathi = false,
            text = questionText,
            isVoiceGenerated = isVoice
        )
        conversation.add(userMsg)
        inputQuery = ""
        isLoadingSaathi = true

        coroutineScope.launch {
            // Scroll to bottom
            delay(100)
            listState.animateScrollToItem(conversation.size - 1)

            // Query Gemini or domain engine
            val systemContext = "You are 'Saathi' (साथी), a deeply respectful, supportive Indian craft business companion helping traditional artisans. Speak warmly in simple language matching the user's intent. Artisan name: $artisanDisplayName ($craftSpecialty). Current language: ${currentLanguage.englishName}."
            val prompt = "$systemContext\n\nArtisan asks: $questionText\n\nProvide practical, encouraging business advice with direct actionable numbers or tips."

            val geminiResult = GeminiClient.generateWithGemini(prompt)
            val answer = geminiResult.getOrElse {
                LocalAIIntelligenceEngine.answerBusinessCoachQuestion(
                    question = questionText,
                    artisanName = artisanDisplayName,
                    activeProductsCount = 4,
                    pendingOrdersCount = 2,
                    userLang = currentLanguage
                )
            }

            isLoadingSaathi = false
            val saathiReply = SaathiMessage(
                senderIsSaathi = true,
                text = answer
            )
            conversation.add(saathiReply)

            delay(100)
            listState.animateScrollToItem(conversation.size - 1)

            if (autoSpeakResponses) {
                audioHelper?.speak(answer, currentLanguage)
            }
        }
    }

    // Auto trigger initial query if navigated with one
    LaunchedEffect(initialQuery) {
        if (!initialQuery.isNullOrBlank()) {
            sendQuestionToSaathi(initialQuery, isVoice = false)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.img_saathi_mascot),
                            contentDescription = "Saathi Avatar",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, GoldenAmberSecondary, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Saathi",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TerracottaPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SuccessGreenBg)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "AI Companion",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessGreen
                                    )
                                }
                            }
                            Text(
                                "Your craft business companion",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_saathi_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { autoSpeakResponses = !autoSpeakResponses },
                        modifier = Modifier.testTag("btn_toggle_saathi_speech")
                    ) {
                        Icon(
                            imageVector = if (autoSpeakResponses) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Toggle Speech",
                            tint = if (autoSpeakResponses) TerracottaPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(WarmBgLight)
        ) {
            // Suggested Prompts Chips Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(suggestedQuestions) { sq ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .clickable { sendQuestionToSaathi(sq) }
                            .testTag("chip_suggested_${sq.take(10).lowercase().replace(' ', '_')}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = GoldenAmberSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = sq,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Conversation Messages Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                items(conversation) { msg ->
                    SaathiChatBubble(
                        message = msg,
                        currentLanguage = currentLanguage,
                        audioHelper = audioHelper,
                        onNavigateMarketPulse = onNavigateMarketPulse,
                        onNavigateOneTapStudio = onNavigateOneTapStudio,
                        onNavigateOrders = onNavigateOrders
                    )
                }

                if (isLoadingSaathi) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(GoldenAmberLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = GoldenAmberSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = TerracottaPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Saathi is formulating advice...",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Voice Recording Waveform Overlay if active
            AnimatedVisibility(
                visible = isRecordingVoice,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    color = TerracottaPrimary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            repeat(7) { index ->
                                val infiniteTransition = rememberInfiniteTransition()
                                val heightMultiplier by infiniteTransition.animateFloat(
                                    initialValue = 8f,
                                    targetValue = 28f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(400 + (index * 80), easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    )
                                )
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(heightMultiplier.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color.White)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Listening to your voice... Tap Mic again to send",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Bottom Input Bar with Voice & Send
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Voice Mic Button
                    IconButton(
                        onClick = {
                            if (!isRecordingVoice) {
                                isRecordingVoice = true
                                audioHelper?.speak("Listening, please speak your question...", currentLanguage)
                            } else {
                                isRecordingVoice = false
                                val demoVoiceQueries = listOf(
                                    "How should I price my silk sarees for Diwali?",
                                    "Which cities are buying the most handloom fabrics?",
                                    "How do I improve my product photo lighting?",
                                    "Suggest a fair price for 50 pieces bulk order"
                                )
                                sendQuestionToSaathi(demoVoiceQueries.random(), isVoice = true)
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isRecordingVoice) DeepOrangeAccent else GoldenAmberLight)
                            .testTag("btn_saathi_mic")
                    ) {
                        Icon(
                            imageVector = if (isRecordingVoice) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = if (isRecordingVoice) Color.White else TerracottaPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Text Input Field
                    OutlinedTextField(
                        value = inputQuery,
                        onValueChange = { inputQuery = it },
                        placeholder = {
                            Text(
                                "Ask Saathi in ${currentLanguage.nativeName}...",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_saathi_query"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TerracottaPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Send Button
                    IconButton(
                        onClick = {
                            if (inputQuery.isNotBlank()) {
                                sendQuestionToSaathi(inputQuery)
                            }
                        },
                        enabled = inputQuery.isNotBlank() && !isLoadingSaathi,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (inputQuery.isNotBlank()) TerracottaPrimary else MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("btn_saathi_send")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (inputQuery.isNotBlank()) Color.White else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SaathiChatBubble(
    message: SaathiMessage,
    currentLanguage: SupportedLanguage,
    audioHelper: AudioVoiceHelper?,
    onNavigateMarketPulse: () -> Unit,
    onNavigateOneTapStudio: () -> Unit,
    onNavigateOrders: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (message.senderIsSaathi) Arrangement.Start else Arrangement.End
    ) {
        if (message.senderIsSaathi) {
            Image(
                painter = painterResource(id = R.drawable.img_saathi_mascot),
                contentDescription = "Saathi",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .border(1.dp, GoldenAmberSecondary, CircleShape)
                    .align(Alignment.Top),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (message.senderIsSaathi) Alignment.Start else Alignment.End
        ) {
            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (message.senderIsSaathi) 4.dp else 16.dp,
                    bottomEnd = if (message.senderIsSaathi) 16.dp else 4.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (message.senderIsSaathi) MaterialTheme.colorScheme.surface else TerracottaPrimary
                ),
                elevation = CardDefaults.cardElevation(1.dp),
                border = if (message.senderIsSaathi) androidx.compose.foundation.BorderStroke(1.dp, GoldenAmberLight) else null
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (message.senderIsSaathi) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Saathi Companion",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerracottaPrimary
                            )
                            AudioPlayButton(
                                textToSpeak = message.text,
                                language = currentLanguage,
                                audioHelper = audioHelper
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Text(
                        text = message.text,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = if (message.senderIsSaathi) MaterialTheme.colorScheme.onSurface else Color.White
                    )

                    if (message.isVoiceGenerated) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = if (message.senderIsSaathi) TerracottaPrimary else Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Voice Query",
                                fontSize = 10.sp,
                                color = if (message.senderIsSaathi) MaterialTheme.colorScheme.onSurfaceVariant else Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Quick shortcuts if relevant
            if (message.senderIsSaathi && (message.text.contains("price", ignoreCase = true) || message.text.contains("trending", ignoreCase = true) || message.text.contains("demand", ignoreCase = true))) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SuggestionChip(
                        onClick = onNavigateMarketPulse,
                        label = { Text("📊 Check Market Pulse", fontSize = 10.sp) }
                    )
                    SuggestionChip(
                        onClick = onNavigateOneTapStudio,
                        label = { Text("✨ List New Product", fontSize = 10.sp) }
                    )
                }
            }
        }
    }
}
