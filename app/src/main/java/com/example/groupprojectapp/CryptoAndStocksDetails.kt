package com.example.groupprojectapp

import android.util.Log
import android.widget.Space
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
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
import androidx.compose.ui.res.painterResource
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun CryptoDetails(navController: NavController, cryptoName: String) {
    val viewModel: CryptoViewModel = viewModel()

    val priceHistory by viewModel.priceHistory.observeAsState(emptyList())

    val priceChanges by viewModel.additionalPriceChanges.observeAsState(emptyMap())

    var selectedInterval by remember { mutableStateOf("24h") }

    LaunchedEffect(cryptoName, selectedInterval) {
        viewModel.fetchPriceHistory(cryptoName, selectedInterval)
    }

    val item = items.find { it.name == cryptoName }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Text(
            text = "$cryptoName - Price History",
            fontSize = 22.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(50.dp))

        if (priceHistory.isEmpty()) {
            Text("No data available for the selected interval.", color = Color.Gray)
        } else {
            PriceChart(priceHistory, selectedInterval)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            TimeIntervalButton("24h", selectedInterval) { selectedInterval = "24h" }
            TimeIntervalButton("7d", selectedInterval) { selectedInterval = "7d" }
            TimeIntervalButton("1m", selectedInterval) { selectedInterval = "30d" }
            TimeIntervalButton("1y", selectedInterval) { selectedInterval = "1y" }
            TimeIntervalButton("Max", selectedInterval) { selectedInterval = "Max" }
        }

        Spacer(modifier = Modifier.height(100.dp))

        if (item != null) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(color = Color.White)
                    .padding(8.dp)
            ) {
                Image(
                    painter = painterResource(id = item.image),
                    contentDescription = item.name,
                    modifier = Modifier.weight(1f)
                        .height(100.dp)
                        .width(80.dp)
                )
                Column() {
                    Text("24h")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "${"%.2f".format(item.percentangeChange)}%",
                        color = if (item.percentangeChange >= 0) Color.Green else Color.Red,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text("7 days")
                    Spacer(modifier = Modifier.height(16.dp))
                    val change7d = priceChanges["${cryptoName}_7d"]
                    Text(
                        text = change7d?.let { "${"%.2f".format(it)}%" } ?: "N/A",
                        color = if (change7d != null && change7d >= 0) Color.Green else Color.Red,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text("1 month")
                    Spacer(modifier = Modifier.height(16.dp))
                    val change30d = priceChanges["${cryptoName}_30d"]
                    Text(
                        text = change30d?.let { "${"%.2f".format(it)}%" } ?: "N/A",
                        color = if (change30d != null && change30d >= 0) Color.Green else Color.Red,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text("1 year")
                    Spacer(modifier = Modifier.height(16.dp))
                    val change1y = priceChanges["${cryptoName}_1y"]
                    Text(
                        text = change1y?.let { "${"%.2f".format(it)}%" } ?: "N/A",
                        color = if (change1y != null && change1y >= 0) Color.Green else Color.Red,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}


@Composable
fun TimeIntervalButton(interval: String, selectedInterval: String, onIntervalChange: () -> Unit) {
    var interval1 = ""
    if (interval == "1m"){
        interval1 = "30d"
    }else{
       interval1 = interval
    }
    Button(
        onClick = {
            onIntervalChange()
        },
        modifier = Modifier
            .padding(4.dp)
            .background(Color.Transparent)
            .border(1.dp, Color.Gray),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Text(
            text = interval,
            color = if (selectedInterval == interval1) Color.Blue else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
fun PriceChart(priceHistory: List<HistoryItem>, interval: String) {

    var interval1 = ""
    if (interval == "1m"){
        interval1 = "30d"
    }else{
        interval1 = interval
    }

    Canvas(modifier = Modifier.fillMaxWidth().height(300.dp)) {
        val padding = 30f
        val chartWidth = size.width - 2 * padding
        val chartHeight = size.height - 2 * padding

        val maxPrice = priceHistory.maxByOrNull { it.price }?.price?.toFloat() ?: 0f
        val minPrice = priceHistory.minByOrNull { it.price }?.price?.toFloat() ?: 0f
        val priceRange = maxPrice - minPrice

        val maxTime = priceHistory.maxByOrNull { it.date }?.date?.toLong() ?: 0L
        val minTime = priceHistory.minByOrNull { it.date }?.date?.toLong() ?: 0L
        val timeRange = maxTime - minTime

        if (priceRange > 0) {
            val path = Path().apply {
                val firstItem = priceHistory.firstOrNull()
                if (firstItem != null) {
                    val firstX = padding
                    val firstY = chartHeight - (firstItem.price - minPrice) / priceRange * chartHeight
                    moveTo(firstX, firstY.toFloat())
                }
            }

            priceHistory.forEachIndexed { index, historyItem ->
                val x = padding + (index * chartWidth) / priceHistory.size
                val y = chartHeight - (historyItem.price - minPrice) / priceRange * chartHeight
                path.lineTo(x, y.toFloat())
            }

            drawPath(path, color = Color.Blue, style = Stroke(width = 2f))

            val priceStep = chartHeight / 5
            for (i in 0..5) {
                val priceLabel = (minPrice + i * (priceRange / 5)).toInt().toString()
                val yPos = chartHeight - (i * priceStep)
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 25f
                        textAlign = android.graphics.Paint.Align.RIGHT
                    }
                    canvas.nativeCanvas.drawText(priceLabel, padding - 10f, yPos, paint)
                }
            }

            fun formatDate(date: Long): String {
                return when (interval1) {
                    "24h" -> {
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = (date * 1000)
                        val hour = calendar.get(Calendar.HOUR_OF_DAY)
                        String.format("%02d:00", hour)
                    }
                    "7d", "30d" -> {
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = (date * 1000)
                        SimpleDateFormat("dd.MMM", Locale.getDefault()).format(calendar.time)
                    }
                    "1y" -> {
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = (date * 1000)
                        SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time)
                    }
                    "Max" -> {
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = (date * 1000)
                        SimpleDateFormat("yyyy", Locale.getDefault()).format(calendar.time)
                    }
                    else -> ""
                }
            }


            val timeStep = chartWidth / priceHistory.size

            for (i in priceHistory.indices step (priceHistory.size / 5)) {
                val timeLabel = formatDate(priceHistory[i].date)
                val xPos = padding + (i * timeStep)
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 25f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    canvas.nativeCanvas.drawText(timeLabel, xPos, chartHeight + 30f, paint)
                }
            }
        } else {
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.RED
                    textSize = 30f
                }
                canvas.nativeCanvas.drawText("Error: Invalid price range", padding, padding, paint)
            }
        }
    }
}


@Composable
fun StockDetails(navController: NavController, stockName: String) {
    val viewModel: StockViewModel = viewModel()

    val stockPriceHistory by viewModel.stockHistory.observeAsState(emptyList())

    // Intervalo seleccionado
    var selectedStockInterval by remember { mutableStateOf("1d") }

    LaunchedEffect(stockName) {
        viewModel.fetchStockHistory(stockName)
    }

    val intervalToFilterMap = mapOf(
        "24h" to 96,
        "7d" to 672,
        "30d" to 2880,
        "1y" to 35040,
        "Max" to Int.MAX_VALUE
    )

    val filteredStockPriceHistory = remember(stockPriceHistory, selectedStockInterval) {
        filterStockHistoryByInterval(stockPriceHistory, selectedStockInterval)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Text(
            text = "$stockName - Historical Prices",
            fontSize = 22.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (filteredStockPriceHistory.isEmpty()) {
            Text("No data available for the selected interval.", color = Color.Gray)
        } else {
            StockPriceChart(filteredStockPriceHistory, selectedStockInterval)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            intervalToFilterMap.keys.forEach { interval ->
                StockTimeIntervalButton(interval, selectedStockInterval) {
                    selectedStockInterval = interval
                }
            }
        }
    }
}

@Composable
fun StockPriceChart(priceHistory: List<TimeSeriesDaily>, interval: String) {
    Canvas(modifier = Modifier.fillMaxWidth().height(300.dp)) {
        val padding = 30f
        val chartWidth = size.width - 2 * padding
        val chartHeight = size.height - 2 * padding

        val maxPrice = priceHistory.maxByOrNull { it.close }?.close?.toFloat() ?: 0f
        val minPrice = priceHistory.minByOrNull { it.close }?.close?.toFloat() ?: 0f
        val priceRange = maxPrice - minPrice

        if (priceRange > 0) {
            val path = Path().apply {
                val firstItem = priceHistory.firstOrNull()
                if (firstItem != null) {
                    val firstX = padding
                    val firstY = chartHeight - (firstItem.close - minPrice) / priceRange * chartHeight
                    moveTo(firstX, firstY.toFloat())
                }
            }

            priceHistory.forEachIndexed { index, historyItem ->
                val x = padding + (index * chartWidth) / priceHistory.size
                val y = chartHeight - (historyItem.close - minPrice) / priceRange * chartHeight
                path.lineTo(x, y.toFloat())
            }

            drawPath(path, color = Color.Cyan, style = Stroke(width = 2f))

            val priceStep = chartHeight / 5
            for (i in 0..5) {
                val priceLabel = (minPrice + i * (priceRange / 5)).toInt().toString()
                val yPos = chartHeight - (i * priceStep)
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 25f
                        textAlign = android.graphics.Paint.Align.RIGHT
                    }
                    canvas.nativeCanvas.drawText(priceLabel, padding - 10f, yPos, paint)
                }
            }
        }
    }
}

