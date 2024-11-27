package com.example.groupprojectapp

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun CryptoDetails(navController: NavController, cryptoName: String) {
    val viewModel: CryptoViewModel = viewModel()

    // LiveData para historial de precios
    val priceHistory by viewModel.priceHistory.observeAsState(emptyList())

    // Intervalo seleccionado, por defecto es "1d"
    var selectedInterval by remember { mutableStateOf("1d") }

    // Llamar la función para obtener el historial con el intervalo seleccionado
    LaunchedEffect(cryptoName, selectedInterval) {
        viewModel.fetchPriceHistory(cryptoName, selectedInterval) // Aquí se recarga el historial de precios con el nuevo intervalo
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
    ) {
        // Título con nombre de la criptomoneda
        Text(
            text = "$cryptoName - Price History",
            fontSize = 22.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Gráfico de precios (usando Canvas)
        if (priceHistory.isEmpty()) {
            Text("No data available for the selected interval.", color = Color.Gray)
        } else {
            // Pasar el intervalo seleccionado como parámetro a la función PriceChart
            PriceChart(priceHistory, selectedInterval)
        }

        // Selector de intervalos de tiempo (24h, 7d, etc.)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            // Botones sin color azul
            TimeIntervalButton("24h", selectedInterval) { selectedInterval = "24h" }
            TimeIntervalButton("7d", selectedInterval) { selectedInterval = "7d" }
            TimeIntervalButton("30d", selectedInterval) { selectedInterval = "30d" }
            TimeIntervalButton("1y", selectedInterval) { selectedInterval = "1y" }
            TimeIntervalButton("Max", selectedInterval) { selectedInterval = "max" }
        }
    }
}

@Composable
fun TimeIntervalButton(interval: String, selectedInterval: String, onIntervalChange: () -> Unit) {
    Button(
        onClick = {
            onIntervalChange() // Actualiza el intervalo seleccionado
        },
        modifier = Modifier
            .padding(4.dp)
            .background(Color.Transparent) // Elimina el fondo azul
            .border(1.dp, Color.Gray), // Agrega un borde gris
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Text(
            text = interval,
            color = if (selectedInterval == interval) Color.Blue else Color.Black, // Resaltar el botón seleccionado
            fontWeight = FontWeight.Bold
        )
    }
}
@Composable
fun PriceChart(priceHistory: List<HistoryItem>, interval: String) {
    // Crea el lienzo donde se dibujará el gráfico
    Canvas(modifier = Modifier.fillMaxWidth().height(250.dp)) { // Hacemos la gráfica más pequeña
        val padding = 16f
        val chartWidth = size.width - 2 * padding
        val chartHeight = size.height - 2 * padding

        // Calcular el máximo y mínimo de los precios para escalar el gráfico
        val maxPrice = priceHistory.maxByOrNull { it.price }?.price?.toFloat() ?: 0f
        val minPrice = priceHistory.minByOrNull { it.price }?.price?.toFloat() ?: 0f
        val priceRange = maxPrice - minPrice

        // Calcular el rango de fechas para el eje X
        val maxTime = priceHistory.maxByOrNull { it.date }?.date?.toLong() ?: 0L
        val minTime = priceHistory.minByOrNull { it.date }?.date?.toLong() ?: 0L
        val timeRange = maxTime - minTime

        // Verificamos si el rango de precios es mayor que cero
        if (priceRange > 0) {
            val path = Path().apply {
                // Escalar la primera coordenada (primer punto)
                val firstItem = priceHistory.firstOrNull()
                if (firstItem != null) {
                    // Escalar el primer punto
                    val firstX = padding
                    val firstY = chartHeight - (firstItem.price - minPrice) / priceRange * chartHeight
                    moveTo(firstX, firstY.toFloat())
                }
            }

            // Dibujar la línea que conecta todos los puntos
            priceHistory.forEachIndexed { index, historyItem ->
                val x = padding + (index * chartWidth) / priceHistory.size  // Escalar el tiempo
                val y = chartHeight - (historyItem.price - minPrice) / priceRange * chartHeight  // Escalar el precio
                path.lineTo(x, y.toFloat())
            }

            // Dibujar la línea en el gráfico
            drawPath(path, color = Color.Black, style = Stroke(width = 2f)) // Línea negra

            // Dibujar los ejes Y (precio)
            val priceStep = chartHeight / 5
            for (i in 0..5) {
                val priceLabel = (minPrice + i * (priceRange / 5)).toInt().toString()  // Precio etiquetado
                val yPos = chartHeight - (i * priceStep)
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 30f  // Tamaño de texto
                        textAlign = android.graphics.Paint.Align.RIGHT  // Alineación del texto
                    }
                    // Dibujar el valor del precio en el eje Y
                    canvas.nativeCanvas.drawText(priceLabel, padding - 10f, yPos, paint)
                }
            }

            // Función para formatear las fechas según el intervalo
            fun formatDate(date: Long): String {
                return when (interval) {
                    "24h" -> {
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = (date * 1000).toLong() // Convertimos de UNIX timestamp
                        val hour = calendar.get(Calendar.HOUR_OF_DAY)
                        val minute = calendar.get(Calendar.MINUTE)
                        String.format("%02d:%02d", hour, minute) // Ejemplo: 08:00
                    }
                    "7d", "30d" -> {
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = (date * 1000).toLong() // Convertimos de UNIX timestamp
                        SimpleDateFormat("dd.MMM", Locale.getDefault()).format(calendar.time) // Ejemplo: 25.Nov
                    }
                    "1y" -> {
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = (date * 1000).toLong() // Convertimos de UNIX timestamp
                        SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time) // Ejemplo: Ene, Feb
                    }
                    "max" -> {
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = (date * 1000).toLong() // Convertimos de UNIX timestamp
                        SimpleDateFormat("yyyy", Locale.getDefault()).format(calendar.time) // Ejemplo: 2018
                    }
                    else -> "" // Si no coincide con ningún intervalo, no mostramos fecha
                }
            }

            // Dibujar las fechas en el eje X
            val timeStep = chartWidth / priceHistory.size
            for (i in priceHistory.indices step (priceHistory.size / 5)) {
                val timeLabel = formatDate(priceHistory[i].date)
                val xPos = padding + (i * chartWidth) / priceHistory.size
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 30f  // Tamaño de texto
                        textAlign = android.graphics.Paint.Align.CENTER  // Alineación del texto
                    }
                    // Dibujar el valor de la fecha en el eje X
                    canvas.nativeCanvas.drawText(timeLabel, xPos, chartHeight + 30f, paint)
                }
            }
        } else {
            // Si el rango de precios es cero, no intentamos dibujar el gráfico
            Log.e("PriceChart", "Price range is zero, unable to render chart.")
        }
    }
}