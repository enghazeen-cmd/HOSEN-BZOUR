package com.example.ui.theme

object StringsAr {

    private val translations = mapOf(
        "app_title" to Pair("Civic Flow", "بلدية الزبابدة - نظام إدارة المياه"),
        "municipality" to Pair("Zababdeh Municipality", "بلدية الزبابدة"),
        "sub_title" to Pair("Water Systems Portal", "بوابة المياه الرقمية"),
        
        // Roles Swapper
        "role" to Pair("System Role: ", "صلاحية الدخول: "),
        "reader_role" to Pair("Field Meter Reader", "جابي الحقل (الميدان)"),
        "manager_role" to Pair("Municipal Admin Panel", "الإدارة والمراقبة المباشرة"),
        
        // Navigation Options
        "nav_dashboard" to Pair("Dashboard", "الرئيسة والأرقام"),
        "nav_customers" to Pair("Customers", "المشتركون والعدادات"),
        "nav_scan" to Pair("AI OCR Scan", "قراءة بالذكاء الاصطناعي"),
        "nav_gis" to Pair("GIS Telemetry Map", "خريطة نظم المعلومات (GIS)"),
        
        // General Stats / Dashboard Indicators
        "total_active_meters" to Pair("Active Water Meters", "الاشتراكات النشطة"),
        "total_consumption" to Pair("Total Water Consumption", "إجمالي الاستهلاك العام"),
        "cubic_meters" to Pair("m³", "متر مكعب"),
        "leakage_alerts" to Pair("Active Leakage Suspects", "تنبيهات تسريب محتملة"),
        "sync_status" to Pair("Sync Station", "مزامنة البيانات"),
        "offline_readings" to Pair("Unsynchronized Local Readings", "قراءات محلية غير مخزنة سحابياً"),
        "sync_action" to Pair("Sync Now", "مزامنة الآن"),
        "all_synced" to Pair("All records in sync with municipality server", "جميع القراءات متزامنة مع السيرفر الرئيس"),
        
        // Zones Section
        "zone_title" to Pair("Zone Comparison Metrics", "مؤشرات أداء قطاعات المياه (Zone 1-7)"),
        "zone_avg" to Pair("Avg. Consumption per user", "معدل استهلاك المشترك"),
        "zone_customers" to Pair("Total Accounts", "عدد الحسابات في القطاع"),
        "leakages" to Pair("Leakages", "تسريبات نقالة"),
        "normal" to Pair("Normal", "طبيعي"),
        "abnormal" to Pair("Abnormal", "غير اعتيادي"),
        "suspended" to Pair("Suspended", "معطل / موقوف مؤقتاً"),
        "active" to Pair("Active", "نشط"),
        
        // Scanning interface
        "scanner_header" to Pair("AI Real-time Meter Digit OCR Scan", "مسح وقراءة العداد بالذكاء الاصطناعي"),
        "viewfinder_instruction" to Pair("Frame the mechanical water meter digits clearly. The AI system will analyze and extract current values.", "قم بتوجيه الكاميرا إلى خانات الأرقام داخل عداد المياه الميكانيكي لاستخراج القراءة."),
        "take_reading" to Pair("Scan & Parse Visual Dials", "صوّر واقرأ العداد"),
        "analyze_loading" to Pair("AI is scanning digits, validating consumption history...", "جاري معالجة الصورة عبر ذكاء Gemini الاصطناعي..."),
        "current_result" to Pair("Extracted Reading Result", "نتائج فحص العداد"),
        "ocr_confidence" to Pair("AI Extraction Certainty", "نسبة دقة الذكاء الاصطناعي"),
        "previous_label" to Pair("Previous Verified Reading: ", "القراءة المعتمدة السابقة: "),
        "detected_digits" to Pair("Identified Digits Value: ", "الأرقام المستخرجة: "),
        "sim_photo" to Pair("Simulate Photo Capture", "محاكاة التقاط صورة عداد"),
        "manual_override" to Pair("Manual Override Input", "إدخال يدوي بديل"),
        "input_placeholder" to Pair("Enter meter digits...", "أدخل رقم القراءة الحالي..."),
        "save_reading_btn" to Pair("Commit Reading to Ledger", "تثبيت القراءة واعتمادها"),
        "choose_customer_prompt" to Pair("1. First, select Customer Meter from field route", "١. اختر المشترك والعداد أولاً من خط السير الميداني"),
        
        // Customer Details and Profiles
        "search_hint" to Pair("Search by name, meter code or ID...", "ابحث بالاسم، برقم العداد، أو الهوية..."),
        "history_timeline" to Pair("Consumption History Timeline", "مخطط بياني للاستهلاك التاريخي للمشترك"),
        "no_history" to Pair("No readings history on record for this customer.", "لا يوجد سجل قراءات سابق معتمد لهذا المشترك."),
        "meter_serial" to Pair("Meter Serial: ", "الرقم التسلسلي للعداد: "),
        "address" to Pair("Location Address: ", "عنوان السكن التفصيلي: "),
        "phone" to Pair("Contact Phone: ", "رقم الجوال المسجل: "),
        "install_date" to Pair("Installation Date: ", "تاريخ تركيب العداد: "),
        "status_label" to Pair("Subscription Status: ", "حالة الاشتراك الحالية: "),
        "last_registered" to Pair("Last Valid Reading: ", "آخر قراءة تم تسجيلها: "),
        "gis_label" to Pair("GIS Registered Coordinates: ", "إحداثيات نظم الجغرافيا GIS: "),
        "water_alert_status" to Pair("Water Flow Status: ", "حالة تدفق شبكة المياه: "),
        
        // Alerts Dialogs & Details
        "billing_total" to Pair("Calculate & Invoice", "احتساب كمية المياه وإصدار الفاتورة"),
        "confirm_msg" to Pair("Reading Saved Successfully!", "تم حفظ القراءة الميدانية بنجاح!"),
        "alert_title" to Pair("Suspicious Water Flow Alert", "تنبيه انسياب مياه مشبوه"),
        "leakage_warn" to Pair("Leakage Suspected: Current consumption is abnormally high compared to customer's 12-month baseline.", "إنذار تسريب محتمل: كمية المياه المستهلكة في هذه الدورة تفوق المعدّل الطبيعي للمشترك بـ 250%."),
        "alert_dismiss" to Pair("Acknowledge & Save anyway", "تجاهل التنبيه وحفظ القراءة")
    )

    fun get(key: String, lang: String): String {
        val pair = translations[key] ?: return key
        return if (lang == "ar") pair.second else pair.first
    }
}
