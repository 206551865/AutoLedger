package com.autoledger.app.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.autoledger.app.ui.theme.YS
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

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
        Text("我的", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = YS.ink,
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp))

        // 品牌头卡
        Box(Modifier.fillMaxWidth().padding(horizontal = 22.dp)
            .background(YS.brand, YS.rHero).padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).background(Color.White.copy(0.15f), YS.rIcon),
                    contentAlignment = Alignment.Center) {
                    Text("✦", color = Color.White, fontSize = 24.sp)
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("有数 · Youshu", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("心中有数，每一笔都从容", fontSize = 11.sp, color = Color.White.copy(0.75f))
                }
            }
        }

        // 权限卡片
        Box(
            Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 14.dp)
                .background(if (granted) YS.brandTint else YS.expenseTint, YS.rCard)
                .padding(16.dp)
                .clickable { if (!granted) openNotificationAccessSettings(context) }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(34.dp).background(if (granted) YS.brand else YS.expense, YS.rIcon),
                    contentAlignment = Alignment.Center) {
                    Text(if (granted) "✓" else "!", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        if (granted) "通知读取权限：已开启" else "通知读取权限：未开启",
                        fontWeight = FontWeight.Bold, fontSize = 14.sp, color = YS.ink
                    )
                    Text(
                        if (granted) "支付通知将自动记账" else "点击前往授权，开启后自动记账生效",
                        fontSize = 11.sp, color = YS.inkSoft
                    )
                }
            }
        }

        // 来源开关
        YsCard("自动记账来源") {
            SourceToggle.KNOWN_SOURCES.forEach { src ->
                var enabled by remember { mutableStateOf(SourceToggle.isSourceEnabled(src.id, context)) }
                Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(src.label, Modifier.weight(1f), fontSize = 14.sp, color = YS.ink)
                    Switch(
                        checked = enabled,
                        onCheckedChange = { enabled = it; SourceToggle.setEnabled(src.id, it, context) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = YS.brand,
                            uncheckedTrackColor = YS.surface3
                        )
                    )
                }
            }
        }

        // 功能入口
        YsCard("工具") {
            SettingsAction("🔍", "通知诊断", "查看支付通知为何没记账") { showDebugDialog = true }
            SettingsAction("🔄", "刷新权限状态", "重新检测通知使用权") {
                granted = SourceToggle.isNotificationAccessGranted(context)
                onPermissionChanged()
                Toast.makeText(context, "权限状态已刷新", Toast.LENGTH_SHORT).show()
            }
        }

        // 使用说明
        YsCard("如何使用") {
            listOf(
                "授予通知使用权，支付通知自动入账",
                "点底部中间 + ，用一句话记账",
                "账本页左滑可删除记录",
                "所有数据仅保存在本机，不上传云端"
            ).forEach {
                Text("· $it", fontSize = 12.sp, color = YS.inkSoft, modifier = Modifier.padding(vertical = 3.dp))
            }
        }

        Box(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 10.dp)
            .clickable { showClearDialog = true }) {
            Text("清空所有账单数据", color = YS.expense, fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 10.dp))
        }
        Text("有数 Youshu · v1.0", fontSize = 11.sp, color = YS.inkFaint,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("确认清空？", color = YS.ink) },
            text = { Text("这将删除本机所有账单记录，且无法恢复。", color = YS.inkSoft) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAll(); showClearDialog = false
                    Toast.makeText(context, "已清空", Toast.LENGTH_SHORT).show()
                }) { Text("确认清空", color = YS.expense) }
            },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("取消", color = YS.inkSoft) } }
        )
    }

    if (showDebugDialog) {
        val logs = NotificationDebugLog.read(context)
        val fmt = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault())
        AlertDialog(
            onDismissRequest = { showDebugDialog = false },
            title = { Text("通知诊断（最近 ${logs.size} 条）", color = YS.ink) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    val lastConn = NotificationDebugLog.lastConnected(context)
                    val connText = if (lastConn > 0L)
                        "✅ 监听服务已连接\n最后连接：${fmt.format(Date(lastConn))}"
                    else "❌ 监听服务从未连接\n请检查通知使用权与自启动权限"
                    Text(connText, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        color = if (lastConn > 0L) YS.income else YS.expense,
                        modifier = Modifier.padding(bottom = 12.dp))
                    if (logs.isEmpty()) {
                        Text("还没有收到任何支付类通知。\n\n请确认：\n1. 通知使用权已开启\n2. 来源开关已打开\n3. 做一笔微信支付后再查看",
                            fontSize = 12.sp, color = YS.inkSoft)
                    } else {
                        logs.forEach { entry ->
                            Column(Modifier.padding(vertical = 6.dp)) {
                                Text(
                                    "${fmt.format(Date(entry.time))}  " + if (entry.ok) "✅ ${entry.result}" else "⚠️ ${entry.result}",
                                    fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                    color = if (entry.ok) YS.income else YS.expense)
                                Text("来源: ${entry.pkg}", fontSize = 10.sp, color = YS.inkFaint)
                                Text("原文: ${entry.content}", fontSize = 10.sp, color = YS.inkFaint)
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
                }) { Text("清空日志", color = YS.brand) }
            },
            dismissButton = { TextButton(onClick = { showDebugDialog = false }) { Text("关闭", color = YS.inkSoft) } }
        )
    }
}

@Composable
private fun YsCard(title: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 7.dp)
        .background(YS.surface, YS.rCard)
        .border(1.dp, YS.line, YS.rCard).padding(16.dp)) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = YS.ink)
        Spacer(Modifier.size(8.dp))
        content()
    }
}

@Composable
private fun SettingsAction(emoji: String, title: String, sub: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(34.dp).background(YS.surface2, YS.rIcon), contentAlignment = Alignment.Center) {
            Text(emoji, fontSize = 16.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = YS.ink)
            Text(sub, fontSize = 10.5.sp, color = YS.inkFaint)
        }
        Text("›", fontSize = 16.sp, color = YS.inkFaint)
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