@Composable
fun StockTimeIntervalButton(interval: String, selectedInterval: String, onIntervalChange: () -> Unit) {
    Button(
        onClick = { onIntervalChange() },
        modifier = Modifier
            .padding(4.dp)
            .border(1.dp, Color.Gray)
            .clip(RoundedCornerShape(8.dp)),
        colors = ButtonDefaults.buttonColors(containerColor = if (selectedInterval == interval) Color.Gray else Color.Black)
    ) {
        Text(
            text = interval,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

fun filterStockHistoryByInterval(fullHistory: List<TimeSeriesDaily>, interval: String): List<TimeSeriesDaily> {

    val intervalToFilterMap = mapOf(
        "24h" to 96,
        "7d" to 672,
        "30d" to 2880,
        "1y" to 35040,
        "Max" to Int.MAX_VALUE
    )

    val pointsToShow = intervalToFilterMap[interval] ?: Int.MAX_VALUE

    if (pointsToShow == Int.MAX_VALUE) return fullHistory

    val step = fullHistory.size / pointsToShow
    val filteredHistory = mutableListOf<TimeSeriesDaily>()

    for (i in 0 until pointsToShow) {
        val index = i * step
        if (index < fullHistory.size) {
            filteredHistory.add(fullHistory[index])
        }
    }

    return filteredHistory
}

fun parseDateToMillis(dateString: String): Long {
    return try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = dateFormat.parse(dateString)
        date?.time ?: 0L
    } catch (e: Exception) {
        Log.e("StockPriceChart", "Error al parsear la fecha: $dateString")
        0L
    }
}