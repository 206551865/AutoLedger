package com.autoledger.app.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autoledger.app.data.TxType
import com.autoledger.app.ui.theme.YS
import com.autoledger.app.util.formatMoney
import com.autoledger.app.viewmodel.LedgerViewModel

@Composable
fun AICaptureScreen(
    viewModel: LedgerViewModel,
    onBack: () -> Unit
) {
    var text by remember { mutableStateOf("午饭 星巴克 38 块") }
    var result by remember { mutableStateOf<LedgerViewModel.NlParseResult?>(null) }
    var showToast by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // 顶部栏
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(34.dp).background(YS.surface, YS.rIcon)
                .border(1.dp, YS.line, YS.rIcon).clickable { onBack() },
                contentAlignment = Alignment.Center) {
                Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = YS.inkSoft, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text("AI 智能记账", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = YS.ink)
            Spacer(Modifier.weight(1f))
            Box(Modifier.size(34.dp).background(YS.surface, YS.rIcon)
                .border(1.dp, YS.line, YS.rIcon), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = YS.inkSoft, modifier = Modifier.size(16.dp))
            }
        }

        // AI orb
        Column(Modifier.fillMaxWidth().padding(top = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            val infinite = rememberInfiniteTransition(label = "orb")
            val pulse by infinite.animateFloat(
                initialValue = 1f,
                targetValue = 1.09f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1300),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse"
            )
            Box(Modifier.size(86.dp), contentAlignment = Alignment.Center) {
                Box(
                    Modifier.size(66.dp).scale(pulse).alpha(0.5f)
                        .border(1.5.dp, YS.brandGlow, CircleShape)
                )
                Box(
                    Modifier.size(66.dp).background(YS.brand, YS.rIcon),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✦", color = Color.White, fontSize = 30.sp)
                }
            }
            Spacer(Modifier.height(14.dp))
            Text("说一句话，自动记好账", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = YS.ink)
            Spacer(Modifier.height(5.dp))
            Text("试试「午饭 星巴克 38 块」", fontSize = 11.5.sp, color = YS.inkFaint)
        }

        // 输入框
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)
            .background(YS.surface, YS.rInput)
            .border(1.5.dp, YS.line, YS.rInput)
            .padding(14.dp)) {
            androidx.compose.material3.TextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("例如：晚饭和小李吃了火锅，花了 168…", fontSize = 13.sp, color = YS.inkFaint) },
                modifier = Modifier.fillMaxWidth().height(80.dp),
                colors = androidx.compose.material3.TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = YS.brand,
                    disabledIndicatorColor = Color.Transparent
                ),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = YS.ink)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(30.dp).background(YS.surface2, YS.rIcon), contentAlignment = Alignment.Center) {
                    Text("🎤", fontSize = 14.sp)
                }
                Spacer(Modifier.width(8.dp))
                Text("按住说话", fontSize = 10.sp, color = YS.inkFaint)
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier.background(YS.brand, YS.rIcon)
                        .clickable {
                            result = viewModel.parseNaturalLanguage(text)
                            showToast = false
                        }
                        .padding(horizontal = 15.dp, vertical = 9.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("AI 识别", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(5.dp))
                        Text("→", color = Color.White, fontSize = 13.sp)
                    }
                }
            }
        }

        // 快捷标签
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf("打车 25 块", "超市采购 120", "工资到账 18000").forEach { chip ->
                Box(
                    Modifier.background(YS.surface, CircleShape)
                        .border(1.dp, YS.line, CircleShape)
                        .clickable { text = chip; result = viewModel.parseNaturalLanguage(chip); showToast = false }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(chip, fontSize = 10.sp, color = YS.inkSoft)
                }
            }
        }

        // 解析结果卡
        result?.let { r ->
            Spacer(Modifier.height(18.dp))
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                    .background(YS.surface, YS.rCard)
                    .border(1.dp, YS.line, YS.rCard)
                    .padding(18.dp)
                    .padding(bottom = 18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("AI 已理解 ✓", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = YS.brand, letterSpacing = 0.5.sp)
                    Spacer(Modifier.weight(1f))
                    Text("置信度 98%", fontSize = 9.sp, color = YS.inkFaint)
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("¥", fontSize = 20.sp, color = YS.inkFaint)
                    Text(formatMoney(r.amount), fontFamily = YS.fontNum, fontSize = 36.sp, color = YS.ink, lineHeight = 40.sp)
                }
                Spacer(Modifier.height(14.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(YS.line.copy(0.7f)))
                Spacer(Modifier.height(12.dp))
                ResultField("商户", r.merchant)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("科目", fontSize = 10.5.sp, color = YS.inkFaint, modifier = Modifier.width(42.dp))
                    Box(Modifier.background(YS.brandTint, CircleShape).padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Text(r.category, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = YS.brand)
                    }
                }
                Spacer(Modifier.height(10.dp))
                ResultField("类型", if (r.type == TxType.INCOME) "收入" else "支出")
                Spacer(Modifier.height(16.dp))
                Box(
                    Modifier.fillMaxWidth().background(YS.ink, YS.rIcon)
                        .clickable {
                            viewModel.confirmNaturalLanguage(r)
                            showToast = true
                            result = null
                            text = ""
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("确认记账", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        } ?: run {
            // 未识别提示
            if (text.isNotBlank()) {
                Spacer(Modifier.height(18.dp))
                Text(
                    "点「AI 识别」解析这句话里的金额和商户",
                    fontSize = 11.sp, color = YS.inkFaint,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp)
                )
            }
        }

        Spacer(Modifier.height(100.dp))
    }

    if (showToast) {
        Box(Modifier.fillMaxSize().padding(bottom = 78.dp), contentAlignment = Alignment.BottomCenter) {
            Box(Modifier.background(YS.ink, CircleShape).padding(horizontal = 18.dp, vertical = 11.dp)) {
                Text("✓ 已记账 · 自动归类", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun ResultField(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 10.5.sp, color = YS.inkFaint, modifier = Modifier.width(42.dp))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = YS.ink)
    }
}
