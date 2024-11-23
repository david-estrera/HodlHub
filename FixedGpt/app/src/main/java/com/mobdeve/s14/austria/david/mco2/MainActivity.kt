package com.mobdeve.s14.austria.david.mco2

    import ChatScreen
    import android.content.Context
    import android.os.Bundle
    import android.util.Log
    import android.widget.Toast
    import androidx.activity.ComponentActivity
    import androidx.activity.compose.setContent
    import androidx.compose.foundation.BorderStroke
    import androidx.compose.foundation.Image
    import androidx.compose.foundation.background
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material3.Button
    import androidx.compose.material3.ButtonDefaults
    import androidx.compose.material3.CircularProgressIndicator
    import androidx.compose.material3.Scaffold
    import androidx.compose.material3.NavigationBar
    import androidx.compose.material3.NavigationBarItem
    import androidx.compose.material3.Icon
    import androidx.compose.material3.OutlinedTextField
    import androidx.compose.material3.Text
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.remember
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.res.painterResource
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.rememberCoroutineScope
    import androidx.compose.runtime.setValue
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.tooling.preview.Preview
    import androidx.room.Dao
    import androidx.room.Database
    import androidx.room.Entity
    import androidx.room.Insert
    import androidx.room.PrimaryKey
    import androidx.room.Query
    import androidx.room.Room
    import androidx.room.RoomDatabase
    import com.mobdeve.s14.austria.david.mco2.ui.theme.CurrencyListScreen
    import com.mobdeve.s14.austria.david.mco2.ui.theme.SwapScreen
    import com.mobdeve.s14.austria.david.mco2.ui.theme.*
    import io.github.jan.supabase.SupabaseClient
    import io.github.jan.supabase.auth.Auth
    import io.github.jan.supabase.auth.auth
    import io.github.jan.supabase.auth.providers.Google
    import io.github.jan.supabase.auth.providers.builtin.IDToken
    import io.github.jan.supabase.createSupabaseClient
    import io.github.jan.supabase.exceptions.RestException
    import io.github.jan.supabase.postgrest.Postgrest
    import io.github.jan.supabase.postgrest.from
    import io.github.jan.supabase.postgrest.postgrest
    import kotlinx.coroutines.launch
    import kotlinx.serialization.Serializable
    import kotlinx.serialization.json.JsonElement
    import kotlinx.serialization.json.JsonObject
    import kotlinx.coroutines.CoroutineScope
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.launch
    import kotlinx.coroutines.withContext


@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val password: String
)

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User)

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun getUser(username: String, password: String): User?
}

@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}

class DatabaseClient private constructor(context: Context) {
    val appDatabase: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java, "app-database"
    ).build()

    companion object {
        private var instance: DatabaseClient? = null

        fun getInstance(context: Context): DatabaseClient {
            if (instance == null) {
                instance = DatabaseClient(context)
            }
            return instance!!
        }
    }
}

    class MainActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContent {
                DashboardUITheme {
                    var currentPage by remember { mutableStateOf("first") } // Start with "first" page

                    Scaffold(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(it)
                                .background(Color(0xFFE3EBEA))
                        ) {
                            when (currentPage) {
                                "first" -> FirstPage(
                                    onRegisterClick = { currentPage = "register" },
                                    onLoginClick = { currentPage = "login" }
                                )

                                "register" -> RegisterPage(
                                    onBackClick = { currentPage = "first" }
                                )

                                "login" -> LoginPage(
                                    onBackClick = { currentPage = "first" },
                                    onLoginSuccess = {
                                        currentPage = "dashboard"
                                    } // Navigate to dashboard on successful login
                                )

                                "dashboard" -> AppContent() // Use AppContent from Main2 for the dashboard
                            }
                        }
                    }
                }
            }
        }
    }

