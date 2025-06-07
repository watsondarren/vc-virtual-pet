package com.example.myapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.myapp.data.CatApi
import com.example.myapp.data.CatImage
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.roundToInt
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private val catApi = Retrofit.Builder()
        .baseUrl("https://api.thecatapi.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(CatApi::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(catApi)
                }
            }
        }
    }
}

private val catNames = listOf(
    "Whiskers", "Luna", "Oliver", "Bella", "Simba",
    "Milo", "Cleo", "Leo", "Lily", "Charlie",
    "Nala", "Max", "Daisy", "Oscar", "Lucy",
    "Jasper", "Mia", "Tiger", "Sophie", "Jack",
    "Loki", "Chloe", "Felix", "Ruby", "Shadow",
    "Mittens", "Ginger", "Smokey", "Pepper", "Coco"
)

data class FavoriteCat(
    val image: CatImage,
    val name: String
)

@Composable
fun HungerBar(
    hungerLevel: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(20.dp)
            .padding(4.dp)
    ) {
        // Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = Color.LightGray,
                    shape = MaterialTheme.shapes.small
                )
        )
        
        // Hunger level
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(hungerLevel)
                .align(Alignment.BottomCenter)
                .background(
                    color = when {
                        hungerLevel > 0.7f -> Color(0xFF4CAF50) // Green
                        hungerLevel > 0.3f -> Color(0xFFFFA726) // Orange
                        else -> Color(0xFFE57373) // Red
                    },
                    shape = MaterialTheme.shapes.small
                )
        )
    }
}

@Composable
fun CurrencyIndicator(
    coins: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🪙",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = coins.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun FeedingAnimation(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF4CAF50) // Green color
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale = infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Canvas(modifier = modifier) {
        // Draw a bowl shape
        drawCircle(
            color = color,
            radius = 20f * scale.value,
            center = Offset(size.width / 2, size.height / 2),
            alpha = 0.7f
        )
    }
}

