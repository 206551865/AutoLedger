package com.autoledger.app.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autoledger.app.service.NotificationDebugLog
import com.autoledger.app.service.SourceToggle
import com.autoledger.app.viewmodel.LedgerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: LedgerViewModel, onPermissionChanged: () -> Unit) {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(SourceToggle.isNotificationAccessGranted(context)) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showDebugDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 权限卡片
            Box(
                Modifier.fillMaxWidth().background(
                    if (granted) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                    RoundedCornerShape(16.dp)
                ).padding(16.dp).clickable {
                    if (!granted) {
                        openNotificationAccessSettings(context)
                    }
                }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (granted) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (granted) Color(0xFF2E7D32) else Color(0xFFF57C00),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (granted) "通知读取权限：已开启" else "通知读取权限：未开启",
                            fontWeight = FontWeight.Bold, fontSize = 15.sp
                        )
                        Text(
                            if (granted) "支付通知将自动记账" else "点击前往授权，开启后自动记账生效",
                            fontSize = 12.sp, color = Color.Gray
                        )
                    }
                }
            }

            // 来源开关
            SectionCard("自动记账来源") {
                Spacer(Modifier.size(8.dp))
                SourceToggle.KNOWN_SOURCES.forEach { src ->
                    var enabled by remember { mutableStateOf(SourceToggle.isSourceEnabled(src.id, context)) }
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(src.label, Modifier.weight(1f), fontSize = 15.sp)
                        Switch(
                            checked = enabled,
                            onCheckedChange = {
                                enabled = it
                                SourceToggle.setEnabled(src.id, it, context)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White)
                        )
                    }
                }
            }

            // 使用说明
            SectionCard("如何使用") {
                Spacer(Modifier.size(8.dp))
                listOf(
                    "1. 点击上方卡片，授予「通知使用权」",
                    "2. 微信/支付宝/银行卡发生收支时，自动入账",
                    "3. 在「账单」页左滑可删除，右下角 + 可手动补记",
                    "4. 所有数据仅保存在本机，不上传云端"
                ).forEach {
                    Text(it, fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))
                }
            }

            // 通知诊断入口
            TextButton(
                onClick = { showDebugDialog = true },
                Modifier.fillMaxWidth()
            ) { Text("🔍 通知诊断（查看支付通知为何没记账）") }

            // 刷新权限状态
            TextButton(
                onClick = {
                    granted = SourceToggle.isNotificationAccessGranted(context)
                    onPermissionChanged()
                    Toast.makeText(context, "权限状态已刷新", Toast.LENGTH_SHORT).show()
                },
                Modifier.fillMaxWidth()
            ) { Text("刷新权限状态") }

            // 清空数据
            TextButton(
                onClick = { showClearDialog = true },
                Modifier.fillMaxWidth()
            ) { Text("清空所有账单数据", color = Color(0xFFC62828)) }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("确认清空？") },
            text = { Text("这将删除本机所有账单记录，且无法恢复。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAll()
                    showClearDialog = false
                    Toast.makeText(context, "已清空", Toast.LENGTH_SHORT).show()
                }) { Text("确认清空", color = Color(0xFFC62828)) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("取消") }
            }
        )
    }

    if (showDebugDialog) {
        val logs = NotificationDebugLog.read(context)
        val fmt = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault())
        AlertDialog(
            onDismissRequest = { showDebugDialog = false },
            title = { Text("通知诊断（最近 ${logs.size} 条）") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    if (logs.isEmpty()) {
                        Text(
                            "还没有收到任何支付类通知。\n\n" +
                                "请确认：\n" +
                                "1. 通知使用权已开启\n" +
                                "2. 上方「自动记账来源」开关已打开\n" +
                                "3. 然后做一笔微信支付，再回来查看",
                            fontSize = 13.sp, color = Color.Gray
                        )
                    } else {
                        logs.forEach { entry ->
                            Column(Modifier.padding(vertical = 6.dp)) {
                                Text(
                                    "${fmt.format(Date(entry.time))}  " +
                                        if (entry.ok) "✅ ${entry.result}" else "⚠️ ${entry.result}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (entry.ok) Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                                Text("来源: ${entry.pkg}", fontSize = 11.sp, color = Color.Gray)
                                Text("原文: ${entry.content}", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    NotificationDebugLog.clear(context)
                    Toast.makeText(context, "日志已清空", Toast.LENGTH_SHORT).show()
                    showDebugDialog = false
                }) { Text("清空日志") }
            },
            dismissButton = {
                TextButton(onClick = { showDebugDialog = false }) { Text("关闭") }
            }
        )
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().background(
            MaterialTheme.colorScheme.surface,
            RoundedCornerShape(16.dp)
        ).padding(16.dp)
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        content()
    }
}

private fun openNotificationAccessSettings(context: Context) {
    runCatching {
        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }.onFailure {
        runCatching {
            context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }
}
