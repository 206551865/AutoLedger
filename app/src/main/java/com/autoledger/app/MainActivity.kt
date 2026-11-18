package com.autoledger.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.autoledger.app.service.SourceToggle
import com.autoledger.app.ui.AddTransactionDialog
import com.autoledger.app.ui.BillListScreen
import com.autoledger.app.ui.SettingsScreen
import com.autoledger.app.ui.StatisticsScreen
import com.autoledger.app.ui.theme.AutoLedgerTheme
import com.autoledger.app.viewmodel.LedgerViewModel

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Android 13+ 通知展示权限，结果无需强制处理 */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Android 13+ 需要动态申请「发送通知」权限（用于服务前台通知等）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            AutoLedgerTheme {
                AppRoot()
            }
        }
    }

    @Composable
    private fun AppRoot() {
        val viewModel: LedgerViewModel = viewModel()
        val transactions by viewModel.allTransactions.collectAsState()
        var selectedTab by remember { mutableStateOf(0) }
        var showAddDialog by remember { mutableStateOf(false) }
        var permissionTick by remember { mutableStateOf(0) }

        Scaffold(
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    TABS.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (selectedTab) {
                    0 -> BillListScreen(transactions, viewModel, onAddClick = { showAddDialog = true })
                    1 -> StatisticsScreen(transactions, viewModel)
                    2 -> SettingsScreen(viewModel, onPermissionChanged = { permissionTick++ })
                }
            }
        }

        if (showAddDialog) {
            AddTransactionDialog(
                onDismiss = { showAddDialog = false },
                onSave = { amount, merchant, type, category ->
                    viewModel.addManual(amount, merchant, type, category)
                }
            )
        }
    }

    private data class Tab(val label: String, val icon: ImageVector)

    private companion object {
        val TABS = listOf(
            Tab("账单", Icons.Default.List),
            Tab("统计", Icons.Default.BarChart),
            Tab("设置", Icons.Default.Settings)
        )
    }
}
