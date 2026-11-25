package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.TheoryDossier
import com.example.share.DossierShareHelper
import com.example.ui.theme.ArchiveBorder
import com.example.ui.theme.ArchiveCard
import com.example.ui.theme.ArchiveCardElevated
import com.example.ui.theme.ArchiveObsidian
import com.example.ui.theme.ArchiveSlate
import com.example.ui.theme.ParchmentCream
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.ParchmentSecondary
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.VerifyTeal
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareDossierSheet(
    dossier: TheoryDossier,
    onDismiss: () -> Unit,
    onCopiedFeedback: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isExportingImage by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ArchiveSlate,
        contentColor = ParchmentCream
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "EXPORT & SHARE DOSSIER CARD",
                style = MaterialTheme.typography.labelMedium,
                color = SignalAmber
            )

            // Preview of the summarized card to be exported
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = ArchiveCard,
                border = BorderStroke(
                    1.5.dp,
                    if (dossier.isWeeklySpotlight) SignalAmber.copy(alpha = 0.7f) else ArchiveBorder
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(14.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (dossier.isWeeklySpotlight) {
                                "CONSPIRACY OF THE WEEK · ${dossier.caseCode}"
                            } else {
                                "CIPHERFEED · ${dossier.caseCode}"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = SignalAmber
                        )
                        CategoryBadge(
                            label = dossier.chronologicalDateLabel,
                            accentColor = VerifyTeal
                        )
                    }

                    Text(
                        text = dossier.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = ParchmentCream,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = dossier.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = ParchmentSecondary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    dossier.formattedGps?.let { gps ->
                        Text(
                            text = "GPS: $gps" + (dossier.locationName?.let { " · $it" } ?: ""),
                            style = MaterialTheme.typography.labelSmall,
                            color = VerifyTeal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (dossier.dangersAndLaws.isNotEmpty()) {
                        LawsAndDangersBadge(
                            topNotice = dossier.dangersAndLaws.first(),
                            totalCount = dossier.dangersAndLaws.size
                        )
                    }

                    if (dossier.sources.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ArchiveCardElevated,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Sources: ${dossier.sources.take(3).joinToString(" · ") { it.domain }}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ParchmentMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Primary: Export PNG Card + Caption to Social Media
            Button(
                onClick = {
                    if (!isExportingImage) {
                        scope.launch {
                            isExportingImage = true
                            try {
                                DossierShareHelper.exportAndShareCardImage(context, dossier)
                                onDismiss()
                            } catch (e: Exception) {
                                onCopiedFeedback("Failed to export card image: ${e.localizedMessage ?: "Unknown error"}")
                            } finally {
                                isExportingImage = false
                            }
                        }
                    }
                },
                enabled = !isExportingImage,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SignalAmber,
                    contentColor = ArchiveObsidian
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("share_card_image_button")
            ) {
                if (isExportingImage) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = ArchiveObsidian,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Rendering Card PNG...")
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Image,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share Visual Card (PNG + Caption)")
                }
            }

            // Secondary Row: Share Text Summary & Copy Summary
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = {
                        DossierShareHelper.shareTextSummary(context, dossier)
                        onDismiss()
                    },
                    border = BorderStroke(1.dp, ArchiveBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("share_text_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = null,
                        tint = ParchmentCream,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Text", color = ParchmentCream)
                }

                OutlinedButton(
                    onClick = {
                        DossierShareHelper.copySummaryToClipboard(context, dossier)
                        onCopiedFeedback("Copied dossier summary to clipboard")
                        onDismiss()
                    },
                    border = BorderStroke(1.dp, ArchiveBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("copy_summary_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = null,
                        tint = VerifyTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Post", color = VerifyTeal)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
