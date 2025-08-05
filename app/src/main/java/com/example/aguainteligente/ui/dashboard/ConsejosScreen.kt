package com.example.aguainteligente.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Shower
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aguainteligente.ui.theme.AguaInteligenteTheme
import com.example.aguainteligente.ui.theme.BlueDark
import com.example.aguainteligente.ui.theme.BlueElectric
import com.example.aguainteligente.ui.theme.GreenAccent
import kotlinx.coroutines.delay

data class ConsejoItem(
    val categoria: String,
    val titulo: String,
    val descripcion: String,
    val ahorroEstimado: String,
    val icono: ImageVector,
    val color: Color,
    val tips: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsejosScreen(
    onNavigateBack: () -> Unit
) {
    val consejos = remember {
        listOf(
            ConsejoItem(
                categoria = "Baño",
                titulo = "Optimiza tu ducha",
                descripcion = "Reducir el tiempo de ducha es una de las formas más efectivas de ahorrar agua",
                ahorroEstimado = "Hasta 150L/día",
                icono = Icons.Default.Shower,
                color = BlueElectric,
                tips = listOf(
                    "Reduce el tiempo de ducha a 5-7 minutos máximo",
                    "Instala regaderas de bajo flujo (6-8 L/min)",
                    "Cierra el agua mientras te enjabonas",
                    "Usa agua fría o tibia en lugar de caliente",
                    "Considera duchas de mano para mayor control"
                )
            ),
            ConsejoItem(
                categoria = "Cocina",
                titulo = "Cocina eficientemente",
                descripcion = "Pequeños cambios en la cocina pueden generar grandes ahorros",
                ahorroEstimado = "Hasta 80L/día",
                icono = Icons.Default.Kitchen,
                color = GreenAccent,
                tips = listOf(
                    "No dejes correr el agua al lavar platos",
                    "Usa el lavavajillas solo cuando esté lleno",
                    "Lava frutas y verduras en un recipiente",
                    "Reutiliza el agua de cocción para regar plantas",
                    "Repara goteras inmediatamente"
                )
            ),
            ConsejoItem(
                categoria = "WC/Inodoro",
                titulo = "Uso inteligente del WC",
                descripcion = "El inodoro consume hasta 30% del agua doméstica",
                ahorroEstimado = "Hasta 100L/día",
                icono = Icons.Default.Home,
                color = Color(0xFF9C27B0),
                tips = listOf(
                    "Instala inodoros de doble descarga",
                    "No uses el WC como basurero",
                    "Coloca una botella con agua en tanques antiguos",
                    "Revisa y repara fugas en el mecanismo",
                    "Considera inodoros de bajo consumo (4.5L por descarga)"
                )
            ),
            ConsejoItem(
                categoria = "Jardín",
                titulo = "Riego inteligente",
                descripcion = "Mantén tu jardín hermoso usando menos agua",
                ahorroEstimado = "Hasta 200L/día",
                icono = Icons.Default.LocalFlorist,
                color = Color(0xFF4CAF50),
                tips = listOf(
                    "Riega temprano en la mañana o al atardecer",
                    "Usa sistemas de riego por goteo",
                    "Planta especies nativas que requieren menos agua",
                    "Aplica mulch para retener humedad",
                    "Recoge agua de lluvia para riego"
                )
            ),
            ConsejoItem(
                categoria = "Lavandería",
                titulo = "Lava de forma eficiente",
                descripcion = "Optimiza el uso de tu lavadora para maximum ahorro",
                ahorroEstimado = "Hasta 60L/día",
                icono = Icons.Default.Build,
                color = Color(0xFFFF9800),
                tips = listOf(
                    "Usa la lavadora solo con carga completa",
                    "Selecciona el nivel de agua apropiado",
                    "Usa agua fría cuando sea posible",
                    "Considera lavadoras de alta eficiencia",
                    "Reutiliza agua de enjuague para limpieza"
                )
            ),
            ConsejoItem(
                categoria = "Hábitos",
                titulo = "Cambios de rutina",
                descripcion = "Pequeños hábitos diarios que marcan la diferencia",
                ahorroEstimado = "Hasta 120L/día",
                icono = Icons.Default.Schedule,
                color = Color(0xFFE91E63),
                tips = listOf(
                    "Cierra el grifo al cepillarte los dientes",
                    "Toma duchas en lugar de baños de tina",
                    "Llena una jarra de agua para beber",
                    "Revisa y repara goteras semanalmente",
                    "Educa a toda la familia sobre el ahorro"
                )
            ),
            ConsejoItem(
                categoria = "Tecnología",
                titulo = "Mejoras tecnológicas",
                descripcion = "Invierte en tecnología para ahorrar a largo plazo",
                ahorroEstimado = "Hasta 300L/día",
                icono = Icons.Default.Security,
                color = Color(0xFF607D8B),
                tips = listOf(
                    "Instala sensores de flujo para detectar fugas",
                    "Usa grifos con sensores automáticos",
                    "Considera sistemas de recirculación de agua",
                    "Instala medidores inteligentes",
                    "Utiliza aplicaciones de monitoreo como esta"
                )
            )
        )
    }

    var visibleItems by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        repeat(consejos.size) { index ->
            delay(150L * index)
            visibleItems = index + 1
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Consejos de Ahorro",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BlueDark
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        listOf(BlueDark, Color(0xFF0A183D))
                    )
                )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
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
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "💧 ¡Ahorra Agua!",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BlueElectric
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Implementa estos consejos y reduce tu consumo hasta un 40%",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center
                                )
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                InfoChip("💰 Ahorra dinero", GreenAccent)
                                InfoChip("🌍 Cuida el planeta", BlueElectric)
                            }
                        }
                    }
                }

                itemsIndexed(consejos) { index, consejo ->
                    AnimatedVisibility(
                        visible = index < visibleItems,
                        enter = slideInVertically(
                            animationSpec = tween(durationMillis = 500),
                            initialOffsetY = { it }
                        ) + fadeIn(animationSpec = tween(durationMillis = 500))
                    ) {
                        ConsejoCard(consejo = consejo)
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(12.dp, RoundedCornerShape(20.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = GreenAccent.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "🎯 ¡Tu Puedes Hacerlo!",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GreenAccent
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Cada gota cuenta. Implementa estos consejos gradualmente y verás la diferencia en tu consumo y factura de agua.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConsejoCard(consejo: ConsejoItem) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(16.dp),
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    modifier = Modifier.size(50.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = consejo.color.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            consejo.icono,
                            contentDescription = null,
                            tint = consejo.color,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        consejo.categoria.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = consejo.color,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        consejo.titulo,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = GreenAccent.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        consejo.ahorroEstimado,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = GreenAccent,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                consejo.descripcion,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(tween(300)) + slideInVertically(tween(300)),
                exit = fadeOut(tween(300)) + slideOutVertically(tween(300))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    Divider(
                        color = consejo.color.copy(alpha = 0.3f),
                        thickness = 1.dp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "💡 Tips específicos:",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = consejo.color
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    consejo.tips.forEach { tip ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                "•",
                                color = consejo.color,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(end = 8.dp, top = 2.dp)
                            )
                            Text(
                                tip,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                if (isExpanded) "👆 Toca para contraer" else "👆 Toca para ver tips específicos",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun InfoChip(text: String, color: Color) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium.copy(
                color = color,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewConsejosScreen() {
    AguaInteligenteTheme {
        ConsejosScreen(onNavigateBack = {})
    }
}