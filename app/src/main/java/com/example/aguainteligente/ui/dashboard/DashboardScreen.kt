package com.example.aguainteligente.ui.dashboard

import android.Manifest
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aguainteligente.data.repository.AuthRepository
import com.example.aguainteligente.data.repository.FirestoreRepository
import com.example.aguainteligente.ui.theme.AguaInteligenteTheme
import com.example.aguainteligente.ui.theme.BlueDark
import com.example.aguainteligente.ui.theme.BlueElectric
import com.example.aguainteligente.ui.theme.GreenAccent
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

val Orange = Color(0xFFFFA500)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    onNavigateToTips: () -> Unit,
    dashboardViewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModelFactory(
            firestoreRepo = remember { FirestoreRepository() },
            authRepo = remember { AuthRepository() }
        )
    )
) {
    val currentUser = Firebase.auth.currentUser
    if (currentUser == null) {
        LaunchedEffect(Unit) { onLogout() }
        return
    }

    val dailyConsumption by dashboardViewModel.dailyConsumption.collectAsState()
    val weeklyConsumption by dashboardViewModel.weeklyConsumption.collectAsState()
    val monthlyConsumption by dashboardViewModel.monthlyConsumption.collectAsState()
    val previousMonthConsumption by dashboardViewModel.previousMonthConsumption.collectAsState()
    val monthlyHistory by dashboardViewModel.monthlyHistory.collectAsState()
    val peakConsumptionData by dashboardViewModel.peakConsumptionData.collectAsState()
    val isLoadingHistory by dashboardViewModel.isLoadingHistory.collectAsState()
    val historyError by dashboardViewModel.historyError.collectAsState()
    val currentFlowLiters by dashboardViewModel.currentFlowLiters.collectAsState()
    val leakAlert by dashboardViewModel.leakAlert.collectAsState()
    val valveState by dashboardViewModel.valveState.collectAsState()

    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            dashboardViewModel.initialize(context)
            dashboardViewModel.fetchConsumptionData()
        }
    )

    LaunchedEffect(key1 = true) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            dashboardViewModel.initialize(context)
            dashboardViewModel.fetchConsumptionData()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Agua Inteligente", color = Color.White, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BlueDark),
                actions = {
                    IconButton(onClick = onNavigateToTips) {
                        Icon(Icons.Default.Lightbulb, contentDescription = "Consejos", tint = Color.White)
                    }
                    IconButton(onClick = {
                        dashboardViewModel.logout()
                        onLogout()
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión", tint = Color.White)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Brush.verticalGradient(listOf(BlueDark, Color(0xFF0A183D)))),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                AnimatedVisibility(visible = true, enter = fadeIn(tween(1000))) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(12.dp, RoundedCornerShape(20.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(20.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(GreenAccent, BlueElectric)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (currentUser.displayName?.firstOrNull()
                                        ?: currentUser.email?.firstOrNull()
                                        ?: 'U').toString().uppercase(),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "¡Hola!",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                )
                                Text(
                                    text = currentUser.displayName
                                        ?: currentUser.email?.split("@")?.get(0)
                                        ?: "Usuario",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }

                ValveControlCard(
                    leakAlert = leakAlert,
                    valveState = valveState,
                    onValveStateChange = { isChecked ->
                        dashboardViewModel.setValveState(isChecked)
                    }
                )

                AnimatedConsumptionCard(currentFlowLiters)

                if (isLoadingHistory) {
                    LoadingCard()
                } else if (historyError != null) {
                    ErrorCard(historyError!!)
                } else {
                    HistoricalConsumptionCard(
                        dailyConsumption = dailyConsumption,
                        weeklyConsumption = weeklyConsumption,
                        monthlyConsumption = monthlyConsumption
                    )

                    MonthlyComparisonCard(
                        currentMonth = monthlyConsumption,
                        previousMonth = previousMonthConsumption,
                        monthlyHistory = monthlyHistory
                    )

                    PeakConsumptionCard(peakConsumptionData)
                }
            }
        }
    }
}

@Composable
fun ValveControlCard(
    leakAlert: Boolean,
    valveState: Boolean,
    onValveStateChange: (Boolean) -> Unit
) {
    val cardColor by animateColorAsState(
        targetValue = if (leakAlert) Color(0xFFFFE0E0) else MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        animationSpec = tween(500)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(visible = leakAlert) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Alerta de Fuga",
                        tint = Color.Red,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "¡Alerta de Fuga Detectada!",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                    )
                }
            }

            Text(
                "Control de Válvula Principal",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    if (valveState) "Válvula Abierta" else "Válvula Cerrada",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (valveState) GreenAccent else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(16.dp))
                Switch(
                    checked = valveState,
                    onCheckedChange = onValveStateChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = GreenAccent,
                        checkedTrackColor = GreenAccent.copy(alpha = 0.5f),
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
fun AnimatedConsumptionCard(currentFlow: Float) {
    val animatedFlow by animateFloatAsState(
        targetValue = currentFlow,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        listOf(
                            GreenAccent.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        radius = 300f
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "💧 Consumo del Intervalo",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "${"%.2f".format(animatedFlow)}",
                    style = MaterialTheme.typography.displayLarge.copy(
                        color = GreenAccent,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 72.sp
                    )
                )
                Text(
                    text = "LITROS",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = GreenAccent.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp
                    )
                )
            }
        }
    }
}

