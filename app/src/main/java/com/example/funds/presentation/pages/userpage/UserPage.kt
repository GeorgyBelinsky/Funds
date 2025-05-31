package com.example.funds.presentation.pages.userpage

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.funds.R
import com.example.funds.presentation.ui.theme.InitiativeBlue
import com.example.funds.presentation.ui.theme.InitiativeGreen
import com.example.funds.presentation.ui.theme.InitiativeRed

data class DonationEntry(
    val time: String,
    val amount: String,
    val initiativeName: String,
    val color: Color,
    val extraInfo: DonationExtra? = null
)

data class DonationExtra(
    val title: String,
    val deadline: String,
    val goal: String
)

fun sampleDonationGroups(): Map<String, List<DonationEntry>> {
    return mapOf(
        "20.01.2001" to listOf(
            DonationEntry(
                time = "23:40",
                amount = "₴90,000",
                initiativeName = "Green School Renovation",
                color = InitiativeBlue,
                extraInfo = DonationExtra("Build New Classrooms", "25.02.2001", "₴300,000")
            ),
            DonationEntry(
                time = "23:30",
                amount = "₴900,000",
                initiativeName = "Startup Booster",
                color = InitiativeGreen,
                extraInfo = DonationExtra("Equipment for Startups", "01.03.2001", "₴1,200,000")
            ),
            DonationEntry(
                time = "23:10",
                amount = "₴100,000",
                initiativeName = "Startup Booster",
                color = InitiativeGreen,
                extraInfo = DonationExtra("Marketing & Outreach", "10.03.2001", "₴600,000")
            ),
        ),
        "20.12.2000" to listOf(
            DonationEntry(
                time = "18:20",
                amount = "₴80",
                initiativeName = "Books for All",
                color = InitiativeBlue,
                extraInfo = DonationExtra("Library Fundraiser", "15.01.2001", "₴50,000")
            ),
            DonationEntry(
                time = "16:00",
                amount = "₴900,000",
                initiativeName = "Children Hospital ICU",
                color = InitiativeRed,
                extraInfo = DonationExtra("ICU Renovation", "01.02.2001", "₴1,500,000")
            ),
            DonationEntry(
                time = "15:00",
                amount = "₴38,000",
                initiativeName = "Children Hospital ICU",
                color = InitiativeRed,
                extraInfo = DonationExtra("Ventilator Purchase", "22.01.2001", "₴380,000")
            ),
        ),
        "10.12.2000" to listOf(
            DonationEntry(
                time = "19:20",
                amount = "₴800",
                initiativeName = "After School Program",
                color = InitiativeBlue,
                extraInfo = DonationExtra("STEM Workshop", "31.01.2001", "₴25,000")
            ),
            DonationEntry(
                time = "18:20",
                amount = "₴12,500",
                initiativeName = "Women-Led Startups",
                color = InitiativeGreen,
                extraInfo = DonationExtra("Pitch Day Event", "05.02.2001", "₴70,000")
            ),
            DonationEntry(
                time = "16:00",
                amount = "₴60,000",
                initiativeName = "Remote Clinic Equipment",
                color = InitiativeRed,
                extraInfo = DonationExtra("Mobile Ultrasound Kit", "30.12.2000", "₴150,000")
            ),
        ),
        "09.12.2000" to listOf(
            DonationEntry(
                time = "19:20",
                amount = "₴3,200",
                initiativeName = "Community Garden Project",
                color = InitiativeBlue,
                extraInfo = DonationExtra("Tool Shed Build", "10.01.2001", "₴10,000")
            ),
            DonationEntry(
                time = "18:20",
                amount = "₴5,000",
                initiativeName = "Village WiFi Network",
                color = InitiativeGreen,
                extraInfo = DonationExtra("Router Installation", "20.01.2001", "₴35,000")
            ),
            DonationEntry(
                time = "16:00",
                amount = "₴60,000",
                initiativeName = "Neonatal Care Units",
                color = InitiativeRed,
                extraInfo = DonationExtra("Neonatal Beds & Monitors", "01.01.2001", "₴500,000")
            ),
        ),
        "01.12.2000" to listOf(
            DonationEntry(
                time = "20:45",
                amount = "₴1,200,000",
                initiativeName = "National Reading Campaign",
                color = InitiativeBlue,
                extraInfo = DonationExtra("Free Books Distribution", "28.02.2001", "₴2,000,000")
            ),
            DonationEntry(
                time = "18:30",
                amount = "₴100",
                initiativeName = "Crafts Cooperative",
                color = InitiativeGreen,
                extraInfo = DonationExtra("Workshop Equipment", "15.01.2001", "₴40,000")
            ),
            DonationEntry(
                time = "17:50",
                amount = "₴75,000",
                initiativeName = "Rural Ambulance Fleet",
                color = InitiativeRed,
                extraInfo = DonationExtra("Ambulance for East Region", "05.01.2001", "₴1,000,000")
            ),
        ),
        "29.11.2000" to listOf(
            DonationEntry(
                time = "22:15",
                amount = "₴400",
                initiativeName = "Clean Energy for Schools",
                color = InitiativeGreen,
                extraInfo = DonationExtra("Solar Panel Installation", "12.01.2001", "₴300,000")
            ),
            DonationEntry(
                time = "21:00",
                amount = "₴15,000",
                initiativeName = "Youth Coding Bootcamp",
                color = InitiativeBlue,
                extraInfo = DonationExtra("Laptops & Tutors", "20.01.2001", "₴100,000")
            ),
            DonationEntry(
                time = "20:30",
                amount = "₴200,000",
                initiativeName = "Pediatric MRI Scanner",
                color = InitiativeRed,
                extraInfo = DonationExtra("MRI Machine Purchase", "15.02.2001", "₴1,200,000")
            ),
        )
    )
}

