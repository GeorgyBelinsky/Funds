package com.example.funds.presentation.pages.mainscreen

import JWT_TOKEN_KEY
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import com.example.funds.R
import com.example.funds.UnsafeOkHttpClient.getUnsafeClient
import com.example.funds.presentation.pages.initiativelist.InitiativeList
import com.example.funds.presentation.pages.raisedetails.RaiseDetails
import com.example.funds.presentation.pages.raiseslist.Raise
import com.example.funds.presentation.pages.raiseslist.RaisesList
import com.example.funds.presentation.pages.userpage.AuthScreen
import com.example.funds.presentation.pages.userpage.UserData
import com.example.funds.presentation.pages.userpage.UserPage
import com.example.funds.presentation.ui.theme.InitiativeBlue
import com.example.funds.presentation.ui.theme.InitiativeGreen
import com.example.funds.presentation.ui.theme.InitiativeRed
import dataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

data class Category(
    val id: Int,
    val categoryName: String,
    val color: Color
)

data class Initiative(
    val id: Int,
    val title: String,
    val description: String,
    val category: Category,
    val raises: List<Raise>
)

suspend fun fetchCategories(): List<Category> {
    return try {
        val client = getUnsafeClient()
        val request = Request.Builder()
            .url("https://10.0.2.2:5001/api/Categories")
            .addHeader("accept", "text/plain")
            .build()

        val response = withContext(Dispatchers.IO) {
            client.newCall(request).execute()
        }

        if (response.isSuccessful) {
            val body = response.body?.string() ?: return emptyList()
            val jsonArray = JSONArray(body)
            (0 until jsonArray.length()).map { i ->
                val obj = jsonArray.getJSONObject(i)
                val name = obj.getString("categoryName")
                Category(
                    id = obj.getInt("id"),
                    categoryName = name,
                    color = when (name.lowercase()) {
                        "economical" -> InitiativeGreen
                        "medical" -> InitiativeRed
                        "ecological" -> InitiativeBlue
                        else -> Color.Gray
                    }
                )
            }
        } else emptyList()
    } catch (e: Exception) {
        emptyList()
    }
}

suspend fun fetchInitiativesWithCategories(categories: List<Category>): List<Initiative> {
    return try {
        val client = getUnsafeClient()
        val request = Request.Builder()
            .url("https://10.0.2.2:5001/api/Initiative")
            .addHeader("accept", "*/*")
            .build()

        val response = withContext(Dispatchers.IO) {
            client.newCall(request).execute()
        }

        if (response.isSuccessful) {
            val body = response.body?.string() ?: return emptyList()
            val jsonArray = JSONArray(body)
            (0 until jsonArray.length()).map { i ->
                val obj = jsonArray.getJSONObject(i)
                val categoryId = obj.getInt("categoryId")
                val category = categories.find { it.id == categoryId }
                    ?: Category(categoryId, "Unknown", Color.Gray)

                Initiative(
                    id = obj.getInt("id"),
                    title = obj.getString("title"),
                    description = obj.getString("description"),
                    category = category,
                    raises = emptyList()
                )
            }
        } else emptyList()
    } catch (e: Exception) {
        emptyList()
    }
}

suspend fun fetchRaiseById(id: Int): Raise? {
    return try {
        val client = getUnsafeClient()
        val request = Request.Builder()
            .url("https://10.0.2.2:5001/api/Fundraisings/$id")
            .addHeader("accept", "*/*")
            .build()

        val response = withContext(Dispatchers.IO) {
            client.newCall(request).execute()
        }

        if (response.isSuccessful) {
            val body = response.body?.string() ?: return null
            val obj = JSONObject(body)

            Raise(
                id = obj.getInt("id"),
                name = obj.getString("title"),
                goal = obj.getInt("goalAmount"),
                deadlineDate = obj.getString("deadline"),
                initiativeId = obj.getInt("initiativeId")
            )
        } else null
    } catch (e: Exception) {
        null
    }
}