@Composable
fun HistoricalConsumptionCard(
    dailyConsumption: Float,
    weeklyConsumption: Float,
    monthlyConsumption: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    Icons.Default.Assessment,
                    contentDescription = null,
                    tint = BlueElectric,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Resumen de Consumo",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            EnhancedWaterConsumptionItem("📅 Hoy", dailyConsumption, GreenAccent)
            Spacer(modifier = Modifier.height(12.dp))
            EnhancedWaterConsumptionItem("📊 Esta Semana", weeklyConsumption, BlueElectric)
            Spacer(modifier = Modifier.height(12.dp))
            EnhancedWaterConsumptionItem("📈 Este Mes", monthlyConsumption, BlueDark)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "📊 Gráfica de Consumo",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            EnhancedBarChart(
                dailyConsumption = dailyConsumption,
                weeklyConsumption = weeklyConsumption,
                monthlyConsumption = monthlyConsumption
            )
        }
    }
}

@Composable
fun MonthlyComparisonCard(
    currentMonth: Float,
    previousMonth: Float,
    monthlyHistory: List<Pair<String, Float>>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "📈 Historial Mensual",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val difference = currentMonth - previousMonth
            val isIncreased = difference > 0

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Este mes", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${"%.1f".format(currentMonth)} L",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = BlueElectric
                        )
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Diferencia", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${if(isIncreased) "+" else ""}${"%.1f".format(difference)} L",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if(isIncreased) Color.Red else GreenAccent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            MonthlyHistoryChart(monthlyHistory)
        }
    }
}

@Composable
fun PeakConsumptionCard(peakData: Map<String, Float>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "⚡ Picos de Consumo",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            peakData.entries.forEachIndexed { index, (label, value) ->
                val colors = listOf(Color.Red, Orange, Color(0xFFFF6B35))
                EnhancedWaterConsumptionItem(label, value, colors[index % colors.size])
                if (index < peakData.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun EnhancedWaterConsumptionItem(label: String, value: Float, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            )
            Text(
                text = "${"%.2f".format(value)} L",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
        }
    }
}

@Composable
fun EnhancedBarChart(
    dailyConsumption: Float,
    weeklyConsumption: Float,
    monthlyConsumption: Float
) {
    val labels = listOf("Día", "Semana", "Mes")
    val values = listOf(dailyConsumption, weeklyConsumption, monthlyConsumption)
    val barColors = listOf(GreenAccent, BlueElectric, BlueDark)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Canvas(modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)) {
            val padding = 30.dp.toPx()
            val barWidth = (size.width - 2 * padding) / (values.size * 2f)
            val maxConsumption = values.maxOrNull()?.let { if (it > 0) it else 1f } ?: 1f


            values.forEachIndexed { index, value ->
                val barHeight = (value / maxConsumption) * (size.height - 3 * padding)
                val x = padding + index * (barWidth * 2f)
                val y = size.height - 2 * padding - barHeight

                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.1f),
                    topLeft = Offset(x + 4.dp.toPx(), y + 4.dp.toPx()),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(8.dp.toPx())
                )

                drawRoundRect(
                    color = barColors[index],
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(8.dp.toPx())
                )

                drawContext.canvas.nativeCanvas.apply {
                    val paint = Paint().apply {
                        this.color = android.graphics.Color.WHITE
                        textSize = 14.sp.toPx()
                        textAlign = Paint.Align.CENTER
                        typeface = Typeface.DEFAULT_BOLD
                    }
                    drawText(
                        "%.1f L".format(value),
                        x + barWidth / 2,
                        y - 8.dp.toPx(),
                        paint
                    )
                }

                drawContext.canvas.nativeCanvas.apply {
                    val paint = Paint().apply {
                        this.color = android.graphics.Color.WHITE
                        textSize = 12.sp.toPx()
                        textAlign = Paint.Align.CENTER
                        typeface = Typeface.DEFAULT_BOLD
                    }
                    drawText(
                        labels[index],
                        x + barWidth / 2,
                        size.height - padding + 20.dp.toPx(),
                        paint
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlyHistoryChart(monthlyHistory: List<Pair<String, Float>>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Canvas(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {
            val padding = 20.dp.toPx()
            val maxValue = monthlyHistory.maxOfOrNull { it.second }?.let { if (it > 0) it else 1f } ?: 1f
            val stepX = if (monthlyHistory.size > 1) (size.width - 2 * padding) / (monthlyHistory.size - 1) else 0f

            if (monthlyHistory.size > 1) {
                val path = Path()
                monthlyHistory.forEachIndexed { index, (_, value) ->
                    val x = padding + index * stepX
                    val y = size.height - padding - (value / maxValue) * (size.height - 2 * padding)

                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = BlueElectric,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            monthlyHistory.forEachIndexed { index, (label, value) ->
                val x = padding + index * stepX
                val y = size.height - padding - (value / maxValue) * (size.height - 2 * padding)

                drawCircle(
                    color = BlueElectric,
                    radius = 6.dp.toPx(),
                    center = Offset(x, y)
                )

                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = Offset(x, y)
                )

                drawContext.canvas.nativeCanvas.apply {
                    val textPaintLabel = Paint().apply {
                        this.color = android.graphics.Color.WHITE
                        textSize = 10.sp.toPx()
                        textAlign = Paint.Align.CENTER
                    }
                    val textPaintValue = Paint().apply {
                        this.color = android.graphics.Color.WHITE
                        textSize = 10.sp.toPx()
                        textAlign = Paint.Align.CENTER
                        typeface = Typeface.DEFAULT_BOLD
                    }

                    drawText(
                        label,
                        x,
                        size.height,
                        textPaintLabel
                    )

                    drawText(
                        "%.0f".format(value),
                        x,
                        y - 10.dp.toPx(),
                        textPaintValue
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(12.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Cargando datos...",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ErrorCard(errorMessage: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(12.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "¡Oh no! Ha ocurrido un error:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    AguaInteligenteTheme {
        DashboardScreen(
            onLogout = {},
            onNavigateToTips = {},
        )
    }
}