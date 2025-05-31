package com.example.funds.presentation.pages.raiseslist

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.funds.presentation.pages.initiativelist.SearchBar
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.funds.R
import com.example.funds.UnsafeOkHttpClient.getUnsafeClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONArray

// Updated Raise class
data class Raise(
    val id: Int,
    val name: String,
    val goal: Int,
    val deadlineDate: String,
    val initiativeId: Int
)

// Sort enum stays the same
enum class RaiseSortType(val label: String) {
    GOAL("₴ Goal"),
    DEADLINE("Deadline"),
    POPULARITY("Popularity")
}

suspend fun fetchRaisesForInitiative(initiativeId: Int): List<Raise> {
    return try {
        val client = getUnsafeClient()
        val request = Request.Builder()
            .url("https://10.0.2.2:5001/api/Fundraisings/byInitiative/$initiativeId")
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
                Raise(
                    id = obj.getInt("id"),
                    name = obj.getString("title"),
                    goal = obj.getInt("goalAmount"),
                    deadlineDate = obj.getString("deadline").substringBefore("T"),
                    initiativeId = obj.getInt("initiativeId")
                )
            }
        } else emptyList()
    } catch (e: Exception) {
        emptyList()
    }
}

@Composable
fun RaisesList(
    initiativeId: Int,
    initiativeTitle: String,
    initiativeColor: Color,
    onBack: () -> Unit,
    onRaiseClick: (Raise) -> Unit
) {
    val context = LocalContext.current
    val textFieldState = rememberTextFieldState()
    var selectedSort: RaiseSortType by remember { mutableStateOf(RaiseSortType.DEADLINE) }
    var sortAscending by remember { mutableStateOf(true) }

    var raises by remember { mutableStateOf<List<Raise>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(initiativeId) {
        isLoading = true
        raises = fetchRaisesForInitiative(initiativeId)
        isLoading = false
    }

    val filteredRaises = remember(textFieldState.text, selectedSort, sortAscending, raises) {
        var list = raises.filter {
            it.name.contains(textFieldState.text, ignoreCase = true)
        }

        list = when (selectedSort) {
            RaiseSortType.GOAL -> if (sortAscending) list.sortedBy { it.goal } else list.sortedByDescending { it.goal }
            RaiseSortType.DEADLINE -> if (sortAscending) list.sortedBy { it.deadlineDate } else list.sortedByDescending { it.deadlineDate }
            RaiseSortType.POPULARITY -> list.shuffled()
        }

        list
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = initiativeTitle,
                style = MaterialTheme.typography.headlineSmall,
                color = initiativeColor,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchBar(
                textFieldState = textFieldState,
                onSearch = {},
                modifier = Modifier.weight(1.5f)
            )
            RaiseSortDropDown(
                selectedSort = selectedSort,
                onSortSelected = { selectedSort = it },
                sortAscending = sortAscending,
                onToggleOrder = { sortAscending = !sortAscending },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                showPushNotification(
                    context = context,
                    title = "Subscribed to $initiativeTitle",
                    message = "You’ll now receive updates for $initiativeTitle initiative."
                )
            }) {
                Icon(Icons.Default.Notifications, contentDescription = "Notification")
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn {
                items(filteredRaises) { raise ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { onRaiseClick(raise) },
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = raise.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Goal: ₴${raise.goal}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Deadline: ${raise.deadlineDate}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RaiseSortDropDown(
    selectedSort: RaiseSortType,
    onSortSelected: (RaiseSortType) -> Unit,
    sortAscending: Boolean,
    onToggleOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val arrowIcon = if (sortAscending) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown

    Box(modifier = modifier.height(56.dp)) {
        OutlinedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxHeight().width(160.dp)
        ) {
            Text(text = selectedSort.label)
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(onClick = { onToggleOrder() }, modifier = Modifier.size(24.dp)) {
                Icon(imageVector = arrowIcon, contentDescription = "Toggle sort")
            }
        }

        DropdownMenu(
            expanded = expanded,
            shape = RoundedCornerShape(16.dp),
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            RaiseSortType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.label) },
                    onClick = {
                        onSortSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

fun showPushNotification(context: Context, title: String, message: String) {
    val channelId = "raises_notifications"
    val notificationId = 1001

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, "Raise Updates", NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.notification_icon)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    NotificationManagerCompat.from(context).notify(notificationId, builder.build())
}