@Composable
fun UserPage(
    user: UserData,
    onSignOut: () -> Unit
) {
    val donationGroups = sampleDonationGroups()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProfileHeader(user = user, onSignOut = onSignOut)

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.donation_history),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(18.dp),
                tint = Color.Black
            )
            Text("Your donations:", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxSize(),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                donationGroups.forEach { (date, donations) ->
                    DateDonationGroup(date, donations)
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(user: UserData, onSignOut: () -> Unit) {
    var name by remember { mutableStateOf("${user.firstName} ${user.secondName}") }
    var isEditingName by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { selectedImageUri = it }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.BottomEnd) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier
                            .size(90.dp)
                            .background(Color.LightGray, CircleShape)
                            .padding(8.dp)
                    )
                }

                IconButton(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color.Blue, CircleShape)
                        .padding(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit image",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.width(180.dp)) {
                        if (isEditingName) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = MaterialTheme.typography.titleMedium
                            )
                        } else {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.align(Alignment.CenterStart)
                            )
                        }
                    }

                    IconButton(
                        onClick = { isEditingName = !isEditingName },
                        modifier = Modifier
                            .size(18.dp)
                            .background(Color.Blue, CircleShape)
                            .padding(2.dp)
                    ) {
                        Icon(
                            imageVector = if (isEditingName) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = if (isEditingName) "Save" else "Edit name",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Text(user.email, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onSignOut,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier
                        .height(35.dp)
                        .width(110.dp)
                ) {
                    Text("Sign Out", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun DateDonationGroup(date: String, donations: List<DonationEntry>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "[$date]",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Black
        )
    }

    donations.forEach { donation ->
        var expanded by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
        ) {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                thickness = 1.dp,
                color = Color.LightGray
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = donation.time)
                    Text(
                        text = donation.amount,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .widthIn(max = 250.dp)
                        .fillMaxHeight()
                ) {
                    Text(
                        text = donation.initiativeName,
                        color = donation.color,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
            }

            if (expanded && donation.extraInfo != null) {

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = donation.extraInfo.title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Deadline: ${donation.extraInfo.deadline}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Normal,
                                color = Color.Black
                            ))
                        Text("Goal: ${donation.extraInfo.goal}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            ))
                    }
                }

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    thickness = 1.dp,
                    color = Color.LightGray
                )
            }
        }
    }
}