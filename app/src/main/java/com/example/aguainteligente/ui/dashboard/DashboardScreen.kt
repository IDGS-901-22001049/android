package com.example.aguainteligente.ui.dashboard

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aguainteligente.data.repository.AuthRepository
import com.example.aguainteligente.data.repository.FirestoreRepository
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
    val flowRate by dashboardViewModel.currentFlowRate.collectAsState()
    val valveState by dashboardViewModel.valveState.collectAsState()

    val context = LocalContext.current
    LaunchedEffect(key1 = true) {
        dashboardViewModel.initialize(context)
        dashboardViewModel.fetchConsumptionData()
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
                            .shadow(8.dp, RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        ),
                        shape = RoundedCornerShape(24.dp)
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
                    valveState = valveState,
                    onValveStateChange = { dashboardViewModel.toggleValve() }
                )

                CurrentFlowCard(flowRate = flowRate)

                DispenseCard(viewModel = dashboardViewModel)

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
    valveState: Boolean,
    onValveStateChange: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Válvula Principal",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Button(
                onClick = onValveStateChange,
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (valveState) Color.Red.copy(alpha = 0.8f) else GreenAccent
                )
            ) {
                AnimatedContent(targetState = valveState, transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                }) { isOpen ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isOpen) Icons.Default.PowerSettingsNew else Icons.Default.PowerSettingsNew,
                            contentDescription = if (isOpen) "Cerrar" else "Abrir"
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (isOpen) "CERRAR" else "ABRIR", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CurrentFlowCard(flowRate: Float) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WaterDrop, contentDescription = "Flujo", tint = BlueElectric)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Flujo Actual",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "%.2f".format(flowRate),
                style = MaterialTheme.typography.displayLarge.copy(
                    color = BlueElectric,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 72.sp
                )
            )
            Text(
                "LITROS / MINUTO",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = BlueElectric.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )
            )
        }
    }
}

@Composable
fun DispenseCard(viewModel: DashboardViewModel) {
    var amountToDispense by remember { mutableStateOf("") }
    val totalToDispense by viewModel.dispenseAmount.collectAsState()
    val dispensedProgress by viewModel.dispensedProgress.collectAsState()
    val isDispensing by remember { derivedStateOf { totalToDispense > 0f } }

    val progress = if (totalToDispense > 0) (dispensedProgress / totalToDispense).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(500))

    Card(
        modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Dispensador de Agua",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            AnimatedContent(targetState = isDispensing, transitionSpec = {
                fadeIn(animationSpec = tween(300)) + scaleIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300)) + scaleOut(animationSpec = tween(300))
            }) { dispensing ->
                if (dispensing) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Dispensando %.2f / %.2f Litros".format(dispensedProgress, totalToDispense),
                            style = MaterialTheme.typography.bodyLarge)
                        LinearProgressIndicator(
                            progress = animatedProgress,
                            modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                            color = GreenAccent,
                            trackColor = GreenAccent.copy(alpha = 0.2f)
                        )
                        Button(
                            onClick = { viewModel.cancelDispensing() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("CANCELAR", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = amountToDispense,
                            onValueChange = { amountToDispense = it },
                            label = { Text("Cantidad en Litros (ej: 0.25)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                        ) {
                            OutlinedButton(onClick = { viewModel.startDispensing(0.2f) }) { Text("Vaso") }
                            OutlinedButton(onClick = { viewModel.startDispensing(1.5f) }) { Text("Botella") }
                            OutlinedButton(onClick = { viewModel.startDispensing(0.05f) }) { Text("Dientes") }
                        }

                        Button(
                            onClick = {
                                val liters = amountToDispense.toFloatOrNull()
                                if (liters != null) {
                                    viewModel.startDispensing(liters)
                                }
                            },
                            enabled = amountToDispense.toFloatOrNull() != null,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("INICIAR DISPENSADO", fontWeight = FontWeight.Bold)
                        }
                    }
                }
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
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Icon(
                    Icons.Default.Leaderboard,
                    contentDescription = null,
                    tint = BlueElectric,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Resumen de Consumo",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            EnhancedWaterConsumptionItem("📅 Hoy", dailyConsumption, GreenAccent)
            Spacer(modifier = Modifier.height(12.dp))
            EnhancedWaterConsumptionItem("📊 Esta Semana", weeklyConsumption, BlueElectric)
            Spacer(modifier = Modifier.height(12.dp))
            EnhancedWaterConsumptionItem("📈 Este Mes", monthlyConsumption, BlueDark)
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
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 20.dp)) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = BlueElectric, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Historial Mensual",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                )
            }

            val difference = currentMonth - previousMonth
            val isIncreased = difference > 0

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Este mes", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${"%.1f".format(currentMonth)} L",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = BlueElectric)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isIncreased) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = "Diferencia",
                        tint = if(isIncreased) Color.Red else GreenAccent
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${"%.1f".format(kotlin.math.abs(difference))} L",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if(isIncreased) Color.Red else GreenAccent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            MonthlyHistoryChart(monthlyHistory)
        }
    }
}

@Composable
fun PeakConsumptionCard(peakData: Map<String, Float>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 20.dp)) {
                Icon(Icons.Default.Bolt, contentDescription = null, tint = Orange, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Picos de Consumo",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
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

@Composable
fun MonthlyHistoryChart(monthlyHistory: List<Pair<String, Float>>) {
    val textColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f).toArgb()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val (points, labels) = monthlyHistory.map { it.second } to monthlyHistory.map { it.first }
            if (points.isEmpty()) return@Canvas

            val maxValue = points.maxOrNull() ?: 0f
            val path = Path()
            val stepX = size.width / (points.size - 1).coerceAtLeast(1)

            points.forEachIndexed { index, point ->
                val x = index * stepX
                val y = size.height - (point / maxValue) * size.height
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(BlueElectric, BlueElectric.copy(alpha = 0.3f))
                ),
                style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
            )

            val textPaint = Paint().apply {
                color = textColor
                textSize = 12.sp.toPx()
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            labels.forEachIndexed { index, label ->
                val x = index * stepX
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    x,
                    size.height,
                    textPaint
                )
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
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
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
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.CloudOff,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "¡Oh no! Ha ocurrido un error:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
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