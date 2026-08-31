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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.data.local.CircleMemberEntity
import com.example.data.local.CraftCircleEntity
import com.example.data.models.BulkAllocationItem
import com.example.data.models.BulkOrderRequestData
import com.example.data.service.StandardBulkOrderMatchingService
import com.example.ui.theme.*

@Composable
fun B2BBulkOrderScreen(
    circle: CraftCircleEntity,
    members: List<CircleMemberEntity>,
    onBack: () -> Unit,
    onSubmitBulkOrder: (BulkOrderRequestData, List<BulkAllocationItem>) -> Unit,
    modifier: Modifier = Modifier
) {
    var productRequirement by remember { mutableStateOf("500 Handwoven Raw Mulberry Silk Stoles with Zari Border") }
    var quantityText by remember { mutableStateOf("500") }
    var unitPriceText by remember { mutableStateOf("900") }
    var deadlineDaysText by remember { mutableStateOf("30") }
    var customizationNotes by remember { mutableStateOf("Gold temple motif border, custom embroidered hotel heritage logo tag, individual eco packaging.") }
    var technicalRequirements by remember { mutableStateOf("100% natural silk warp & weft, certified azo-free natural dyes, minimum 200 GSM density, GI Tagged.") }

    val quantity = quantityText.toIntOrNull() ?: 100
    val unitPrice = unitPriceText.toDoubleOrNull() ?: 900.0
    val totalBudget = quantity * unitPrice

    val matchingService = remember { StandardBulkOrderMatchingService() }

    // Dynamic AI Allocation list (transparent & editable by circle admin)
    var allocations by remember(quantity, unitPrice, members) {
        mutableStateOf(
            matchingService.computeTransparentAllocation(
                totalQuantity = quantity,
                targetUnitPrice = unitPrice,
                circle = circle,
                members = members
            )
        )
    }

    var isEditMode by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("B2B Bulk Institutional Order", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(circle.name, fontSize = 11.sp, color = TerracottaPrimary, fontWeight = FontWeight.SemiBold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_b2b_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Budget", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${String.format("%,.0f", totalBudget)}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TerracottaPrimary)
                        Text("${quantity} units @ ₹${unitPrice.toInt()}/pc", fontSize = 10.sp, color = SuccessGreen)
                    }

                    Button(
                        onClick = {
                            val requestData = BulkOrderRequestData(
                                craftCategory = circle.craftType,
                                productRequirement = productRequirement,
                                quantity = quantity,
                                targetUnitPrice = unitPrice,
                                totalBudget = totalBudget,
                                deadlineDays = deadlineDaysText.toIntOrNull() ?: 30,
                                deadlineDate = "30 Oct 2026",
                                customizationNotes = customizationNotes,
                                technicalRequirements = technicalRequirements
                            )
                            onSubmitBulkOrder(requestData, allocations)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        modifier = Modifier.testTag("btn_submit_bulk_order")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Submit Request", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(WarmBgLight)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            // Guild Capacity Header
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PeacockTealTertiary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Diversity3, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Pooling ${circle.memberCount} Artisans via ${circle.name}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "AI-assisted fair capacity distribution with verified GI certification.",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // 1. Requirements
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("1. Product & Customization Requirements", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    OutlinedTextField(
                        value = productRequirement,
                        onValueChange = { productRequirement = it },
                        label = { Text("Product Requirement") },
                        placeholder = { Text("e.g. 500 Handwoven Silk Stoles with Zari Border") },
                        minLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_bulk_product_req"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = customizationNotes,
                        onValueChange = { customizationNotes = it },
                        label = { Text("Customization & Branding") },
                        placeholder = { Text("Motif, logo embroidery, packaging...") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = technicalRequirements,
                        onValueChange = { technicalRequirements = it },
                        label = { Text("Technical Specs (GSM, Azo-free, GI Tag)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // 2. Quantity, Budget & Deadline
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("2. Quantity, Budget & Timeline", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = quantityText,
                            onValueChange = { quantityText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Quantity (Pcs)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_bulk_quantity"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = unitPriceText,
                            onValueChange = { unitPriceText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            label = { Text("Unit Target (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_bulk_unit_price"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = deadlineDaysText,
                            onValueChange = { deadlineDaysText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Deadline (Days)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // 3. AI-Assisted Transparent Allocation
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("3. Transparent Member Allocation", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Text("AI-assisted based on capacity, trust & history", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        TextButton(
                            onClick = { isEditMode = !isEditMode },
                            colors = ButtonDefaults.textButtonColors(contentColor = TerracottaPrimary)
                        ) {
                            Icon(imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isEditMode) "Done" else "Modify", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    allocations.forEachIndexed { index, alloc ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(TerracottaPrimary.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = alloc.artisanName.take(2).uppercase(),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TerracottaPrimary
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(alloc.artisanName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("Trust ${alloc.trustScore} • ${alloc.craftSpecialization}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    if (!isEditMode) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("${alloc.allocatedQuantity} pcs", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TerracottaPrimary)
                                            Text("₹${String.format("%,.0f", alloc.estimatedPayout)} payout", fontSize = 10.sp, color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                                        }
                                    } else {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = {
                                                    if (alloc.allocatedQuantity > 10) {
                                                        val updated = allocations.toMutableList()
                                                        val newQty = alloc.allocatedQuantity - 10
                                                        updated[index] = alloc.copy(
                                                            allocatedQuantity = newQty,
                                                            estimatedPayout = newQty * alloc.unitPayout
                                                        )
                                                        allocations = updated
                                                    }
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease", tint = TerracottaPrimary)
                                            }
                                            Text("${alloc.allocatedQuantity}", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 4.dp))
                                            IconButton(
                                                onClick = {
                                                    val updated = allocations.toMutableList()
                                                    val newQty = alloc.allocatedQuantity + 10
                                                    updated[index] = alloc.copy(
                                                        allocatedQuantity = newQty,
                                                        estimatedPayout = newQty * alloc.unitPayout
                                                    )
                                                    allocations = updated
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase", tint = TerracottaPrimary)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = alloc.aiReasoning,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
