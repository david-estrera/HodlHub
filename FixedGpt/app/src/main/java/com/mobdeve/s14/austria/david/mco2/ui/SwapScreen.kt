package com.mobdeve.s14.austria.david.mco2.ui.theme

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

// Room Database Entity
@Entity(tableName = "coins")
data class Coin(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val amountOwned: Double
)

// DAO for accessing the database
@Dao
interface CoinDao {
    @Query("SELECT * FROM coins")
    suspend fun getAllCoins(): List<Coin>

    @Insert
    suspend fun insertCoin(coin: Coin)

    @Update
    suspend fun updateCoin(coin: Coin)

    @Delete
    suspend fun deleteCoin(coin: Coin)
}

// Room Database
@Database(entities = [Coin::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun coinDao(): CoinDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "coin-database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

@Composable
fun SwapScreen() {
    val context = LocalContext.current
    val database = AppDatabase.getInstance(context) // Get the database instance
    val coinDao = database.coinDao()
    val ownedCryptos = remember { mutableStateListOf<Coin>() }
    val convertedValues = remember { mutableStateMapOf<String, Double>() }
    var totalValue by remember { mutableStateOf(0.0) }
    val coroutineScope = rememberCoroutineScope()

    // Load coins from the database
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val coins = coinDao.getAllCoins()
            ownedCryptos.clear()
            ownedCryptos.addAll(coins)
        }
    }

    // Calculate converted values and total portfolio value
    LaunchedEffect(ownedCryptos) {
        coroutineScope.launch {
            val fetchedCoins = fetchCoinsFromApi()
            val prices = fetchedCoins.associate { it["id"] as String to it["current_price"] as Double }

            var sum = 0.0
            ownedCryptos.forEach { coin ->
                val price = prices[coin.name] ?: 0.0
                val converted = coin.amountOwned * price
                convertedValues[coin.name] = converted
                sum += converted
            }

            totalValue = sum
        }
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Total portfolio value
                Text(
                    text = "Total Value: ₱${"%.2f".format(totalValue)}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Portfolio heading
                Text(
                    text = "Your Portfolio",
                    fontSize = 32.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Portfolio list
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(ownedCryptos) { coin ->
                        val converted = convertedValues[coin.name] ?: 0.0
                        CryptoItem(
                            coin = coin,
                            convertedValue = converted,
                            onDelete = { selectedCoin ->
                                coroutineScope.launch {
                                    coinDao.deleteCoin(selectedCoin)
                                    ownedCryptos.remove(selectedCoin)
                                }
                            },
                            onEdit = { updatedCoin ->
                                coroutineScope.launch {
                                    coinDao.updateCoin(updatedCoin)
                                    val index = ownedCryptos.indexOfFirst { it.id == updatedCoin.id }
                                    if (index >= 0) {
                                        ownedCryptos[index] = updatedCoin
                                    }
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Add Coin Form
                AddCoinForm(coinDao, coroutineScope, ownedCryptos)
            }
        }
    }
}

@Composable
fun CryptoItem(
    coin: Coin,
    convertedValue: Double,
    onDelete: (Coin) -> Unit,
    onEdit: (Coin) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editAmount by remember { mutableStateOf(coin.amountOwned.toString()) }

    if (isEditing) {
        // Edit mode
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = coin.name, fontSize = 20.sp)
                OutlinedTextField(
                    value = editAmount,
                    onValueChange = { editAmount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.width(150.dp)
                )
            }
            Row {
                Button(
                    onClick = {
                        val updatedAmount = editAmount.toDoubleOrNull()
                        if (updatedAmount != null) {
                            onEdit(coin.copy(amountOwned = updatedAmount))
                            isEditing = false
                        }
                    },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Save")
                }
                Button(onClick = { isEditing = false }) {
                    Text("Cancel")
                }
            }
        }
    } else {
        // Display mode
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = coin.name, fontSize = 20.sp)
                Text(text = "Amount: ${coin.amountOwned}", fontSize = 16.sp)
            }
            Row {
                Text(text = "₱${"%.2f".format(convertedValue)}", fontSize = 16.sp)
                IconButton(onClick = { isEditing = true }) {
                    Text("✏️")
                }
                IconButton(onClick = { onDelete(coin) }) {
                    Text("🗑️")
                }
            }
        }
    }
}

@Composable
fun AddCoinForm(
    coinDao: CoinDao,
    coroutineScope: CoroutineScope,
    ownedCryptos: MutableList<Coin>
) {
    val coinOptions = remember { mutableStateListOf<Map<String, Any>>() }
    var selectedCoin by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var amountOwned by remember { mutableStateOf("") }

    // Fetch coin options dynamically from API
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val coins = fetchCoinsFromApi()
            coinOptions.clear()
            coinOptions.addAll(coins)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), // Overall padding for the form
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Button to add coin (placed at the top)
        Button(
            onClick = {
                if (selectedCoin != null && amountOwned.isNotEmpty()) {
                    val coin = Coin(
                        name = selectedCoin!!["id"] as String, // Save API-compatible `id`
                        amountOwned = amountOwned.toDoubleOrNull() ?: 0.0
                    )
                    coroutineScope.launch {
                        coinDao.insertCoin(coin)
                        ownedCryptos.add(coin)
                    }
                    selectedCoin = null
                    amountOwned = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp) // Add space below the button
        ) {
            Text("Add Coin")
        }

        // Dropdown for coin selection
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedCoin?.get("name")?.toString() ?: "",
                onValueChange = {},
                label = { Text("Select Coin") },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isDropdownExpanded = true }
            )
            DropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false }
            ) {
                coinOptions.forEach { coin ->
                    DropdownMenuItem(
                        onClick = {
                            selectedCoin = coin
                            isDropdownExpanded = false
                        },
                        text = {
                            Text("${coin["name"]} - ₱${coin["current_price"]}")
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp)) // Space between dropdown and text field

        // Input for amount owned
        OutlinedTextField(
            value = amountOwned,
            onValueChange = { amountOwned = it },
            label = { Text("Amount Owned") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}



// Fetch coins from CoinGecko API
suspend fun fetchCoinsFromApi(): List<Map<String, Any>> {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://api.coingecko.com/api/v3/coins/markets?vs_currency=php&order=market_cap_desc&per_page=50&page=1")
        .get()
        .addHeader("accept", "application/json")
        .build()

    return withContext(Dispatchers.IO) {
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val jsonArray = JSONArray(responseBody)
                    List(jsonArray.length()) { index ->
                        val jsonObject = jsonArray.getJSONObject(index)
                        mapOf(
                            "id" to jsonObject.getString("id"),
                            "name" to jsonObject.getString("name"),
                            "current_price" to jsonObject.getDouble("current_price")
                        )
                    }
                } else emptyList()
            } else emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
