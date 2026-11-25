package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.OfflinePin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.ChronologicalOrder
import com.example.data.model.TheoryDossier
import com.example.ui.components.CategoryBadge
import com.example.ui.components.DossierCoverImage
import com.example.ui.components.GpsCoordinatePill
import com.example.ui.components.LawsAndDangersBadge
import com.example.ui.components.OfflineModeBanner
import com.example.ui.components.OfflineReadyBadge
import com.example.ui.components.SourceLinksStrip
import com.example.ui.components.TopicFilterBar
import com.example.ui.components.openExternalUrl
import com.example.ui.components.openGpsInMaps
import com.example.ui.theme.ArchiveBorder
import com.example.ui.theme.ArchiveCard
import com.example.ui.theme.ArchiveCardElevated
import com.example.ui.theme.ArchiveSlate
import com.example.ui.theme.ParchmentCream
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.ParchmentSecondary
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.VerifyTeal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VaultScreen(
    bookmarkedDossiers: List<TheoryDossier>,
    totalBookmarkedCount: Int,
    totalArchivedCount: Int,
    isOnline: Boolean,
    vaultSearchQuery: String,
    vaultSelectedTopic: String,
    vaultTopicCounts: Map<String, Int>,
    vaultSortOrder: ChronologicalOrder,
    onVaultSearchQueryChange: (String) -> Unit,
    onSelectVaultTopic: (String) -> Unit,
    onToggleVaultSortOrder: () -> Unit,
    onOpenDossier: (String) -> Unit,
    onToggleBookmark: (TheoryDossier) -> Unit,
    onShareDossier: (TheoryDossier) -> Unit,
    onClearUnsavedCache: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mappedCount = bookmarkedDossiers.count { it.hasGpsCoordinates }
    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy", Locale.US) }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier.fillMaxSize()
    ) {
        item(key = "vault_header") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "OFFLINE ROOM ARCHIVE",
                            style = MaterialTheme.typography.labelMedium,
                            color = SignalAmber
                        )
                        Text(
                            text = "Saved Vault",
                            style = MaterialTheme.typography.displaySmall,
                            color = ParchmentCream
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ArchiveCardElevated,
                        border = BorderStroke(1.dp, VerifyTeal.copy(alpha = 0.5f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.OfflinePin,
                                contentDescription = "Offline ready",
                                tint = VerifyTeal,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "OFFLINE READY",
                                style = MaterialTheme.typography.labelSmall,
                                color = VerifyTeal
                            )
                        }
                    }
                }

                if (!isOnline) {
                    OfflineModeBanner(savedCount = totalBookmarkedCount)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    VaultStatTile(
                        label = "SAVED",
                        value = totalBookmarkedCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    VaultStatTile(
                        label = "GPS SITES",
                        value = mappedCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    VaultStatTile(
                        label = "FEED CACHE",
                        value = totalArchivedCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }

                if (totalBookmarkedCount > 0) {
                    OutlinedTextField(
                        value = vaultSearchQuery,
                        onValueChange = onVaultSearchQueryChange,
                        placeholder = {
                            Text(
                                text = "Search saved dossiers offline...",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Search saved bookmarks",
                                tint = VerifyTeal
                            )
                        },
                        trailingIcon = {
                            if (vaultSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { onVaultSearchQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Clear vault search",
                                        tint = ParchmentMuted
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VerifyTeal,
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
                            .fillMaxWidth()
                            .testTag("vault_search_input")
                    )

                    TopicFilterBar(
                        selectedTopic = vaultSelectedTopic,
                        topicCounts = vaultTopicCounts,
                        onSelectTopic = onSelectVaultTopic,
                        testTagPrefix = "vault_topic"
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${bookmarkedDossiers.size} OFFLINE DOSSIERS",
                            style = MaterialTheme.typography.labelMedium,
                            color = ParchmentMuted
                        )
                        Surface(
                            onClick = onToggleVaultSortOrder,
                            shape = RoundedCornerShape(8.dp),
                            color = ArchiveCardElevated,
                            border = BorderStroke(1.dp, ArchiveBorder),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("vault_sort_toggle")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.SwapVert,
                                    contentDescription = "Sort saved dossiers",
                                    tint = SignalAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (vaultSortOrder == ChronologicalOrder.NEWEST_FIRST) {
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
                }

                OutlinedButton(
                    onClick = onClearUnsavedCache,
                    border = BorderStroke(1.dp, ArchiveBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("clear_cache_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Clear unsaved cache",
                        tint = ParchmentSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Clear Unsaved Feed Cache (Keeps Vault)",
                        color = ParchmentSecondary
                    )
                }
            }
        }

        if (bookmarkedDossiers.isEmpty()) {
            item(key = "empty_vault") {
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
                            contentDescription = "Empty vault illustration",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Text(
                            text = if (vaultSearchQuery.isNotBlank()) {
                                "No Saved Matches"
                            } else {
                                stringResource(R.string.empty_vault_title)
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            color = ParchmentCream
                        )
                        Text(
                            text = if (vaultSearchQuery.isNotBlank()) {
                                "Try a different keyword or clear the offline vault search."
                            } else {
                                stringResource(R.string.empty_vault_subtitle)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = ParchmentSecondary
                        )
                    }
                }
            }
        } else {
            items(
                items = bookmarkedDossiers,
                key = { it.id }
            ) { dossier ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ArchiveCard,
                    border = BorderStroke(1.dp, ArchiveBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vault_card_${dossier.id}")
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onOpenDossier(dossier.id) }
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(14.dp)
                    ) {
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
                                CategoryBadge(label = dossier.chronologicalDateLabel, accentColor = SignalAmber)
                                OfflineReadyBadge()
                            }
                            Row {
                                IconButton(
                                    onClick = { onShareDossier(dossier) },
                                    modifier = Modifier.testTag("vault_share_${dossier.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Share,
                                        contentDescription = "Share dossier card",
                                        tint = ParchmentMuted
                                    )
                                }
                                IconButton(
                                    onClick = { onToggleBookmark(dossier) },
                                    modifier = Modifier.testTag("vault_unbookmark_${dossier.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Bookmark,
                                        contentDescription = "Remove from offline vault",
                                        tint = SignalAmber
                                    )
                                }
                            }
                        }

                        DossierCoverImage(
                            imageUrl = dossier.imageUrl,
                            fallbackResId = R.drawable.img_archive_hero,
                            contentDescription = dossier.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(124.dp)
                        )

                        Text(
                            text = dossier.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = ParchmentCream
                        )

                        dossier.bookmarkedAtMillis?.let { savedAt ->
                            Text(
                                text = "Saved locally ${dateFormatter.format(Date(savedAt))} · ${dossier.normalizedTopic}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ParchmentMuted
                            )
                        }

                        Text(
                            text = dossier.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ParchmentSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        dossier.formattedGps?.let { gps ->
                            GpsCoordinatePill(
                                formattedGps = gps,
                                locationName = dossier.locationName,
                                onClick = {
                                    val lat = dossier.latitude ?: return@GpsCoordinatePill
                                    val lon = dossier.longitude ?: return@GpsCoordinatePill
                                    openGpsInMaps(context, lat, lon, dossier.locationName ?: dossier.title)
                                }
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
                            onSourceClick = { url -> openExternalUrl(context, url) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VaultStatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = ArchiveCardElevated,
        border = BorderStroke(1.dp, ArchiveBorder),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = SignalAmber
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = ParchmentMuted
            )
        }
    }
}
