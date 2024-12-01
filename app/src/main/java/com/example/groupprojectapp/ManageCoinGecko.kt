package com.example.groupprojectapp

import android.telecom.Call
import android.util.Log
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import okhttp3.Request

data class CoinPriceResponse(
    val usd: Double
)

data class CoinMarketResponse(
    val id: String,
    val name: String,
    val current_price: Double,
    val price_change_percentage_24h: Double
)

data class HistoryItem(
    val date: Long,
    val price: Double
)

data class MarketChartResponse(
    val prices: List<List<Double>>
)

data class CryptoHistoryResponse(
    val prices: List<List<Double>>,
    val market_caps: List<List<Double>>,
    val total_volumes: List<List<Double>>
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
        @Query("ids") ids: String,
        @Query("vs_currencies") vsCurrency: String = "usd"
    ): Response<Map<String, CoinPriceResponse>>


    @GET("coins/markets")
    suspend fun getCryptoPriceChange(
        @Query("ids") ids: String,
        @Query("vs_currency") vsCurrency: String = "usd"
    ): Response<List<CoinMarketResponse>>

    @GET("coins/{id}/market_chart")
    suspend fun getCryptoHistory(
        @Path("id") id: String,
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("days") days: String = "1"
    ): Response<CryptoHistoryResponse>
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
    "Apple" to "AAPL",
    "Tesla" to "TSLA",
    "Amazon" to "AMZN",
    "Google" to "GOOGL",
    "Microsoft" to "MSFT",
    "Meta" to "META",
    "NVIDIA" to "NVDA",
    "AMD" to "AMD",
    "Intel" to "INTC",
    "Netflix" to "NFLX",
    "Spotify" to "SPOT",
    "Salesforce" to "CRM",
    "Oracle" to "ORCL",
    "Shopify" to "SHOP",
    "X" to "X",
)

data class StockQuote(
    val c: Double,
    val d: Double,
    val dp: Double,
    val h: Double,
    val l: Double,
    val o: Double,
    val pc: Double
)


interface FinnhubApi {

    @GET("quote")
    suspend fun getStockQuote(
        @Query("symbol") symbol: String,
        @Query("token") apiKey: String
    ): Response<StockQuote>
}

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

data class TimeSeriesDaily(
    val date: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
)

interface AlphaVantageApi {
    @GET("query")
    suspend fun getHistoricalData(
        @Query("function") function: String,
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String,
        @Query("interval") interval: String
    ): Response<Map<String, Any>>
}

object RetrofitClient {
    private const val BASE_URL = "https://www.alphavantage.co/"

    val apiService: AlphaVantageApi by lazy {
        val client = OkHttpClient.Builder().build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AlphaVantageApi::class.java)
    }
}