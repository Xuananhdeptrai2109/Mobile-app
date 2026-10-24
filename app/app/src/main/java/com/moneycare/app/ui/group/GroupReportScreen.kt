package com.moneycare.app.ui.group

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneycare.app.data.model.GroupMember
import com.moneycare.app.ui.theme.DarkGreen
import com.moneycare.app.ui.theme.LightGreen
import com.moneycare.app.ui.theme.PrimaryGreen
import com.moneycare.app.ui.theme.SoftRed
import com.moneycare.app.ui.components.ErrorRetryContent
import com.moneycare.app.ui.state.LoadState
import com.moneycare.app.util.DebtSimplifier
import com.moneycare.app.util.toVnd
import com.moneycare.app.viewmodel.GroupReportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupReportScreen(
    groupId: String,
    onBackClick: () -> Unit,
    viewModel: GroupReportViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    var pendingCsv by remember { mutableStateOf<ByteArray?>(null) }
    val createDocument = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        val bytes = pendingCsv
        if (uri != null && bytes != null) {
            runCatching {
                val output = context.contentResolver.openOutputStream(uri)
                    ?: error("Không thể mở tệp đã chọn")
                output.use { it.write(bytes) }
            }.onSuccess {
                Toast.makeText(context, "Đã xuất báo cáo CSV", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, "Không thể lưu báo cáo", Toast.LENGTH_SHORT).show()
            }
        }
        pendingCsv = null
    }

    LaunchedEffect(groupId) { viewModel.load(groupId) }
    LaunchedEffect(state.errorMessage) {
        if (state.report != null) state.errorMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Báo cáo nhóm") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        when (val content = state.contentState) {
            LoadState.Loading -> Column(
                Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) { CircularProgressIndicator() }

            is LoadState.Error -> ErrorRetryContent(
                message = content.message,
                onRetry = { viewModel.load(groupId) },
                modifier = Modifier.padding(padding)
            )

            LoadState.Empty -> ErrorRetryContent(
                message = "Chưa có dữ liệu báo cáo",
                onRetry = { viewModel.load(groupId) },
                modifier = Modifier.padding(padding)
            )

            is LoadState.Success -> {
                val report = content.data
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(report.groupName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = LightGreen.copy(alpha = 0.45f))) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                ReportLine("Tổng chi", report.totalExpense.toVnd())
                                ReportLine("Đã thanh toán", report.settledAmount.toVnd())
                                ReportLine("Còn phải thanh toán", report.outstandingAmount.toVnd())
                                ReportLine("Khoản chi", report.expenseCount.toString())
                                ReportLine("Nhiệm vụ", "${report.completedTaskCount}/${report.taskCount} hoàn thành")
                            }
                        }
                    }
                    item {
                        val membersList = remember(report.members) {
                            report.members.map { GroupMember(id = it.memberId.toString(), name = it.fullName) }
                        }
                        val netBalancesMap = remember(report.members) {
                            report.members.associate { it.memberId.toString() to it.netBalance }
                        }
                        val settlements = remember(membersList, netBalancesMap) {
                            DebtSimplifier.simplifyDebts(membersList, netBalancesMap)
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Kế hoạch Quyết toán Nợ (Rút gọn tối ưu)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            if (settlements.isEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = PrimaryGreen)
                                        Text("Tất cả công nợ đã được thanh toán hoàn tất!", fontWeight = FontWeight.Medium)
                                    }
                                }
                            } else {
                                settlements.forEach { settle ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(settle.fromMemberName, fontWeight = FontWeight.Bold, color = SoftRed)
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(settle.amount.toVnd(), fontWeight = FontWeight.Bold, color = DarkGreen)
                                                Icon(
                                                    Icons.AutoMirrored.Filled.ArrowForward,
                                                    contentDescription = null,
                                                    tint = PrimaryGreen,
                                                    modifier = Modifier.padding(horizontal = 6.dp)
                                                )
                                            }
                                            Text(settle.toMemberName, fontWeight = FontWeight.Bold, color = DarkGreen)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item { Text("Chi tiết theo thành viên", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                    items(report.members, key = { it.memberId }) { member ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(member.fullName, fontWeight = FontWeight.Bold)
                                ReportLine("Đã ứng", member.totalPaid.toVnd())
                                ReportLine("Phần được chia", member.totalOwed.toVnd())
                                ReportLine("Còn nợ", member.outstandingAmount.toVnd())
                                ReportLine("Số dư ròng", member.netBalance.toVnd())
                            }
                        }
                    }
                    item {
                        Button(
                            onClick = {
                                viewModel.export(groupId) { bytes ->
                                    pendingCsv = bytes
                                    createDocument.launch("bao-cao-nhom-${report.groupId}.csv")
                                }
                            },
                            enabled = !state.isExporting,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (state.isExporting) CircularProgressIndicator(Modifier.padding(end = 8.dp))
                            else Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                            Text("Xuất CSV")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}
