package com.autoledger.app.data

/**
 * 自动分类规则：按商户名关键词归类。
 * 纯 Kotlin object，无 Android 依赖，便于 JVM 单元测试。
 */
object CategoryRules {

    /** 分类 -> 关键词列表（关键词出现在商户名中即命中） */
    private val RULES: List<Pair<String, List<String>>> = listOf(
        "餐饮" to listOf(
            "餐饮", "餐厅", "饭店", "美食", "外卖", "美团", "饿了么", "肯德基", "麦当劳",
            "星巴克", "瑞幸", "咖啡", "奶茶", "火锅", "烧烤", "食堂", "小吃", "面馆",
            "汉堡", "披萨", "寿司", "茶", "烘焙", "蛋糕", "bread", "coffee"
        ),
        "交通" to listOf(
            "打车", "滴滴", "出行", "地铁", "公交", "高铁", "火车", "12306", "航空",
            "机票", "加油", "中石化", "中石油", "停车", "高速", "ETC", "共享单车",
            "哈啰", "青桔", "曹操出行", "T3出行"
        ),
        "购物" to listOf(
            "淘宝", "天猫", "京东", "拼多多", "唯品会", "超市", "便利店", "商场",
            "服饰", "优衣库", "屈臣氏", "沃尔玛", "永辉", "家乐福", "盒马", "小米",
            "苹果", "苏宁", "国美", "购物", "商城", "支付宝付款"
        ),
        "生活缴费" to listOf(
            "水费", "电费", "燃气", "话费", "充值", "宽带", "物业费", "房租",
            "宽带费", "有线电视", "缴费"
        ),
        "娱乐" to listOf(
            "电影", "影院", "万达影城", "游戏", "Steam", "netflix", "视频会员",
            "KTV", "按摩", "健身", "瑜伽", "旅游", "酒店", "民宿", "门票", "演唱会"
        ),
        "医疗" to listOf(
            "医院", "药房", "药店", "诊所", "体检", "挂号", "医疗", "口腔", "眼科"
        ),
        "教育" to listOf(
            "书店", "图书", "培训", "学费", "课程", "网课", "教育", "学习", "考试"
        ),
        "转账红包" to listOf(
            "转账", "红包", "亲属卡", "朋友", "微信好友", "支付宝好友"
        ),
        "工资收入" to listOf(
            "工资", "薪资", "薪酬", "报销", "奖金", "劳务", "兼职"
        ),
        "理财" to listOf(
            "余额宝", "基金", "理财", "收益", "利息", "股票", "证券", "分红"
        )
    )

    /** 收入类分类（命中这些分类时图标/颜色区分） */
    val INCOME_CATEGORIES = setOf("工资收入", "理财", "转账红包")

    /**
     * 根据商户名与收支方向推断分类。
     * @param merchant 商户或对方名称
     * @param isIncome 是否为收入，收入时优先匹配收入类规则
     */
    fun classify(merchant: String, isIncome: Boolean): String {
        val lower = merchant.lowercase()
        // 收入时先按收入类关键词匹配
        if (isIncome) {
            for (category in INCOME_CATEGORIES) {
                val keywords = RULES.firstOrNull { it.first == category }?.second.orEmpty()
                if (keywords.any { lower.contains(it.lowercase()) }) return category
            }
        }
        for ((category, keywords) in RULES) {
            if (keywords.any { lower.contains(it.lowercase()) }) return category
        }
        return if (isIncome) "其他收入" else "其他支出"
    }

    /** 所有支出分类（用于统计页展示，不含其他收入） */
    fun expenseCategories(): List<String> = RULES.map { it.first }.filterNot { it in INCOME_CATEGORIES } + "其他支出"

    fun allCategories(): List<String> = RULES.map { it.first } + listOf("其他支出", "其他收入")
}
