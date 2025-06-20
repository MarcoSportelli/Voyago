package com.example.voyago.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.voyago.DataProfile
import com.example.voyago.Travel
import com.example.voyago.utils.ProfileImage
import com.example.voyago.viewModels.ProfileRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripSearchbar(
    query: String,
    onQueryChange: (String) -> Unit,
    onResultClick: (String) -> Unit,
    filteredTrips: List<Travel>,
    modifier: Modifier = Modifier,
    isSearchActive: Boolean,
    onActiveChange: (Boolean) -> Unit,
    userId: Int? = null
) {


    var profile by remember { mutableStateOf<DataProfile?>(null) }

    LaunchedEffect(userId) {
        if (userId != null) {
            profile = ProfileRepository.getProfileByInternalId(userId)
        }
    }


    val suggestions = remember(filteredTrips) {
        filteredTrips
            .map { it.title }
            .distinct()
            .shuffled()
            .take(3)
    }

    SearchBar(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 300.dp),
        query = query,
        onQueryChange = {
            onQueryChange(it)
             onActiveChange(it.isNotEmpty())
        },
        onSearch = {onActiveChange(false) },
        leadingIcon = {
            if (!isSearchActive) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            } else {
                IconButton(onClick = { onQueryChange(""); onActiveChange(false) }) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                }
            }
        },
        trailingIcon = {
            if (isSearchActive && query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
            if(!isSearchActive && profile != null){
                ProfileImage(
                    profile = profile!!,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                )
            }
        },
        active = isSearchActive,
        onActiveChange = { active -> onActiveChange(active) },
        enabled = true,
        placeholder = { Text("Find your trip") },
        shape = RoundedCornerShape(48.dp),
        colors = SearchBarDefaults.colors(containerColor = Color.White),
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
        windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        interactionSource = remember { MutableInteractionSource() },
        content = {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp),
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(Color.White)

                ) {
                    items(suggestions.size) { index ->
                        val suggestion = suggestions[index]
                        ListItem(
                            headlineContent = {
                                Text(suggestion)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onResultClick(suggestion)
                                    onQueryChange(suggestion)
                                    onActiveChange(false)
                                }
                                .background(Color.White)
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    )
}
