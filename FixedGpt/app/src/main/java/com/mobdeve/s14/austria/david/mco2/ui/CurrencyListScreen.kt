package com.mobdeve.s14.austria.david.mco2.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

@Preview
@Composable
fun CurrencyListScreen() {
    val currencies = remember { mutableStateListOf<JSONObject>() }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch data when the composable is launched
    LaunchedEffect(Unit) {
        val fetchedCurrencies = fetchCurrencies()
        if (fetchedCurrencies != null) {
            currencies.addAll(fetchedCurrencies)
        }
        isLoading = false
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Top Currencies",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyColumn {
                        itemsIndexed(currencies) { _, currency ->
                            CurrencyItem(
                                name = currency.getString("name"),
                                ticker = currency.getString("symbol").uppercase(),
                                price = "₱${currency.getDouble("current_price")}",
                                change = "${"%.2f".format(currency.getDouble("price_change_percentage_24h"))}%",
                                isPositive = currency.getDouble("price_change_percentage_24h") > 0,
                                iconUrl = currency.getString("image")
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CurrencyItem(
    name: String,
    ticker: String,
    price: String,
    change: String,
    isPositive: Boolean,
    iconUrl: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Use Coil to load images from URLs
        Image(
            painter = rememberAsyncImagePainter(model = iconUrl),
            contentDescription = name,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, fontWeight = FontWeight.Bold)
            Text(text = ticker, fontSize = 12.sp, color = Color.Gray)
        }

        Text(text = price, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = change,
            color = if (isPositive) Color.Green else Color.Red
        )
    }
}

suspend fun fetchCurrencies(): List<JSONObject>? {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://api.coingecko.com/api/v3/coins/markets?vs_currency=php&order=market_cap_desc&per_page=100&page=1&price_change_percentage=24h")
        .get()
        .addHeader("accept", "application/json")
        .addHeader("x-cg-demo-api-key", "CG-Tar9xNxNrDqnQXXnKnMdCfda")
        .build()

    return withContext(Dispatchers.IO) {
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val jsonArray = JSONArray(responseBody)
                    List(jsonArray.length()) { index ->
                        jsonArray.getJSONObject(index)
                    }
                } else null
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
