package com.autoledger.app.parser

import com.autoledger.app.data.TxType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class NotificationParserTest {

    private fun parse(text: String, pkg: String? = null) = NotificationParser.parse(text, pkg)

    // ---------- 微信支付 ----------
    @Test
    fun `微信支付 - 商户消费支出`() {
        // 注意：真实微信商户消费通知不含「收款通知」字样（那是他人向你收款/你收款），
        // 典型文案为「商户名称 + 支付金额」，此处按真实格式构造
        val r = parse("微信支付 商户名称：永辉超市 支付金额¥88.50", "com.tencent.mm")
        assertNotNull(r)
        assertEquals(88.50, r!!.amount, 0.001)
        assertEquals(TxType.EXPENSE, r.type)
        assertEquals(PaymentSource.WECHAT, r.source)
        assert(r.merchant.contains("永辉") || r.merchant.contains("超市"))
    }

    @Test
    fun `微信支付 - 向某人转账支出`() {
        val r = parse("微信支付 转账给张三 ¥200.00")
        assertNotNull(r)
        assertEquals(200.0, r!!.amount, 0.001)
        assertEquals(TxType.EXPENSE, r.type)
    }

    @Test
    fun `微信支付 - 收到转账收入`() {
        val r = parse("微信支付 收到转账 ¥500.00 来自李四")
        assertNotNull(r)
        assertEquals(500.0, r!!.amount, 0.001)
        assertEquals(TxType.INCOME, r.type)
    }

    @Test
    fun `微信支付 - 收款通知判为收入`() {
        val r = parse("微信支付收款通知 +¥66.00")
        assertNotNull(r)
        assertEquals(TxType.INCOME, r!!.type)
        assertEquals(66.0, r.amount, 0.001)
    }

    // ---------- 支付宝 ----------
    @Test
    fun `支付宝 - 付款成功支出`() {
        val r = parse("支付宝 付款成功 ¥35.60 收款方：麦当劳", "com.eg.android.AlipayGphone")
        assertNotNull(r)
        assertEquals(35.60, r!!.amount, 0.001)
        assertEquals(TxType.EXPENSE, r.type)
        assertEquals(PaymentSource.ALIPAY, r.source)
    }

    @Test
    fun `支付宝 - 消费元为单位`() {
        val r = parse("支付宝消费128元")
        assertNotNull(r)
        assertEquals(128.0, r!!.amount, 0.001)
        assertEquals(TxType.EXPENSE, r.type)
    }

    @Test
    fun `支付宝 - 退款到账收入`() {
        val r = parse("支付宝 退款成功 ¥19.90 已退回余额")
        assertNotNull(r)
        assertEquals(19.90, r!!.amount, 0.001)
        assertEquals(TxType.INCOME, r.type)
    }

    @Test
    fun `支付宝 - 余额宝收益收入`() {
        val r = parse("余额宝收益 ¥2.35 已到账")
        assertNotNull(r)
        assertEquals(2.35, r!!.amount, 0.001)
        assertEquals(TxType.INCOME, r.type)
    }

    // ---------- 银行卡 ----------
    @Test
    fun `银行卡 - 储蓄卡消费支出`() {
        val r = parse("招商银行 您尾号1234的储蓄卡账户消费人民币¥1,280.00元", "com.cmbchina.ccdplato.android.cmbank")
        assertNotNull(r)
        assertEquals(1280.0, r!!.amount, 0.001)
        assertEquals(TxType.EXPENSE, r.type)
        assertEquals(PaymentSource.BANK, r.source)
    }

    @Test
    fun `银行卡 - 收入人民币`() {
        val r = parse("工商银行 您尾号8888账户收入人民币¥5000.00元 余额10000")
        assertNotNull(r)
        assertEquals(5000.0, r!!.amount, 0.001)
        assertEquals(TxType.INCOME, r.type)
    }

    @Test
    fun `银行卡 - 信用卡还款支出`() {
        val r = parse("中国银行信用卡还款¥800.00成功")
        assertNotNull(r)
        assertEquals(800.0, r!!.amount, 0.001)
        assertEquals(TxType.EXPENSE, r.type)
    }

    // ---------- 降噪与边界 ----------
    @Test
    fun `验证码通知应被过滤`() {
        val r = parse("您的验证码是123456，请勿泄露")
        assertNull(r)
    }

    @Test
    fun `无金额通知应被过滤`() {
        val r = parse("微信支付 您有一笔新的账单待查看")
        assertNull(r)
    }

    @Test
    fun `空文本应被过滤`() {
        assertNull(parse(""))
        assertNull(parse("   "))
    }

    @Test
    fun `未知来源从文本兜底识别`() {
        val r = parse("微信支付 消费¥12.00")
        assertNotNull(r)
        assertEquals(PaymentSource.WECHAT, r!!.source)
    }

    @Test
    fun `余额在消费之前不应把余额当金额`() {
        val r = parse("工商银行 余额10000.00元 消费88.50元")
        assertNotNull(r)
        assertEquals(88.50, r!!.amount, 0.001)
        assertEquals(TxType.EXPENSE, r.type)
    }

    // ---------- AI 自然语言记账（设计稿默认提示语 + 快捷标签） ----------
    private fun nl(text: String) = NotificationParser.parseNaturalLanguage(text)

    @Test
    fun `AI - 默认提示语午饭星巴克38块`() {
        val r = nl("午饭 星巴克 38 块")
        assertNotNull(r)
        assertEquals(38.0, r!!.amount, 0.001)
        assertEquals(TxType.EXPENSE, r.type)
        assertEquals("星巴克", r.merchant)
    }

    @Test
    fun `AI - 快捷标签打车25块`() {
        val r = nl("打车 25 块")
        assertNotNull(r)
        assertEquals(25.0, r!!.amount, 0.001)
        assertEquals("打车", r.merchant)
    }

    @Test
    fun `AI - 快捷标签超市采购120`() {
        val r = nl("超市采购 120")
        assertNotNull(r)
        assertEquals(120.0, r!!.amount, 0.001)
    }

    @Test
    fun `AI - 快捷标签工资到账18000判收入`() {
        val r = nl("工资到账 18000")
        assertNotNull(r)
        assertEquals(18000.0, r!!.amount, 0.001)
        assertEquals(TxType.INCOME, r.type)
    }

    @Test
    fun `AI - 晚饭和小李吃了火锅花了168`() {
        val r = nl("晚饭和小李吃了火锅，花了 168")
        assertNotNull(r)
        assertEquals(168.0, r!!.amount, 0.001)
        assertEquals("火锅", r.merchant)
    }
}