@Composable
fun MainScreen(
    context: Context = LocalContext.current,
    navigateToRaiseId: Int? = null
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val selectedRaise = remember { mutableStateOf<Raise?>(null) }
    val selectedInitiative = remember { mutableStateOf<Initiative?>(null) }

    val jwtToken = remember { mutableStateOf<String?>(null) }
    val userData = remember { mutableStateOf<UserData?>(null) }

    val dataStore = context.dataStore
    val scope = rememberCoroutineScope()

    val categories = remember { mutableStateOf<List<Category>>(emptyList()) }
    val initiatives = remember { mutableStateOf<List<Initiative>>(emptyList()) }

    LaunchedEffect(Unit) {
        val fetchedCategories = fetchCategories()
        val fetchedInitiatives = fetchInitiativesWithCategories(fetchedCategories)
        categories.value = fetchedCategories
        initiatives.value = fetchedInitiatives
    }

    LaunchedEffect(navigateToRaiseId) {
        navigateToRaiseId?.let { id ->
            val raise = fetchRaiseById(id)
            if (raise != null) {
                val initiative = initiatives.value.find { it.id == raise.initiativeId }
                if (initiative != null) {
                    selectedInitiative.value = initiative
                    selectedRaise.value = raise
                    selectedIndex = 0
                } else {
                    //Log.w("DynamicLink", "Initiative not found for raise $id")
                }
            }
        }
    }

    // Load JWT token and user data
    LaunchedEffect(Unit) {
        dataStore.data.map { it[JWT_TOKEN_KEY] }.collect { token ->
            if (!token.isNullOrBlank()) {
                jwtToken.value = token
                val user = fetchUserInfo(token)
                if (user != null) {
                    userData.value = user
                }
            }
        }
    }

    val navItemList = listOf(
        NavItem("Home", ImageVector.vectorResource(id = R.drawable.vector)),
        NavItem("Profile", Icons.Default.Person),
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .padding(bottom = 25.dp, start = 10.dp, end = 10.dp, top = 15.dp)
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                ) {
                    navItemList.forEachIndexed { index, navItem ->
                        NavigationBarItem(
                            selected = selectedIndex == index,
                            onClick = {
                                selectedInitiative.value = null
                                selectedRaise.value = null
                                selectedIndex = index
                            },
                            modifier = Modifier.padding(top = 30.dp),
                            icon = {
                                Icon(
                                    imageVector = navItem.icon,
                                    contentDescription = navItem.label,
                                    modifier = Modifier.size(24.dp),
                                    tint = if (selectedIndex == index) Color.Black else Color.Gray
                                )
                            },
                            label = {
                                Text(
                                    text = navItem.label,
                                    color = if (selectedIndex == index) Color.Black else Color.Gray
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent,
                                selectedIconColor = Color.Black,
                                unselectedIconColor = Color.Gray,
                                selectedTextColor = Color.Black,
                                unselectedTextColor = Color.Gray
                            ),
                            alwaysShowLabel = true
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ContentScreen(
                selectedIndex = selectedIndex,
                onSelectedIndexChange = { selectedIndex = it },
                jwtToken = jwtToken.value,
                userData = userData.value,
                onLoginSuccess = { token ->
                    jwtToken.value = token
                    scope.launch {
                        context.dataStore.edit { it[JWT_TOKEN_KEY] = token }
                        val user = fetchUserInfo(token)
                        if (user != null) {
                            userData.value = user
                        }
                    }
                },
                selectedInitiative = selectedInitiative,
                onInitiativeSelected = { selectedInitiative.value = it },
                selectedRaise = selectedRaise,
                onRaiseClick = { selectedRaise.value = it },
                onLogout = {
                    jwtToken.value = null
                    userData.value = null
                    scope.launch {
                        context.dataStore.edit { it.remove(JWT_TOKEN_KEY) }
                    }
                },
                categories = categories.value,
                initiatives = initiatives.value
            )
        }
    }
}

@Composable
fun ContentScreen(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    jwtToken: String?,
    userData: UserData?,
    onLoginSuccess: (token: String) -> Unit,
    selectedInitiative: MutableState<Initiative?>,
    onInitiativeSelected: (Initiative) -> Unit,
    selectedRaise: MutableState<Raise?>,
    onRaiseClick: (Raise) -> Unit,
    onLogout: () -> Unit,
    categories: List<Category>,
    initiatives: List<Initiative>
) {
    Box(modifier = modifier) {
        when {
            selectedRaise.value != null && selectedInitiative.value != null -> {
                RaiseDetails(
                    initiative = selectedInitiative.value!!,
                    raise = selectedRaise.value!!,
                    onBack = { selectedRaise.value = null },
                    isUserLoggedIn = jwtToken != null,
                    onNavigateToAuth = {
                        onSelectedIndexChange(1)
                        selectedRaise.value = null
                        selectedInitiative.value = null
                    }
                )
            }

            selectedInitiative.value != null -> {
                val initiative = selectedInitiative.value!!
                RaisesList(
                    initiativeId = initiative.id,
                    initiativeTitle = initiative.title,
                    initiativeColor = initiative.category.color,
                    onBack = { selectedInitiative.value = null },
                    onRaiseClick = { raise -> selectedRaise.value = raise }
                )
            }

            selectedIndex == 0 -> InitiativeList(
                categories = categories,
                initiatives = initiatives,
                onInitiativeClick = { selectedInitiative.value = it }
            )

            selectedIndex == 1 -> {
                if (jwtToken != null && userData != null) {
                    UserPage(user = userData, onSignOut = onLogout)
                } else {
                    AuthScreen(onLoginSuccess = { token -> onLoginSuccess(token) })
                }
            }
        }
    }
}

suspend fun fetchUserInfo(token: String): UserData? {
    return try {
        val client = getUnsafeClient()
        val request = Request.Builder()
            .url("https://10.0.2.2:5001/api/User/GetUser")
            .addHeader("Authorization", "Bearer $token")
            .build()

        val response = withContext(Dispatchers.IO) {
            client.newCall(request).execute()
        }

        if (response.isSuccessful) {
            val body = response.body?.string()
            val json = JSONObject(body)
            UserData(
                firstName = json.optString("firstName"),
                secondName = json.optString("secondName"),
                email = json.optString("email")
            )
        } else null
    } catch (e: Exception) {
        null
    }
}