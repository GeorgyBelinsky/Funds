package com.example.funds.presentation.pages.initiativelist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.example.funds.R
import com.example.funds.presentation.pages.mainscreen.Category
import com.example.funds.presentation.pages.mainscreen.Initiative


@Composable
fun InitiativeList(
    modifier: Modifier = Modifier,
    categories: List<Category>,
    initiatives: List<Initiative>,
    onInitiativeClick: (Initiative) -> Unit
) {
    val textFieldState = rememberTextFieldState()
    var selectedFilter by remember { mutableStateOf<Category?>(null) }

    val visibleInitiatives = remember(textFieldState.text, selectedFilter, initiatives) {
        initiatives.filter { initiative ->
            val matchesSearch = textFieldState.text.isBlank() ||
                    initiative.title.contains(textFieldState.text, ignoreCase = true) ||
                    initiative.description.contains(textFieldState.text, ignoreCase = true)
            val matchesFilter = selectedFilter == null || initiative.category.id == selectedFilter!!.id

            matchesSearch && matchesFilter
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SearchBar(
                textFieldState = textFieldState,
                onSearch = { },
                modifier = Modifier.weight(1.9f)
            )
            FilterDropDown(
                selectedFilter = selectedFilter,
                categories = categories,
                onFilterSelected = { selectedFilter = it },
                modifier = Modifier.weight(1.1f)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(visibleInitiatives) { initiative ->
                InitiativeCard(initiative, onClick = { onInitiativeClick(initiative) })
            }
        }
    }
}

@Composable
fun InitiativeCard(initiative: Initiative, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(185.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(12.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(width = 110.dp, height = 80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEAF2FF))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.invitation),
                        contentDescription = "Initiative image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = initiative.title,
                        style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF1F2024))
                    )
                    Text(
                        text = initiative.category.categoryName,
                        style = MaterialTheme.typography.bodyLarge.copy(color = initiative.category.color)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = initiative.description,
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF71727A)),
                maxLines = 3
            )
        }
    }
}

@Composable
fun SearchBar(
    textFieldState: TextFieldState,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by rememberSaveable { mutableStateOf(textFieldState.text.toString()) }

    OutlinedTextField(
        value = query,
        onValueChange = {
            query = it
            textFieldState.edit { replace(0, length, it) }
            onSearch(it)
        },
        modifier = modifier.height(56.dp),
        placeholder = { Text("Search") },
        leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon")
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Gray
        )
    )
}

@Composable
fun FilterDropDown(
    selectedFilter: Category?,
    categories: List<Category>,
    onFilterSelected: (Category?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedText = selectedFilter?.categoryName ?: "Filter"
    val selectedColor = selectedFilter?.color ?: Color.Black

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            border = ButtonDefaults.outlinedButtonBorder,
        ) {
            Text(text = selectedText, color = selectedColor)
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.filter_icon),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 6.dp)
                    .size(18.dp),
                tint = Color.Black
            )
        }

        DropdownMenu(
            expanded = expanded,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 16.dp,
            modifier = modifier
                .background(Color.White)
                .padding(horizontal = 13.dp),
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("All", color = Color.Black) },
                modifier = Modifier.height(46.dp),
                onClick = {
                    onFilterSelected(null)
                    expanded = false
                }
            )
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.categoryName, color = category.color) },
                    modifier = Modifier.height(46.dp),
                    onClick = {
                        onFilterSelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}
