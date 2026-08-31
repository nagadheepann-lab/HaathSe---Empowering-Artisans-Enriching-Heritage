@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.ui.screens.circles

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CraftCircleEntity
import com.example.data.models.CircleJoinFormData
import com.example.ui.theme.*

@Composable
fun JoinCircleDialog(
    circle: CraftCircleEntity,
    onDismiss: () -> Unit,
    onSubmit: (CircleJoinFormData) -> Unit
) {
    var specialization by remember { mutableStateOf(circle.craftType) }
    var experienceYears by remember { mutableStateOf("12") }
    var previousWorkDesc by remember { mutableStateOf("3rd generation master artisan trained in traditional handloom motifs and natural vegetable dyes.") }
    var monthlyCapacity by remember { mutableStateOf("40") }
    var availabilityTimeline by remember { mutableStateOf("Immediate") }
    var location by remember { mutableStateOf(circle.location) }
    var portfolioImages by remember { mutableStateOf(listOf("Zari Silk Pallu", "Natural Indigo Stole", "Pit-loom Fabric")) }

    var isSubmitting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.95f),
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = TerracottaPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Join Craft Circle",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = circle.name,
                    fontSize = 12.sp,
                    color = TerracottaPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Guild membership connects you to high-volume institutional B2B bulk orders with transparent revenue distribution.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )

                // 1. Craft Specialization
                OutlinedTextField(
                    value = specialization,
                    onValueChange = { specialization = it },
                    label = { Text("Craft Specialization") },
                    placeholder = { Text("e.g. Mulberry Silk, Blue Pottery, Brass Dhokra") },
                    leadingIcon = { Icon(Icons.Default.Palette, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_join_specialization"),
                    shape = RoundedCornerShape(10.dp)
                )

                // 2. Experience & Monthly Capacity Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = experienceYears,
                        onValueChange = { experienceYears = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Experience (Yrs)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_join_experience"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = monthlyCapacity,
                        onValueChange = { monthlyCapacity = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Capacity (Pcs/Mo)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_join_capacity"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // 3. Location
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Your Location / Village") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_join_location"),
                    shape = RoundedCornerShape(10.dp)
                )

                // 4. Availability Timeline
                Column {
                    Text("Availability Timeline", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Immediate", "Within 2 Weeks", "Next Month").forEach { option ->
                            FilterChip(
                                selected = availabilityTimeline == option,
                                onClick = { availabilityTimeline = option },
                                label = { Text(option, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // 5. Previous Work / Lineage
                OutlinedTextField(
                    value = previousWorkDesc,
                    onValueChange = { previousWorkDesc = it },
                    label = { Text("Previous Work & Heritage Lineage") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_join_previous_work"),
                    shape = RoundedCornerShape(10.dp)
                )

                // 6. Portfolio Photos / Verification Sample
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Previous Work Portfolio", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("${portfolioImages.size} Attached", fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        portfolioImages.forEach { item ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = TerracottaPrimary.copy(alpha = 0.1f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, TerracottaPrimary.copy(alpha = 0.3f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(item, fontSize = 10.sp, maxLines = 1, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isSubmitting = true
                    onSubmit(
                        CircleJoinFormData(
                            craftSpecialization = specialization,
                            experienceYears = experienceYears.toIntOrNull() ?: 5,
                            previousWorkDesc = previousWorkDesc,
                            productionCapacityMonthly = monthlyCapacity.toIntOrNull() ?: 30,
                            availabilityTimeline = availabilityTimeline,
                            location = location,
                            portfolioImages = portfolioImages
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_submit_join_request")
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Submit Request")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
