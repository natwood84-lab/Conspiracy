package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.TheorySource
import com.example.data.model.TheoryTopic
import com.example.ui.theme.ArchiveBorder
import com.example.ui.theme.ArchiveCardElevated
import com.example.ui.theme.ArchiveObsidian
import com.example.ui.theme.ArchiveSlate
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonSoft
import com.example.ui.theme.CrimsonThread
import com.example.ui.theme.ParchmentCream
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.SignalAmberContainer
import com.example.ui.theme.SignalAmberSoft
import com.example.ui.theme.VerifyTeal
import com.example.ui.theme.VerifyTealContainer

fun openExternalUrl(context: Context, url: String) {
    val cleanUrl = url.trim()
    if (!cleanUrl.startsWith("http")) return
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(cleanUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show()
    }
}

fun openGpsInMaps(context: Context, latitude: Double, longitude: Double, label: String?) {
    val encodedLabel = Uri.encode(label ?: "Conspiracy Site")
    val geoUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($encodedLabel)")
    try {
        val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(mapIntent)
    } catch (_: Exception) {
        openExternalUrl(context, "https://maps.google.com/?q=$latitude,$longitude")
    }
}

@Composable
fun DossierCoverImage(
    imageUrl: String?,
    @DrawableRes fallbackResId: Int,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.clip(RoundedCornerShape(12.dp))) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                placeholder = painterResource(id = fallbackResId),
                error = painterResource(id = fallbackResId),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(id = fallbackResId),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            ArchiveObsidian.copy(alpha = 0.72f)
                        )
                    )
                )
        )
    }
}

@Composable
fun GpsCoordinatePill(
    formattedGps: String,
    locationName: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = VerifyTealContainer.copy(alpha = 0.75f),
        border = BorderStroke(1.dp, VerifyTeal.copy(alpha = 0.45f)),
        modifier = modifier.minimumInteractiveComponentSize()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.MyLocation,
                contentDescription = "GPS Coordinates",
                tint = VerifyTeal,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = formattedGps,
                    style = MaterialTheme.typography.labelMedium,
                    color = VerifyTeal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!locationName.isNullOrBlank()) {
                    Text(
                        text = locationName,
                        style = MaterialTheme.typography.bodySmall,
                        color = ParchmentCream.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun LawsAndDangersBadge(
    topNotice: String,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = CrimsonContainer.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, CrimsonThread.copy(alpha = 0.45f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Gavel,
                contentDescription = "Legal and safety notice",
                tint = CrimsonSoft,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (totalCount > 1) "$topNotice (+${totalCount - 1} more)" else topNotice,
                style = MaterialTheme.typography.bodySmall,
                color = CrimsonSoft,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SourceLinksStrip(
    sources: List<TheorySource>,
    onSourceClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (sources.isEmpty()) return
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        sources.take(5).forEach { source ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = ArchiveCardElevated,
                border = BorderStroke(1.dp, ArchiveBorder),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSourceClick(source.url) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                        contentDescription = "Open external source",
                        tint = SignalAmber,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = source.domain.ifBlank { source.title },
                        style = MaterialTheme.typography.labelMedium,
                        color = ParchmentCream,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GroundingAttributionFooter(
    queries: List<String>,
    modifier: Modifier = Modifier
) {
    if (queries.isEmpty()) return
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = ArchiveCardElevated.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, ArchiveBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Google Search Grounding",
                    tint = SignalAmber,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "LIVE WEB GROUNDING QUERIES",
                    style = MaterialTheme.typography.labelMedium,
                    color = ParchmentMuted
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                queries.distinct().take(6).forEach { query ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ArchiveObsidian,
                        border = BorderStroke(1.dp, ArchiveBorder)
                    ) {
                        Text(
                            text = query,
                            style = MaterialTheme.typography.labelSmall,
                            color = ParchmentCream.copy(alpha = 0.85f),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryBadge(
    label: String,
    accentColor: Color = SignalAmber,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = accentColor.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = accentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun WarningBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CrimsonContainer,
        border = BorderStroke(1.dp, CrimsonThread.copy(alpha = 0.6f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.WarningAmber,
                contentDescription = "Error notice",
                tint = CrimsonSoft,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = ParchmentCream
            )
        }
    }
}

@Composable
fun OfflineModeBanner(
    savedCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = VerifyTealContainer.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, VerifyTeal.copy(alpha = 0.55f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.MyLocation,
                contentDescription = "Offline reading mode",
                tint = VerifyTeal,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Offline Mode · $savedCount bookmarked ${if (savedCount == 1) "dossier" else "dossiers"} ready for offline reading",
                style = MaterialTheme.typography.bodySmall,
                color = ParchmentCream
            )
        }
    }
}

@Composable
fun OfflineReadyBadge(
    label: String = "OFFLINE READY",
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = VerifyTealContainer,
        border = BorderStroke(1.dp, VerifyTeal.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = VerifyTeal,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

fun topicIconFor(topicLabel: String): ImageVector {
    return when (topicLabel) {
        TheoryTopic.HISTORY.label -> Icons.Rounded.HistoryEdu
        TheoryTopic.TECHNOLOGY.label -> Icons.Rounded.Memory
        TheoryTopic.SPACE.label -> Icons.Rounded.RocketLaunch
        TheoryTopic.POLITICS.label -> Icons.Rounded.AccountBalance
        TheoryTopic.SCIENCE.label -> Icons.Rounded.Science
        TheoryTopic.POP_CULTURE.label -> Icons.Rounded.Public
        else -> Icons.Rounded.Apps
    }
}

@Composable
fun TopicFilterBar(
    selectedTopic: String,
    topicCounts: Map<String, Int>,
    onSelectTopic: (String) -> Unit,
    modifier: Modifier = Modifier,
    testTagPrefix: String = "topic_filter"
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        TheoryTopic.entries.forEach { topic ->
            val selected = selectedTopic == topic.label
            val count = topicCounts[topic.label] ?: 0
            val labelText = if (count > 0) "${topic.label} ($count)" else topic.label
            FilterChip(
                selected = selected,
                onClick = { onSelectTopic(topic.label) },
                leadingIcon = {
                    Icon(
                        imageVector = topicIconFor(topic.label),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                label = {
                    Text(
                        text = labelText,
                        style = MaterialTheme.typography.labelLarge
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
                    selected = selected,
                    borderColor = ArchiveBorder,
                    selectedBorderColor = SignalAmber.copy(alpha = 0.65f)
                ),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("${testTagPrefix}_${topic.label.lowercase().replace(" ", "_")}")
            )
        }
    }
}


