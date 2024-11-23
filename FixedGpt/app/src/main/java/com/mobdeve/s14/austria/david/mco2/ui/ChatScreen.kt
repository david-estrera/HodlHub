import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

@Composable
fun ChatScreen(onClose: () -> Unit) {
    var userInput by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf("Bot: Hi there! How can I help you?") }
    val scope = rememberCoroutineScope()

    fun sendMessageToChatGPT(message: String) {
        val apiKey = "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" // Replace with secure API key loading
        val url = "https://api.openai.com/v1/chat/completions"

        val client = OkHttpClient()

        val jsonBody = JSONObject().apply {
            put("model", "gpt-4o-mini")
            put(
                "messages", JSONArray(
                    listOf(
                        mapOf("role" to "system", "content" to "You are a helpful assistant in crypto."),
                        mapOf("role" to "user", "content" to message)
                    )
                )
            )
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                scope.launch {
                    messages.add("Bot: Failed to fetch response. Error: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (!responseBody.isNullOrBlank()) {
                        try {
                            val jsonResponse = JSONObject(responseBody)
                            val botReply = jsonResponse
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content")

                            scope.launch {
                                messages.add("Bot: $botReply")
                            }
                        } catch (e: Exception) {
                            scope.launch {
                                messages.add("Bot: Failed to parse response. Error: ${e.message}")
                            }
                        }
                    } else {
                        scope.launch {
                            messages.add("Bot: Empty response from server.")
                        }
                    }
                } else {
                    if (response.code == 429) {
                        scope.launch {
                            messages.add("Bot: (Code 429) Rate limit exceeded. Retrying...")
                            delay(5000L) // Backoff before retry
                            sendMessageToChatGPT(message)
                        }
                    } else {
                        scope.launch {
                            messages.add("Bot: API call failed with status: ${response.code}")
                        }
                    }
                }
            }
        })
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 850.dp)
            .background(Color.LightGray.copy(alpha = 0.9f))
            .padding(bottom = 50.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color.White, shape = MaterialTheme.shapes.medium)
                .padding(16.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Chat Bot", fontSize = 24.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 16.dp)
            ) {
                items(messages) { message ->
                    Text(
                        text = message,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .padding(vertical = 30.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(onClick = {
                    if (userInput.isNotBlank()) {
                        messages.add("User: $userInput")
                        sendMessageToChatGPT(userInput)
                        userInput = ""
                    }
                }) {
                    Text("Send")
                }
            }
        }

        Button(
            onClick = { onClose() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Text("Close Chat")
        }
    }
}
