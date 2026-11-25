package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.ChronologicalOrder
import com.example.data.model.TheoryDossier
import com.example.ui.components.CategoryBadge
import com.example.ui.components.DossierCoverImage
import com.example.ui.components.GpsCoordinatePill
import com.example.ui.components.GroundingAttributionFooter
import com.example.ui.components.LawsAndDangersBadge
import com.example.ui.components.OfflineModeBanner
import com.example.ui.components.SourceLinksStrip
import androidx.compose.material.icons.rounded.MyLocation
import com.example.data.model.TheoryTopic
import com.example.ui.components.TopicFilterBar
import com.example.ui.components.WarningBanner
import com.example.ui.components.openExternalUrl
import com.example.ui.components.openGpsInMaps
import com.example.ui.theme.ArchiveBorder
import com.example.ui.theme.ArchiveCard
import com.example.ui.theme.ArchiveCardElevated
import com.example.ui.theme.ArchiveObsidian
import com.example.ui.theme.ArchiveSlate
import com.example.ui.theme.CrimsonThread
import com.example.ui.theme.ParchmentCream
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.ParchmentSecondary
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.SignalAmberContainer
import com.example.ui.theme.SignalAmberSoft
import com.example.ui.theme.VerifyTeal
import com.example.ui.viewmodel.WebScanState

private val quickTopics = listOf(
    "This Week" to "Most viral conspiracy theory of the week November 2026 and emerging theories",
    "History" to "Historical conspiracy theories, secret operations, and mysteries across eras",
    "Technology" to "Technology, AI, cyber surveillance, and Dead Internet conspiracy theories",
    "Space" to "Space, UAP, aerospace, and lunar conspiracy theories with physical locations",
    "Politics" to "Political and declassified government conspiracy theories and investigations",
    "Science" to "Science, physics, CERN, and unexplained experiment conspiracy theories"
)

