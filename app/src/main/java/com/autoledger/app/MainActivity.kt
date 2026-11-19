package com.autoledger.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.autoledger.app.ui.AICaptureScreen
import com.autoledger.app.ui.BillListScreen
import com.autoledger.app.ui.OverviewScreen
import com.autoledger.app.ui.SettingsScreen
import com.autoledger.app.ui.StatisticsScreen
import com.autoledger.app.ui.theme.AutoLedgerTheme
import com.autoledger.app.ui.theme.YS
import com.autoledger.app.viewmodel.LedgerViewModel

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            AutoLedgerTheme { AppRoot() }
        }
    }

    @Composable
    private fun AppRoot() {
        val viewModel: LedgerViewModel = viewModel()
        val transactions by viewModel.allTransactions.collectAsState()
        var tab by remember { mutableStateOf(0) }
        var showAI by remember { mutableStateOf(false) }
        var tick by remember { mutableStateOf(0) }

        Box(Modifier.fillMaxSize()) {
            Scaffold(
                containerColor = YS.bg,
                bottomBar = {
                    YsBottomBar(selected = tab, onSelect = { tab = it }, onAdd = { showAI = true })
                }
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    when (tab) {
                        0 -> OverviewScreen(transactions, viewModel, onSeeAll = { tab = 3 })
                        1 -> StatisticsScreen(transactions, viewModel)
                        3 -> BillListScreen(transactions, viewModel)
                        4 -> SettingsScreen(viewModel, onPermissionChanged = { tick++ })
                    }
                }
            }

            if (showAI) {
                Box(Modifier.fillMaxSize().background(YS.bg)) {
                    AICaptureScreen(viewModel, onBack = { showAI = false })
                }
            }
        }
    }
}

@Composable
private fun YsBottomBar(selected: Int, onSelect: (Int) -> Unit, onAdd: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(62.dp).background(YS.surface)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(YS.line))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            NavItem(Icons.Default.Home, "总览", selected == 0, Modifier.weight(1f)) { onSelect(0) }
            NavItem(Icons.Default.BarChart, "分析", selected == 1, Modifier.weight(1f)) { onSelect(1) }
            // 中间凸起 +
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Box(
                    Modifier.size(44.dp).background(YS.brand, RoundedCornerShape(15.dp))
                        .clickable { onAdd() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "记一笔", tint = Color.White, modifier = Modifier.size(22.dp))
                }
            }
            NavItem(Icons.Default.List, "账本", selected == 3, Modifier.weight(1f)) { onSelect(3) }
            NavItem(Icons.Default.Person, "我的", selected == 4, Modifier.weight(1f)) { onSelect(4) }
        }
    }
}

@Composable
private fun NavItem(icon: ImageVector, label: String, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier.clickable { onClick() }.padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = if (active) YS.brand else YS.inkFaint,
            modifier = Modifier.size(21.dp))
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Medium,
            color = if (active) YS.brand else YS.inkFaint)
    }
}