@Composable
fun GlitterEffect(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFFFD700) // Gold color
) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    val scale = infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Canvas(modifier = modifier) {
        rotate(rotation.value) {
            // Draw multiple glitter particles
            repeat(8) { index ->
                val angle = (index * 45f) * (Math.PI / 180f)
                val x = size.width / 2 + (size.width / 2 * Math.cos(angle)).toFloat()
                val y = size.height / 2 + (size.height / 2 * Math.sin(angle)).toFloat()
                
                drawCircle(
                    color = color,
                    radius = 8f * scale.value,
                    center = Offset(x, y),
                    alpha = 0.7f
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(catApi: CatApi) {
    var catImage by remember { mutableStateOf<CatImage?>(null) }
    var imageOffset by remember { mutableStateOf(Offset.Zero) }
    var currentCatName by remember { mutableStateOf("") }
    var favorites by remember { mutableStateOf(listOf<FavoriteCat>()) }
    var selectedTab by remember { mutableStateOf(0) }
    var isSpecialCat by remember { mutableStateOf(false) }
    var isCompactView by remember { mutableStateOf(false) }
    var isFeeding by remember { mutableStateOf(false) }
    var hungerLevel by remember { mutableStateOf(1f) }
    var coins by remember { mutableStateOf(10) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Hunger depletion effect
    LaunchedEffect(catImage) {
        if (catImage != null) {
            hungerLevel = 1f
            var hasShownHungryToast = false
            while (true) {
                kotlinx.coroutines.delay(1000) // Update every second
                hungerLevel = (hungerLevel - 0.0167f).coerceAtLeast(0f) // Deplete over 60 seconds
                
                // Show toast when hunger drops below 30% and hasn't shown the toast yet
                if (hungerLevel < 0.3f && !hasShownHungryToast) {
                    Toast.makeText(context, "${currentCatName} is getting hungry! ${if (isSpecialCat) "✨" else ""}", Toast.LENGTH_LONG).show()
                    hasShownHungryToast = true
                } else if (hungerLevel >= 0.3f) {
                    hasShownHungryToast = false
                }
            }
        }
    }
    
    fun loadFavoriteCat(favorite: FavoriteCat) {
        catImage = favorite.image
        currentCatName = favorite.name
        imageOffset = Offset.Zero
        isSpecialCat = false
        selectedTab = 0
        hungerLevel = 1f
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Cat Viewer") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Favorites") }
            )
        }
        
        when (selectedTab) {
            0 -> {
                Text(
                    text = "Welcome to Cat Frenzy!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (coins > 0) {
                                coins-- // Deduct 1 coin
                                scope.launch {
                                    try {
                                        catImage = catApi.getRandomCat().firstOrNull()
                                        imageOffset = Offset.Zero
                                        currentCatName = catNames.random()
                                        hungerLevel = 1f
                                        // 10% chance for a special cat
                                        isSpecialCat = Random.nextFloat() < 0.1f
                                        if (isSpecialCat) {
                                            Toast.makeText(context, "✨ You found a special cat! ✨", Toast.LENGTH_LONG).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Failed to load cat image", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Not enough coins to load a new cat!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        enabled = coins > 0
                    ) {
                        Text("Load Random Cat (1 🪙)")
                    }
                }

                if (catImage != null && currentCatName.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val isDuplicate = favorites.any { it.image.url == catImage!!.url }
                                if (isDuplicate) {
                                    Toast.makeText(context, "This cat is already in your favorites!", Toast.LENGTH_SHORT).show()
                                } else {
                                    val newFavorite = FavoriteCat(catImage!!, currentCatName)
                                    favorites = favorites + newFavorite
                                    Toast.makeText(context, "Added ${currentCatName} to favorites!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            )
                        ) {
                            Text("Save to Favorites")
                        }

                        Button(
                            onClick = {
                                isFeeding = true
                                hungerLevel = 1f
                                scope.launch {
                                    kotlinx.coroutines.delay(2000) // Show feeding animation for 2 seconds
                                    isFeeding = false
                                    Toast.makeText(context, "${currentCatName} enjoyed the food! ${if (isSpecialCat) "✨" else ""}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFB19CD9), // Pastel purple
                                contentColor = Color.White
                            ),
                            enabled = !isFeeding
                        ) {
                            Text(if (isFeeding) "Feeding..." else "Feed Cat")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    catImage?.let { cat ->
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .offset { IntOffset(imageOffset.x.roundToInt(), imageOffset.y.roundToInt()) }
                                .pointerInput(Unit) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        imageOffset = Offset(
                                            x = imageOffset.x + dragAmount.x,
                                            y = imageOffset.y + dragAmount.y
                                        )
                                    }
                                }
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(cat.url),
                                contentDescription = "Random cat",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                            
                            if (isSpecialCat) {
                                GlitterEffect(
                                    modifier = Modifier.fillMaxSize(),
                                    color = Color(0xFFFFD700) // Gold color
                                )
                            }

                            if (isFeeding) {
                                FeedingAnimation(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .align(Alignment.BottomCenter)
                                        .offset(y = 20.dp),
                                    color = Color(0xFFB19CD9) // Match the feed button color
                                )
                            }
                        }
                    }
                    
                    if (currentCatName.isNotEmpty()) {
                        Text(
                            text = if (isSpecialCat) "Meet Special Cat ${currentCatName}!" else "Meet ${currentCatName}!",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp)
                        )
                    }

                    // Add hunger bar on the right side
                    if (catImage != null) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            HungerBar(
                                hungerLevel = hungerLevel,
                                modifier = Modifier.height(200.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            CurrencyIndicator(
                                coins = coins,
                                modifier = Modifier
                            )
                        }
                    }
                }
            }
            1 -> {
                if (favorites.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No favorites yet!",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Compact View",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Switch(
                                checked = isCompactView,
                                onCheckedChange = { isCompactView = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                        
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(if (isCompactView) 8.dp else 16.dp)
                        ) {
                            items(favorites) { favorite ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(if (isCompactView) 4.dp else 8.dp)
                                        .clickable { loadFavoriteCat(favorite) },
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Box {
                                        if (isCompactView) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Image(
                                                    painter = rememberAsyncImagePainter(favorite.image.url),
                                                    contentDescription = "Favorite cat",
                                                    modifier = Modifier
                                                        .size(60.dp),
                                                    contentScale = ContentScale.Fit
                                                )
                                                Spacer(modifier = Modifier.width(16.dp))
                                                Text(
                                                    text = favorite.name,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        } else {
                                            Column(
                                                modifier = Modifier.padding(16.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Image(
                                                    painter = rememberAsyncImagePainter(favorite.image.url),
                                                    contentDescription = "Favorite cat",
                                                    modifier = Modifier
                                                        .size(200.dp),
                                                    contentScale = ContentScale.Fit
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = favorite.name,
                                                    style = MaterialTheme.typography.titleLarge,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                        
                                        IconButton(
                                            onClick = {
                                                favorites = favorites.filter { it != favorite }
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove from favorites",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 