@Composable
fun FeedScreen(
    dossiers: List<TheoryDossier>,
    weeklySpotlight: TheoryDossier?,
    isOnline: Boolean,
    bookmarkedCount: Int,
    searchQuery: String,
    selectedCategory: String,
    topicCounts: Map<String, Int>,
    onlyGpsMapped: Boolean,
    sortOrder: ChronologicalOrder,
    scanState: WebScanState,
    onSearchQueryChange: (String) -> Unit,
    onRunSearch: (String, Boolean) -> Unit,
    onScanTopic: (String) -> Unit,
    onRefreshSpotlight: () -> Unit,
    onSelectCategory: (String) -> Unit,
    onToggleOnlyGpsMapped: () -> Unit,
    onResetFilters: () -> Unit,
    onToggleSortOrder: () -> Unit,
    onToggleBookmark: (TheoryDossier) -> Unit,
    onShareDossier: (TheoryDossier) -> Unit,
    onOpenDossier: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val isScanning = scanState is WebScanState.Scanning

    val latestQueries = dossiers.firstOrNull { it.searchQueries.isNotEmpty() }?.searchQueries
        ?: weeklySpotlight?.searchQueries.orEmpty()

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // Editorial Masthead
        item(key = "masthead") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.app_tagline),
                        style = MaterialTheme.typography.labelMedium,
                        color = SignalAmber
                    )
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.displaySmall,
                        color = ParchmentCream
                    )
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ArchiveCardElevated,
                    border = BorderStroke(1.dp, ArchiveBorder)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Radar,
                            contentDescription = "Live Grounded Search",
                            tint = if (isScanning) SignalAmber else VerifyTeal,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isScanning) "SCANNING" else "${dossiers.size} FILES",
                            style = MaterialTheme.typography.labelMedium,
                            color = ParchmentCream
                        )
                    }
                }
            }
        }

        // Offline Mode Indicator
        if (!isOnline) {
            item(key = "offline_banner") {
                OfflineModeBanner(savedCount = bookmarkedCount)
            }
        }

        // Web Search Bar + Scan Button
        item(key = "search_bar") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = {
                            Text(
                                text = stringResource(R.string.search_placeholder),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Search web",
                                tint = SignalAmber
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Clear search",
                                        tint = ParchmentMuted
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (searchQuery.isNotBlank()) {
                                    keyboardController?.hide()
                                    onRunSearch(searchQuery, false)
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SignalAmber,
                            unfocusedBorderColor = ArchiveBorder,
                            focusedContainerColor = ArchiveSlate,
                            unfocusedContainerColor = ArchiveSlate,
                            focusedTextColor = ParchmentCream,
                            unfocusedTextColor = ParchmentCream,
                            focusedPlaceholderColor = ParchmentMuted,
                            unfocusedPlaceholderColor = ParchmentMuted
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_input")
                    )

                    Button(
                        onClick = {
                            keyboardController?.hide()
                            val queryToRun = searchQuery.ifBlank {
                                "Trending conspiracy theories and mysteries on the web 2025 2026"
                            }
                            onRunSearch(queryToRun, searchQuery.isBlank())
                        },
                        enabled = !isScanning,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SignalAmber,
                            contentColor = ArchiveObsidian
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
                        modifier = Modifier
                            .height(54.dp)
                            .testTag("scan_web_button")
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = ArchiveObsidian,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.scan_web),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }

                // Quick Curated Web Scan Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    quickTopics.forEach { (label, prompt) ->
                        Surface(
                            onClick = {
                                if (!isScanning) {
                                    onRunSearch(prompt, label == "This Week")
                                }
                            },
                            shape = RoundedCornerShape(20.dp),
                            color = ArchiveCard,
                            border = BorderStroke(1.dp, ArchiveBorder),
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                color = SignalAmberSoft,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Scan Progress / Error Banner
        item(key = "scan_status") {
            AnimatedVisibility(visible = scanState is WebScanState.Scanning || scanState is WebScanState.Error) {
                when (scanState) {
                    is WebScanState.Scanning -> {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SignalAmberContainer.copy(alpha = 0.7f),
                            border = BorderStroke(1.dp, SignalAmber.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "Searching web & checking updates/laws for: \"${scanState.query}\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ParchmentCream
                                )
                                LinearProgressIndicator(
                                    color = SignalAmber,
                                    trackColor = ArchiveObsidian,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                    is WebScanState.Error -> {
                        WarningBanner(message = scanState.message)
                    }
                    else -> Unit
                }
            }
        }

        // Conspiracy of the Week Spotlight Card
        if (weeklySpotlight != null) {
            item(key = "weekly_spotlight_${weeklySpotlight.id}") {
                WeeklySpotlightCard(
                    dossier = weeklySpotlight,
                    isScanning = isScanning,
                    onRefreshSpotlight = onRefreshSpotlight,
                    onOpenDossier = { onOpenDossier(weeklySpotlight.id) },
                    onToggleBookmark = { onToggleBookmark(weeklySpotlight) },
                    onShare = { onShareDossier(weeklySpotlight) },
                    onOpenGps = {
                        val lat = weeklySpotlight.latitude
                        val lon = weeklySpotlight.longitude
                        if (lat != null && lon != null) {
                            openGpsInMaps(context, lat, lon, weeklySpotlight.locationName ?: weeklySpotlight.title)
                        }
                    },
                    onOpenSource = { url -> openExternalUrl(context, url) }
                )
            }
        } else if (!isScanning) {
            item(key = "spotlight_placeholder") {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ArchiveCard,
                    border = BorderStroke(1.dp, SignalAmber.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_weekly_spotlight),
                            contentDescription = "Weekly Spotlight Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.conspiracy_of_the_week),
                                style = MaterialTheme.typography.labelMedium,
                                color = SignalAmber
                            )
                            Text(
                                text = "Scan to load this week's featured dossier with links, photos & GPS site data.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ParchmentSecondary
                            )
                        }
                        IconButton(
                            onClick = onRefreshSpotlight,
                            modifier = Modifier.testTag("refresh_spotlight_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Refresh Conspiracy of the Week",
                                tint = SignalAmber
                            )
                        }
                    }
                }
            }
        }

        // Chronological Controls: Topic Filter Bar + GPS Filter + Sort Direction
        item(key = "timeline_controls") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "BROWSE BY TOPIC & TIMELINE",
                        style = MaterialTheme.typography.labelMedium,
                        color = ParchmentMuted
                    )
                    Surface(
                        onClick = onToggleSortOrder,
                        shape = RoundedCornerShape(8.dp),
                        color = ArchiveCardElevated,
                        border = BorderStroke(1.dp, ArchiveBorder),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("sort_toggle_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SwapVert,
                                contentDescription = "Toggle chronological sort",
                                tint = SignalAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (sortOrder == ChronologicalOrder.NEWEST_FIRST) {
                                    "Newest -> Oldest"
                                } else {
                                    "Oldest -> Newest"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = ParchmentCream
                            )
                        }
                    }
                }

                TopicFilterBar(
                    selectedTopic = selectedCategory,
                    topicCounts = topicCounts,
                    onSelectTopic = onSelectCategory,
                    testTagPrefix = "feed_topic"
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = onlyGpsMapped,
                        onClick = onToggleOnlyGpsMapped,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.MyLocation,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "GPS Sites Only",
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SignalAmberContainer,
                            selectedLabelColor = SignalAmberSoft,
                            selectedLeadingIconColor = SignalAmber,
                            containerColor = ArchiveSlate,
                            labelColor = ParchmentMuted,
                            iconColor = ParchmentMuted
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = onlyGpsMapped,
                            borderColor = ArchiveBorder,
                            selectedBorderColor = SignalAmber.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("gps_filter_chip")
                    )

                    if (selectedCategory != TheoryTopic.ALL.label || onlyGpsMapped) {
                        Surface(
                            onClick = onResetFilters,
                            shape = RoundedCornerShape(8.dp),
                            color = ArchiveCardElevated,
                            border = BorderStroke(1.dp, ArchiveBorder),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("clear_feed_filters_button")
                        ) {
                            Text(
                                text = "Reset Filters",
                                style = MaterialTheme.typography.labelMedium,
                                color = SignalAmber,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Empty State or Chronological Timeline Cards
        if (dossiers.isEmpty()) {
            item(key = "empty_feed") {
                EmptyArchiveState(
                    selectedCategory = selectedCategory,
                    hasActiveFilter = selectedCategory != TheoryTopic.ALL.label || onlyGpsMapped,
                    onScanTopic = {
                        if (selectedCategory != TheoryTopic.ALL.label) {
                            onScanTopic(selectedCategory)
                        } else {
                            onRunSearch(
                                "Conspiracy of the week and notable web conspiracy theories 2025 2026",
                                true
                            )
                        }
                    },
                    onResetFilters = onResetFilters
                )
            }
        } else {
            itemsIndexed(
                items = dossiers,
                key = { _, item -> item.id }
            ) { index, dossier ->
                TimelineDossierRow(
                    dossier = dossier,
                    isFirst = index == 0,
                    isLast = index == dossiers.lastIndex,
                    onClick = { onOpenDossier(dossier.id) },
                    onToggleBookmark = { onToggleBookmark(dossier) },
                    onShare = { onShareDossier(dossier) },
                    onOpenGps = {
                        val lat = dossier.latitude
                        val lon = dossier.longitude
                        if (lat != null && lon != null) {
                            openGpsInMaps(context, lat, lon, dossier.locationName ?: dossier.title)
                        }
                    },
                    onOpenSource = { url -> openExternalUrl(context, url) }
                )
            }
        }

        if (latestQueries.isNotEmpty()) {
            item(key = "grounding_footer") {
                GroundingAttributionFooter(queries = latestQueries)
            }
        }
    }
}

@Composable
private fun WeeklySpotlightCard(
    dossier: TheoryDossier,
    isScanning: Boolean,
    onRefreshSpotlight: () -> Unit,
    onOpenDossier: () -> Unit,
    onToggleBookmark: () -> Unit,
    onShare: () -> Unit,
    onOpenGps: () -> Unit,
    onOpenSource: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = ArchiveCard,
        border = BorderStroke(1.5.dp, SignalAmber.copy(alpha = 0.65f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("weekly_spotlight_card")
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onOpenDossier)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
            ) {
                DossierCoverImage(
                    imageUrl = dossier.imageUrl,
                    fallbackResId = R.drawable.img_weekly_spotlight,
                    contentDescription = dossier.title,
                    modifier = Modifier.fillMaxSize()
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SignalAmber
                    ) {
                        Text(
                            text = stringResource(R.string.conspiracy_of_the_week),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = ArchiveObsidian,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = onRefreshSpotlight,
                            enabled = !isScanning,
                            modifier = Modifier
                                .background(ArchiveObsidian.copy(alpha = 0.7f), CircleShape)
                                .testTag("refresh_spotlight_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Refresh weekly spotlight",
                                tint = SignalAmber
                            )
                        }
                        IconButton(
                            onClick = onShare,
                            modifier = Modifier
                                .background(ArchiveObsidian.copy(alpha = 0.7f), CircleShape)
                                .testTag("share_spotlight_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Share,
                                contentDescription = "Share weekly spotlight",
                                tint = ParchmentCream
                            )
                        }
                        IconButton(
                            onClick = onToggleBookmark,
                            modifier = Modifier.background(ArchiveObsidian.copy(alpha = 0.7f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (dossier.isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                                contentDescription = "Bookmark spotlight",
                                tint = if (dossier.isBookmarked) SignalAmber else ParchmentCream
                            )
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    CategoryBadge(label = dossier.chronologicalDateLabel, accentColor = SignalAmberSoft)
                    CategoryBadge(label = dossier.normalizedTopic, accentColor = VerifyTeal)
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = dossier.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = ParchmentCream
                )
                if (!dossier.weeklySpotlightNote.isNullOrBlank()) {
                    Text(
                        text = dossier.weeklySpotlightNote,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SignalAmberSoft
                    )
                }
                Text(
                    text = dossier.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ParchmentSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                if (dossier.recentUpdates.isNotBlank()) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Rounded.Update,
                            contentDescription = "Latest update",
                            tint = SignalAmber,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Update: ${dossier.recentUpdates}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ParchmentCream.copy(alpha = 0.9f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                dossier.formattedGps?.let { gps ->
                    GpsCoordinatePill(
                        formattedGps = gps,
                        locationName = dossier.locationName,
                        onClick = onOpenGps
                    )
                }

                if (dossier.dangersAndLaws.isNotEmpty()) {
                    LawsAndDangersBadge(
                        topNotice = dossier.dangersAndLaws.first(),
                        totalCount = dossier.dangersAndLaws.size
                    )
                }

                SourceLinksStrip(
                    sources = dossier.sources,
                    onSourceClick = onOpenSource
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Inspect Dossier",
                        style = MaterialTheme.typography.labelLarge,
                        color = SignalAmber
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = SignalAmber,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineDossierRow(
    dossier: TheoryDossier,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    onToggleBookmark: () -> Unit,
    onShare: () -> Unit,
    onOpenGps: () -> Unit,
    onOpenSource: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dossier_card_${dossier.id}")
    ) {
        // Chronological Red/Amber Thread Spine
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(52.dp)
                .padding(top = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(14.dp)
                    .background(if (isFirst) Color.Transparent else CrimsonThread.copy(alpha = 0.45f))
            )
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = ArchiveCardElevated,
                border = BorderStroke(1.dp, CrimsonThread.copy(alpha = 0.7f))
            ) {
                Text(
                    text = dossier.originYear.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = SignalAmberSoft,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(160.dp)
                        .background(CrimsonThread.copy(alpha = 0.25f))
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Dossier Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = ArchiveCard,
            border = BorderStroke(1.dp, ArchiveBorder),
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(14.dp)
            ) {
                // Header Row: Case Code + Date + Bookmark
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = dossier.caseCode,
                            style = MaterialTheme.typography.labelMedium,
                            color = ParchmentMuted
                        )
                        CategoryBadge(label = dossier.normalizedTopic, accentColor = VerifyTeal)
                    }
                    Row {
                        IconButton(
                            onClick = onShare,
                            modifier = Modifier.testTag("share_${dossier.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Share,
                                contentDescription = "Share dossier card",
                                tint = ParchmentMuted
                            )
                        }
                        IconButton(
                            onClick = onToggleBookmark,
                            modifier = Modifier.testTag("bookmark_${dossier.id}")
                        ) {
                            Icon(
                                imageVector = if (dossier.isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                                contentDescription = if (dossier.isBookmarked) "Remove bookmark" else "Bookmark dossier",
                                tint = if (dossier.isBookmarked) SignalAmber else ParchmentMuted
                            )
                        }
                    }
                }

                // Cover Picture (Real Wikimedia image if found, else editorial fallback)
                DossierCoverImage(
                    imageUrl = dossier.imageUrl,
                    fallbackResId = R.drawable.img_archive_hero,
                    contentDescription = dossier.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(132.dp)
                )

                Text(
                    text = "${dossier.chronologicalDateLabel} · ${dossier.tractionStatus.uppercase()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = SignalAmber
                )

                Text(
                    text = dossier.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = ParchmentCream
                )

                Text(
                    text = dossier.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ParchmentSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                if (dossier.recentUpdates.isNotBlank()) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Rounded.Update,
                            contentDescription = "Latest update",
                            tint = SignalAmber,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = dossier.recentUpdates,
                            style = MaterialTheme.typography.bodySmall,
                            color = ParchmentCream.copy(alpha = 0.88f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                dossier.formattedGps?.let { gps ->
                    GpsCoordinatePill(
                        formattedGps = gps,
                        locationName = dossier.locationName,
                        onClick = onOpenGps
                    )
                }

                if (dossier.dangersAndLaws.isNotEmpty()) {
                    LawsAndDangersBadge(
                        topNotice = dossier.dangersAndLaws.first(),
                        totalCount = dossier.dangersAndLaws.size
                    )
                }

                SourceLinksStrip(
                    sources = dossier.sources,
                    onSourceClick = onOpenSource
                )
            }
        }
    }
}

@Composable
private fun EmptyArchiveState(
    selectedCategory: String,
    hasActiveFilter: Boolean,
    onScanTopic: () -> Unit,
    onResetFilters: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ArchiveCard,
        border = BorderStroke(1.dp, ArchiveBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(20.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_empty_radar),
                contentDescription = "Empty archive radar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Text(
                text = if (hasActiveFilter) {
                    "No $selectedCategory Dossiers Matched"
                } else {
                    stringResource(R.string.empty_feed_title)
                },
                style = MaterialTheme.typography.headlineSmall,
                color = ParchmentCream
            )
            Text(
                text = if (hasActiveFilter) {
                    "Run a live web scan to discover and summarize $selectedCategory conspiracy theories, or reset your active filters."
                } else {
                    stringResource(R.string.empty_feed_subtitle)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = ParchmentSecondary
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onScanTopic,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SignalAmber,
                        contentColor = ArchiveObsidian
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                ) {
                    Text(
                        text = if (selectedCategory != TheoryTopic.ALL.label) {
                            "Scan Web: $selectedCategory"
                        } else {
                            "Run Web Scan"
                        }
                    )
                }
                if (hasActiveFilter) {
                    androidx.compose.material3.OutlinedButton(
                        onClick = onResetFilters,
                        border = BorderStroke(1.dp, ArchiveBorder),
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Text("Reset", color = ParchmentCream)
                    }
                }
            }
        }
    }
}
