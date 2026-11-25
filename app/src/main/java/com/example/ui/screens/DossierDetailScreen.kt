package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.OfflinePin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import com.example.R
import com.example.data.model.TheoryDossier
import com.example.data.model.TheoryTopic
import com.example.ui.components.CategoryBadge
import com.example.ui.components.topicIconFor
import com.example.ui.components.DossierCoverImage
import com.example.ui.components.GroundingAttributionFooter
import com.example.ui.components.OfflineModeBanner
import com.example.ui.components.OfflineReadyBadge
import com.example.ui.components.SourceLinksStrip
import com.example.ui.components.WarningBanner
import com.example.ui.components.openExternalUrl
import com.example.ui.components.openGpsInMaps
import com.example.ui.theme.ArchiveBorder
import com.example.ui.theme.ArchiveCard
import com.example.ui.theme.ArchiveCardElevated
import com.example.ui.theme.ArchiveObsidian
import com.example.ui.theme.ArchiveSlate
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonSoft
import com.example.ui.theme.CrimsonThread
import com.example.ui.theme.ParchmentCream
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.ParchmentSecondary
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.SignalAmberSoft
import com.example.ui.theme.VerifyTeal
import com.example.ui.theme.VerifyTealContainer
import com.example.ui.viewmodel.LiveStreamState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DossierDetailScreen(
    dossier: TheoryDossier?,
    isOnline: Boolean,
    bookmarkedCount: Int,
    streamState: LiveStreamState,
    onBack: () -> Unit,
    onToggleBookmark: (TheoryDossier) -> Unit,
    onShareDossier: (TheoryDossier) -> Unit,
    onUpdateTopic: (TheoryDossier, TheoryTopic) -> Unit,
    onSaveStreamedUpdateOffline: (TheoryDossier, String) -> Unit,
    onStreamUpdate: (TheoryDossier, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var followUpQuestion by remember { mutableStateOf("") }

    Scaffold(
        containerColor = ArchiveObsidian,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = dossier?.caseCode ?: "DOSSIER",
                            style = MaterialTheme.typography.labelMedium,
                            color = SignalAmber
                        )
                        if (dossier?.isBookmarked == true) {
                            OfflineReadyBadge(label = "SAVED OFFLINE")
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back to feed",
                            tint = ParchmentCream
                        )
                    }
                },
                actions = {
                    if (dossier != null) {
                        IconButton(
                            onClick = { onShareDossier(dossier) },
                            modifier = Modifier.testTag("detail_share_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Share,
                                contentDescription = "Export and share dossier card",
                                tint = SignalAmber
                            )
                        }
                        IconButton(
                            onClick = { onToggleBookmark(dossier) },
                            modifier = Modifier.testTag("detail_bookmark_button")
                        ) {
                            Icon(
                                imageVector = if (dossier.isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                                contentDescription = if (dossier.isBookmarked) "Remove from offline vault" else "Bookmark for offline reading",
                                tint = if (dossier.isBookmarked) SignalAmber else ParchmentCream
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ArchiveObsidian
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (dossier == null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                CircularProgressIndicator(color = SignalAmber)
            }
            return@Scaffold
        }

        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (!isOnline) {
                item(key = "detail_offline_banner") {
                    OfflineModeBanner(savedCount = bookmarkedCount)
                }
            }

            // Hero Image & Metadata Badges
            item(key = "hero") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DossierCoverImage(
                        imageUrl = dossier.imageUrl,
                        fallbackResId = R.drawable.img_archive_hero,
                        contentDescription = dossier.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CategoryBadge(label = dossier.chronologicalDateLabel, accentColor = SignalAmber)
                        CategoryBadge(label = dossier.normalizedTopic, accentColor = VerifyTeal)
                        CategoryBadge(label = dossier.tractionStatus, accentColor = CrimsonSoft)
                    }

                    Text(
                        text = dossier.title,
                        style = MaterialTheme.typography.displaySmall,
                        color = ParchmentCream
                    )

                    Text(
                        text = dossier.summary,
                        style = MaterialTheme.typography.bodyLarge,
                        color = ParchmentSecondary
                    )

                    OutlinedButton(
                        onClick = { onToggleBookmark(dossier) },
                        border = BorderStroke(
                            1.dp,
                            if (dossier.isBookmarked) VerifyTeal.copy(alpha = 0.6f) else SignalAmber.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("detail_offline_save_button")
                    ) {
                        Icon(
                            imageVector = if (dossier.isBookmarked) Icons.Rounded.OfflinePin else Icons.Rounded.BookmarkBorder,
                            contentDescription = null,
                            tint = if (dossier.isBookmarked) VerifyTeal else SignalAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (dossier.isBookmarked) {
                                "Saved in Room Vault for Offline Reading (Tap to Remove)"
                            } else {
                                "Bookmark for Offline Reading"
                            },
                            color = if (dossier.isBookmarked) VerifyTeal else SignalAmber
                        )
                    }

                    OutlinedButton(
                        onClick = { onShareDossier(dossier) },
                        border = BorderStroke(1.dp, ArchiveBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("detail_export_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = null,
                            tint = ParchmentCream,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Export & Share Summarized Card",
                            color = ParchmentCream
                        )
                    }

                    // Topic Categorization Selector
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ArchiveCard,
                        border = BorderStroke(1.dp, ArchiveBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "TOPIC CLASSIFICATION (TAP TO RECATEGORIZE)",
                                style = MaterialTheme.typography.labelSmall,
                                color = ParchmentMuted
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                TheoryTopic.selectableTopics.forEach { topic ->
                                    val selected = dossier.normalizedTopic.equals(topic.label, ignoreCase = true)
                                    FilterChip(
                                        selected = selected,
                                        onClick = { onUpdateTopic(dossier, topic) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = topicIconFor(topic.label),
                                                contentDescription = null,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = topic.label,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = VerifyTealContainer,
                                            selectedLabelColor = VerifyTeal,
                                            selectedLeadingIconColor = VerifyTeal,
                                            containerColor = ArchiveSlate,
                                            labelColor = ParchmentMuted,
                                            iconColor = ParchmentMuted
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = selected,
                                            borderColor = ArchiveBorder,
                                            selectedBorderColor = VerifyTeal.copy(alpha = 0.6f)
                                        ),
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("detail_topic_${topic.label.lowercase().replace(" ", "_")}")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Specific GPS Coordinates Card (if available)
            dossier.formattedGps?.let { gps ->
                item(key = "gps_card") {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = VerifyTealContainer.copy(alpha = 0.65f),
                        border = BorderStroke(1.dp, VerifyTeal.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.MyLocation,
                                        contentDescription = "GPS Site",
                                        tint = VerifyTeal,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(R.string.gps_title).uppercase(),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = VerifyTeal
                                    )
                                }
                                Text(
                                    text = gps,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ParchmentCream
                                )
                                if (!dossier.locationName.isNullOrBlank()) {
                                    Text(
                                        text = dossier.locationName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ParchmentSecondary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Button(
                                onClick = {
                                    val lat = dossier.latitude ?: return@Button
                                    val lon = dossier.longitude ?: return@Button
                                    openGpsInMaps(context, lat, lon, dossier.locationName ?: dossier.title)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VerifyTeal,
                                    contentColor = ArchiveObsidian
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("open_gps_button")
                            ) {
                                Text(
                                    text = stringResource(R.string.open_in_maps),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }

            // Saved Offline Live Notes (persisted in Room)
            if (!dossier.savedLiveNotes.isNullOrBlank()) {
                item(key = "saved_live_notes") {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = VerifyTealContainer.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, VerifyTeal.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.OfflinePin,
                                    contentDescription = "Saved offline update",
                                    tint = VerifyTeal,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SAVED OFFLINE UPDATE NOTES",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = VerifyTeal
                                )
                            }
                            Text(
                                text = dossier.savedLiveNotes,
                                style = MaterialTheme.typography.bodyMedium,
                                color = ParchmentCream
                            )
                        }
                    }
                }
            }

            // Dangers, Private Property & Applicable Laws
            if (dossier.dangersAndLaws.isNotEmpty()) {
                item(key = "laws_dangers") {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = CrimsonContainer.copy(alpha = 0.72f),
                        border = BorderStroke(1.dp, CrimsonThread.copy(alpha = 0.55f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Gavel,
                                    contentDescription = "Laws and Dangers",
                                    tint = CrimsonSoft,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "DANGERS, PRIVATE PROPERTY & APPLICABLE LAWS",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CrimsonSoft
                                )
                            }
                            dossier.dangersAndLaws.forEach { lawOrDanger ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Text(
                                        text = "!",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = CrimsonThread,
                                        modifier = Modifier.padding(end = 8.dp, top = 2.dp)
                                    )
                                    Text(
                                        text = lawOrDanger,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = ParchmentCream
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Latest Updates + Live Web Stream Search
            item(key = "updates_and_stream") {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = ArchiveCard,
                    border = BorderStroke(1.dp, SignalAmber.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Update,
                                contentDescription = "Latest Web Updates",
                                tint = SignalAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "LATEST WEB UPDATES & LIVE SCAN",
                                style = MaterialTheme.typography.labelMedium,
                                color = SignalAmber
                            )
                        }

                        if (dossier.recentUpdates.isNotBlank()) {
                            Text(
                                text = dossier.recentUpdates,
                                style = MaterialTheme.typography.bodyMedium,
                                color = ParchmentCream
                            )
                        }

                        OutlinedTextField(
                            value = followUpQuestion,
                            onValueChange = { followUpQuestion = it },
                            placeholder = {
                                Text("Optional: ask for specific updates, laws, or developments...")
                            },
                            singleLine = true,
                            enabled = isOnline,
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
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                onStreamUpdate(
                                    dossier,
                                    followUpQuestion.trim().takeIf { it.isNotBlank() }
                                )
                            },
                            enabled = isOnline &&
                                streamState !is LiveStreamState.Connecting &&
                                streamState !is LiveStreamState.Streaming,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SignalAmber,
                                contentColor = ArchiveObsidian
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("stream_updates_button")
                        ) {
                            if (streamState is LiveStreamState.Connecting ||
                                streamState is LiveStreamState.Streaming
                            ) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    color = ArchiveObsidian,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Searching Live Web...")
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (isOnline) "Search Fresh Updates & Legal Notices"
                                    else "Reconnect to Search Fresh Updates"
                                )
                            }
                        }

                        when (streamState) {
                            is LiveStreamState.Streaming -> {
                                LiveStreamResultBox(
                                    text = streamState.text,
                                    sources = streamState.sources,
                                    onOpenSource = { openExternalUrl(context, it) }
                                )
                            }
                            is LiveStreamState.Success -> {
                                LiveStreamResultBox(
                                    text = streamState.text,
                                    sources = streamState.sources,
                                    onOpenSource = { openExternalUrl(context, it) }
                                )
                                if (!dossier.isBookmarked) {
                                    OutlinedButton(
                                        onClick = {
                                            onSaveStreamedUpdateOffline(dossier, streamState.text)
                                        },
                                        border = BorderStroke(1.dp, VerifyTeal.copy(alpha = 0.6f)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .minimumInteractiveComponentSize()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.OfflinePin,
                                            contentDescription = null,
                                            tint = VerifyTeal,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Bookmark & Save Update Offline",
                                            color = VerifyTeal
                                        )
                                    }
                                }
                                if (streamState.searchQueries.isNotEmpty()) {
                                    GroundingAttributionFooter(queries = streamState.searchQueries)
                                }
                            }
                            is LiveStreamState.Error -> {
                                WarningBanner(message = streamState.message)
                            }
                            else -> Unit
                        }
                    }
                }
            }

            // Origin & Chronological Evolution
            item(key = "origin_story") {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = ArchiveCard,
                    border = BorderStroke(1.dp, ArchiveBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text(
                            text = "ORIGIN & CHRONOLOGY",
                            style = MaterialTheme.typography.labelMedium,
                            color = SignalAmberSoft
                        )
                        Text(
                            text = dossier.originStory,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ParchmentSecondary
                        )
                    }
                }
            }

            // Key Claims
            if (dossier.keyClaims.isNotEmpty()) {
                item(key = "key_claims") {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = ArchiveCard,
                        border = BorderStroke(1.dp, ArchiveBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Text(
                                text = "CORE CLAIMS CIRCULATED",
                                style = MaterialTheme.typography.labelMedium,
                                color = ParchmentMuted
                            )
                            dossier.keyClaims.forEachIndexed { idx, claim ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Text(
                                        text = "0${idx + 1}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = SignalAmber,
                                        modifier = Modifier.padding(end = 10.dp, top = 2.dp)
                                    )
                                    Text(
                                        text = claim,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = ParchmentCream
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Verified Context & Fact-Check
            if (dossier.factualContext.isNotBlank()) {
                item(key = "factual_context") {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = VerifyTealContainer.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, VerifyTeal.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.VerifiedUser,
                                    contentDescription = "Verified Context",
                                    tint = VerifyTeal,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.verified_context_title).uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = VerifyTeal
                                )
                            }
                            Text(
                                text = dossier.factualContext,
                                style = MaterialTheme.typography.bodyMedium,
                                color = ParchmentCream
                            )
                        }
                    }
                }
            }

            // Clickable Web Sources
            if (dossier.sources.isNotEmpty()) {
                item(key = "sources_list") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.sources_title).uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = ParchmentMuted
                        )
                        dossier.sources.forEach { source ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ArchiveCardElevated,
                                border = BorderStroke(1.dp, ArchiveBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { openExternalUrl(context, source.url) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = source.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = ParchmentCream,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = source.domain,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SignalAmber
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                                        contentDescription = "Open source link",
                                        tint = SignalAmber,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (dossier.searchQueries.isNotEmpty()) {
                item(key = "detail_grounding") {
                    GroundingAttributionFooter(queries = dossier.searchQueries)
                }
            }
        }
    }
}

@Composable
private fun LiveStreamResultBox(
    text: String,
    sources: List<com.example.data.model.TheorySource>,
    onOpenSource: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(ArchiveObsidian, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = ParchmentCream
        )
        if (sources.isNotEmpty()) {
            SourceLinksStrip(sources = sources, onSourceClick = onOpenSource)
        }
    }
}