@Composable
fun RegisterPage(onBackClick: () -> Unit){

    var nameValue by remember { mutableStateOf("") }
    var userNameValue by remember { mutableStateOf("") }
    var passwordValue by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val database = DatabaseClient.getInstance(context).appDatabase

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp), // Add padding, including left (start)
        horizontalAlignment = Alignment.Start,  // Align content to the left
        verticalArrangement = Arrangement.spacedBy(0.dp)  // No vertical space between children
    ) {
        Image(
            painter = painterResource(id = R.drawable.register_pic),
            contentDescription = "Page 2 Logo",
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .padding(bottom = 0.dp) // Add some space below the image
        )

        // Updated Text: Bold, larger, and aligned to the left
        Text(
            text = "Register!",
            fontSize = 50.sp,  // Set font size
            fontWeight = FontWeight.Bold,  // Make text bold
            modifier = Modifier
                .fillMaxWidth()  // Align with the image width
                .padding(start = 20.dp, top = 0.dp)  // No extra padding, aligns with image
        )
        Text(
            text = "Please Register to Access Portfolio Tracker",
            fontSize = 15.sp,
            fontWeight = FontWeight.Thin,
            modifier = Modifier
                .fillMaxWidth()  // Align with the image width
                .padding(start = 20.dp, end = 20.dp)
        )

        //Name
        Text(
            text = "Name",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()  // Align with the image width
                .padding(start = 20.dp, end = 20.dp, top = 15.dp)
        )

        OutlinedTextField(
            value = nameValue,
            onValueChange = { nameValue = it },
            label = { Text("Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp)  // Padding for left and right
        )


        //Username
        Text(
            text = "Username",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()  // Align with the image width
                .padding(start = 20.dp, end = 20.dp, top = 15.dp)
        )

        OutlinedTextField(
            value = userNameValue,
            onValueChange = { userNameValue = it },
            label = { Text("Username") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp)  // Padding for left and right
        )


        //Password
        Text(
            text = "Password",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()  // Align with the image width
                .padding(start = 20.dp, end = 20.dp, top = 15.dp)
        )

        OutlinedTextField(
            value = passwordValue,
            onValueChange = { passwordValue = it },
            label = { Text("Password") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp)  // Padding for left and right
        )

        // Row for buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 30.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Custom button with transparent background and black text
            Button(
                onClick = { onBackClick() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,  // Transparent background
                    contentColor = Color.Black  // Black text
                ),
                border = BorderStroke(2.dp, Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Back")
            }

            Spacer(modifier = Modifier.width(16.dp))  // Space between buttons

            // Regular button for Log In
            Button(
                onClick = {
                    // Insert new user into Room database
                    if (userNameValue.isNotEmpty() && passwordValue.isNotEmpty()) {
                        val user = User(username = userNameValue, password = passwordValue)
                        CoroutineScope(Dispatchers.IO).launch {
                            database.userDao().insert(user)
                        }
                        Toast.makeText(context, "Account created successfully!", Toast.LENGTH_SHORT).show()
                        onBackClick() // Go back to the previous page
                    } else {
                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Create Account")
            }
        }
    }
}

@Composable
fun LoginPage(onBackClick: () -> Unit, onLoginSuccess: () -> Unit) {
    var nameValue by remember { mutableStateOf("") }
    var userNameValue by remember { mutableStateOf("") }
    var passwordValue by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val database = DatabaseClient.getInstance(context).appDatabase


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp), // Add padding, including left (start)
        horizontalAlignment = Alignment.Start,  // Align content to the left
        verticalArrangement = Arrangement.spacedBy(0.dp)  // No vertical space between children
    ) {
        Image(
            painter = painterResource(id = R.drawable.login_pic),
            contentDescription = "Page 2 Logo",
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .padding(bottom = 0.dp, top = 30.dp) // Add some space below the image
        )

        // Updated Text: Bold, larger, and aligned to the left
        Text(
            text = "Log In!",
            fontSize = 50.sp,  // Set font size
            fontWeight = FontWeight.Bold,  // Make text bold
            modifier = Modifier
                .fillMaxWidth()  // Align with the image width
                .padding(start = 20.dp, top = 0.dp)  // No extra padding, aligns with image
        )
        Text(
            text = "Please Log in to Access Portfolio Tracker",
            fontSize = 15.sp,
            fontWeight = FontWeight.Thin,
            modifier = Modifier
                .fillMaxWidth()  // Align with the image width
                .padding(start = 20.dp, end = 20.dp)
        )

        //Username
        Text(
            text = "Username",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()  // Align with the image width
                .padding(start = 20.dp, end = 20.dp, top = 40.dp)
        )

        OutlinedTextField(
            value = userNameValue,
            onValueChange = { userNameValue = it },
            label = { Text("Username") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp)  // Padding for left and right
        )


        //Password
        Text(
            text = "Password",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()  // Align with the image width
                .padding(start = 20.dp, end = 20.dp, top = 30.dp)
        )

        OutlinedTextField(
            value = passwordValue,
            onValueChange = { passwordValue = it },
            label = { Text("Password") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp)  // Padding for left and right
        )

        // Row for buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 60.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Custom button with transparent background and black text
            Button(
                onClick = { onBackClick() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,  // Transparent background
                    contentColor = Color.Black  // Black text
                ),
                border = BorderStroke(2.dp, Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Back")
            }

            Spacer(modifier = Modifier.width(16.dp))  // Space between buttons

            // Regular button for Log In
            Button(
                onClick = {
                    // Check credentials in Room database
                    if (userNameValue.isNotEmpty() && passwordValue.isNotEmpty()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            val user = database.userDao().getUser(userNameValue, passwordValue)
                            if (user != null) {
                                // Valid credentials, log in success
                                onLoginSuccess()
                            } else {
                                // Invalid credentials
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Log In")
            }
        }
    }
}


    @Composable
    fun AppContent() {
        var selectedTab by remember { mutableStateOf(0) }
        var isChatOpen by remember { mutableStateOf(false) } // State to manage chat visibility
        Scaffold(
            bottomBar = {
                BottomNavigationBar(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                when (selectedTab) {
                    0 -> CurrencyListScreen()
                    1 -> SwapScreen()
                }
                Button(
                    onClick = { isChatOpen = true },
                    modifier = Modifier
                        .align(Alignment.BottomCenter) // Position in the bottom center of the screen
                        .padding(0.dp)
                        .offset(y = 0.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.robot),
                        contentDescription = "Chat Bot",
                        modifier = Modifier.size(40.dp) // Adjust the size as needed
                    )
                }

            }
            // Display the ChatScreen if isChatOpen is true
            if (isChatOpen) {
                ChatScreen(onClose = { isChatOpen = false })
            }
        }

    }

    @Composable
    fun BottomNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
        NavigationBar (
            containerColor = White
        ) {

            NavigationBarItem(
                icon = {
                    Icon(
                        painterResource(id = R.drawable.house_blank),
                        contentDescription = "Home",
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = "Top Currencies",
                        fontSize = 12.sp
                    )
                },
                selected = selectedTab == 0,
                onClick = { onTabSelected(0) }
            )
            NavigationBarItem(
                icon = {
                    Icon(
                        painterResource(id = R.drawable.swap),
                        contentDescription = "Swap",
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = "Swap",
                        fontSize = 12.sp
                    )
                },
                selected = selectedTab == 1,
                onClick = { onTabSelected(1) }
            )
        }

    }


    @Composable
    fun FirstPage(onRegisterClick: () -> Unit, onLoginClick: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 85.dp, start = 16.dp, end = 16.dp), // Add padding, including left (start)
            horizontalAlignment = Alignment.Start  // Align content to the left
        ) {
            Image(
                painter = painterResource(id = R.drawable.page_2_logo),
                contentDescription = "Page 2 Logo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .padding(bottom = 8.dp) // Add some space below the image
            )
            Spacer(modifier = Modifier.width(40.dp))
            // Updated Text: Bold, larger, and aligned to the left
            Text(
                text = "HodlHub!",
                fontSize = 60.sp,  // Set font size
                fontWeight = FontWeight.Bold,  // Make text bold
                modifier = Modifier
                    .fillMaxWidth()  // Align with the image width
                    .padding(start = 20.dp)  // No extra padding, aligns with image
            )
            Text(
                text = "A Crypto Portfolio Manager!",
                fontSize = 26.sp,  // Set font size
                fontWeight = FontWeight.Bold,  // Make text bold
                modifier = Modifier
                    .fillMaxWidth()  // Align with the image width
                    .padding(start = 20.dp)  // No extra padding, aligns with image
            )

            Text(
                text = "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non.",
                fontSize = 15.sp,
                fontWeight = FontWeight.Thin,
                modifier = Modifier
                    .fillMaxWidth()  // Align with the image width
                    .padding(start = 20.dp, end = 20.dp, top = 30.dp)
            )

            Spacer(modifier = Modifier.width(30.dp))  // Pushes the buttons to the bottom

            // Row for buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 50.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Custom button with transparent background and black text
                Button(
                    onClick = { onRegisterClick() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,  // Transparent background
                        contentColor = Color.Black  // Black text
                    ),
                    border = BorderStroke(2.dp, Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Register")
                }

                Spacer(modifier = Modifier.width(16.dp))  // Space between buttons

                // Regular button for Log In
                Button(
                    onClick = { onLoginClick() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,  // Black background
                        contentColor = Color.White  // White text
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Log In")
                }
            }
        }

    }
