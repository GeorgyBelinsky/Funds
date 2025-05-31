package com.example.funds.presentation.pages.raisedetails


import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.funds.R
import com.example.funds.UnsafeOkHttpClient.getUnsafeClient
import com.example.funds.presentation.pages.mainscreen.Initiative
import com.example.funds.presentation.pages.raiseslist.Raise
import com.google.firebase.Firebase
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.androidParameters
import com.google.firebase.dynamiclinks.dynamicLink
import com.google.firebase.dynamiclinks.dynamicLinks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject


@Composable
fun RaiseDetails(
    initiative: Initiative,
    raise: Raise,
    onBack: () -> Unit,
    isUserLoggedIn: Boolean,
    onNavigateToAuth: () -> Unit
) {
    val context = LocalContext.current

    var showDonationDialog by remember { mutableStateOf(false) }
    var donationAmount by remember { mutableStateOf("") }

    data class DailyIncome(val date: String, val amount: Int)
    data class RaiseStatistics(
        val fundraisingId: Int,
        val goal: Int,
        val totalCollected: Int,
        val dailyIncomes: List<DailyIncome>
    )

    var stats by remember { mutableStateOf<RaiseStatistics?>(null) }
    var refreshTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(raise.id, refreshTrigger) {
        try {
            val client = getUnsafeClient()
            val request = Request.Builder()
                .url("https://10.0.2.2:5001/api/Fundraisings/${raise.id}/statistics")
                .build()

            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }

            if (response.isSuccessful) {
                response.body?.string()?.let { body ->
                    val json = JSONObject(body)
                    val incomesJson = json.getJSONArray("dailyIncomes")
                    val incomes = (0 until incomesJson.length()).map { i ->
                        val item = incomesJson.getJSONObject(i)
                        DailyIncome(
                            date = item.getString("date").substring(0, 10),
                            amount = item.getInt("amount")
                        )
                    }

                    stats = RaiseStatistics(
                        fundraisingId = json.getInt("fundraisingId"),
                        goal = json.getInt("goal"),
                        totalCollected = json.getInt("totalCollected"),
                        dailyIncomes = incomes
                    )
                }
            }
        } catch (_: Exception) {
            // Handle error gracefully
        }
    }

    if (stats == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val progress = stats!!.totalCollected.toFloat() / stats!!.goal
    val percentage = (progress * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            IconButton(onClick = { shareRaise(context, raise) }) {
                Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
            }
        }

        Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
            Column {
                Image(
                    painterResource(id = R.drawable.invitation),
                    contentDescription = raise.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .padding(bottom = 16.dp)
                )

                Text(
                    text = raise.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "₴${stats!!.totalCollected} / ₴${stats!!.goal}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = Color(0xFFEEEEEE)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("$percentage%", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Donations", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    stats!!.dailyIncomes.forEach { income ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(income.date, style = MaterialTheme.typography.bodyMedium)
                            Text("₴${income.amount}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (isUserLoggedIn) {
                        showDonationDialog = true
                    } else {
                        onNavigateToAuth()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Support", color = Color.White)
            }

            if (showDonationDialog) {
                AlertDialog(
                    onDismissRequest = { showDonationDialog = false },
                    confirmButton = {
                        Button(
                            onClick = {
                                val amountInt = donationAmount.toIntOrNull()
                                if (amountInt == null || amountInt <= 0) {
                                    Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                val client = getUnsafeClient()
                                val mediaType = "application/json".toMediaType()
                                val requestBody = """{"fundraisingId": ${raise.id},"amount": $amountInt,"userId": 1}"""
                                    .trimIndent().toRequestBody(mediaType)

                                val request = Request.Builder()
                                    .url("https://10.0.2.2:5001/api/Donate/DonateTest")
                                    .post(requestBody)
                                    .build()

                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val response = client.newCall(request).execute()
                                        withContext(Dispatchers.Main) {
                                            if (response.isSuccessful) {
                                                Toast.makeText(context, "Donated ₴$amountInt", Toast.LENGTH_SHORT).show()
                                                donationAmount = ""
                                                showDonationDialog = false
                                                refreshTrigger++ // ✅ Trigger re-fetch of stats
                                            } else {
                                                Toast.makeText(context, "Donation failed", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                        ) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showDonationDialog = false }) {
                            Text("Cancel", color = Color.Black)
                        }
                    },
                    title = { Text("Donate to ${raise.name}") },
                    text = {
                        Column {
                            Text("Enter donation amount:")
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = donationAmount,
                                onValueChange = { donationAmount = it },
                                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = {
                                    Text("₴", style = MaterialTheme.typography.bodyLarge)
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                )
            }
        }
    }
}


fun shareRaise(context: Context, raise: Raise) {
    val dynamicLink = Firebase.dynamicLinks.dynamicLink {
        link = "https://fundsraise.com/raise?id=${raise.id}".toUri()
        domainUriPrefix = "https://fundsraise.page.link"
        androidParameters { }
    }

    val dynamicLinkUri = dynamicLink.uri

    Firebase.dynamicLinks.createDynamicLink()
        .setLink("https://fundsraise.com/raise?id=${raise.id}".toUri())
        .setDomainUriPrefix("https://fundsraise.page.link")
        .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build())
        .buildShortDynamicLink()
        .addOnSuccessListener { result ->
            val shortLink = result.shortLink
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "${raise.name} \n $shortLink")
            }
            context.startActivity(Intent.createChooser(intent, "Share via"))
        }
        .addOnFailureListener {
            Toast.makeText(context, "Failed to create link", Toast.LENGTH_SHORT).show()
        }
}