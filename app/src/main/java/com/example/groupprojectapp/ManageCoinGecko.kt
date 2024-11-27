package com.example.groupprojectapp

import android.telecom.Call
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class CoinPrice(
    val usd: Double,
    @SerializedName("usd_24h_change") val change24h: Double
)

data class HistoryItem(val date: Long, val price: Double)  // Cambié el tipo de `date` a Long

data class MarketChartResponse(
    val prices: List<List<Double>> // Cada lista tiene [timestamp, price]
)

val coinGeckoIds = mapOf(
    "Bitcoin" to "bitcoin",
    "Ethereum" to "ethereum",
    "Ripple" to "ripple",
    "Litecoin" to "litecoin",
    "Cardano" to "cardano",
    "Polkadot" to "polkadot",
    "Solana" to "solana",
    "Dogecoin" to "dogecoin",
    "Shiba Inu" to "shiba-inu",
    "Binance Coin" to "binancecoin"
)

interface CoinGeckoApi {
    @GET("simple/price")
    suspend fun getCryptoPrice(
        @Query("ids") id: String,
        @Query("vs_currencies") vsCurrencies: String = "usd"
    ): Response<Map<String, CoinPrice>>

    @GET("coins/{id}/market_chart")
    suspend fun getCryptoHistory(
        @Path("id") id: String,
        @Query("vs_currency") currency: String = "usd",
        @Query("days") days: String = "1"
    ): Response<MarketChartResponse>
}

object RetrofitInstance {
    private const val BASE_URL = "https://api.coingecko.com/api/v3/"

    val api: CoinGeckoApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CoinGeckoApi::class.java)
    }
}

val stockSymbols = mapOf(
    "Apple" to "AAPL",       // Apple Inc.
    "Tesla" to "TSLA",       // Tesla Inc.
    "Amazon" to "AMZN",      // Amazon.com, Inc.
    "Google" to "GOOGL",     // Alphabet Inc. (Google)
    "Microsoft" to "MSFT",   // Microsoft Corporation
    "Meta" to "META",        // Meta Platforms, Inc. (anteriormente Facebook)
    "NVIDIA" to "NVDA",      // NVIDIA Corporation
    "AMD" to "AMD",          // Advanced Micro Devices, Inc.
    "Intel" to "INTC",       // Intel Corporation
    "Netflix" to "NFLX",     // Netflix, Inc.
    "Spotify" to "SPOT",     // Spotify Technology S.A.
    "Salesforce" to "CRM",   // Salesforce, Inc.
    "Oracle" to "ORCL",      // Oracle Corporation
    "Shopify" to "SHOP",     // Shopify Inc.
    "X" to "X",              // X Corp. (anteriormente Twitter)
)

data class StockQuote(
    val c: Double,  // Precio actual
    val d: Double,  // Cambio absoluto
    val dp: Double, // Cambio porcentual
    val h: Double,  // Máximo del día
    val l: Double,  // Mínimo del día
    val o: Double,  // Precio de apertura
    val pc: Double  // Precio de cierre anterior
)

// Interfaz de Retrofit para la solicitud a Finnhub
interface FinnhubApi {

    @GET("quote")
    suspend fun getStockQuote(
        @Query("symbol") symbol: String,
        @Query("token") apiKey: String
    ): Response<StockQuote>
}

// Instancia de Retrofit configurada para Finnhub
object RetrofitInstanceStocks {
    private const val BASE_URL = "https://finnhub.io/api/v1/"

    val api: FinnhubApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FinnhubApi::class.java)
    }
}