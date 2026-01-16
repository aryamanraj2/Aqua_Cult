//
//  LocalizationManager.swift
//  aqua
//
//  Localization Manager for Multilingual Support
//

import Foundation
import SwiftUI
import Combine

class LocalizationManager: ObservableObject {
    @Published var currentLanguage: String {
        didSet {
            UserDefaults.standard.set(currentLanguage, forKey: "selectedLanguage")
        }
    }

    static let shared = LocalizationManager()

    private init() {
        self.currentLanguage = UserDefaults.standard.string(forKey: "selectedLanguage") ?? "English"
    }

    func setLanguage(_ language: String) {
        currentLanguage = language
    }

    func localizedString(for key: String) -> String {
        switch currentLanguage {
        case "हिन्दी":
            return hindiTranslations[key] ?? englishTranslations[key] ?? key
        case "বাংলা":
            return bengaliTranslations[key] ?? englishTranslations[key] ?? key
        default:
            return englishTranslations[key] ?? key
        }
    }

    // English translations (default)
    private let englishTranslations: [String: String] = [
        // Navigation
        "skip": "Skip",
        "continue": "Continue",
        "get_started": "Get Started",
        "done": "Done",
        "cancel": "Cancel",
        "save": "Save",
        "edit": "Edit",
        "back": "Back",

        // Language Selection
        "choose_language": "Choose Your Language",
        "select_preferred_language": "Select your preferred language",

        // Onboarding Titles
        "welcome_title": "Welcome to Aqua Sense",
        "monitoring_title": "Monitor Your Fishes",
        "disease_title": "Disease Detection",
        "marketplace_title": "All-in-One Marketplace",
        "voice_support_title": "Voice-First Support",

        // Onboarding Subtitles
        "welcome_subtitle": "Your smart companion for modern aquaculture management",
        "monitoring_subtitle": "Real-time tracking of water quality, temperature, and fish health in all your tanks",
        "disease_subtitle": "AI-powered detection helps identify fish diseases early and suggests treatments",
        "marketplace_subtitle": "Access equipment, medicines, feed, and supplies - everything you need in one place",
        "voice_support_subtitle": "Hands-free operation designed for cultivators - speak to control and get instant insights",

        // Tab Labels
        "tab_dashboard": "Dashboard",
        "tab_disease": "Disease",
        "tab_market": "Market",
        "tab_profile": "Profile",

        // Dashboard
        "active_tanks": "Active Tanks",
        "total_volume": "Total Volume",
        "avg_temp": "Avg Temp",
        "my_tanks": "My Tanks",
        "monitor_aquaculture": "Monitor your aquaculture systems",
        "tank_conditions": "Tank Conditions",
        "current_stage": "Current Stage",
        "environmental_conditions": "Environmental Conditions",
        "humidity": "Humidity",
        "rain": "Rain",

        // Water Quality Metrics
        "ph_level": "pH Level",
        "water_temp": "Water Temp",
        "dissolved_o2": "Dissolved O₂",
        "ammonia": "Ammonia",
        "salinity": "Salinity",
        "turbidity": "Turbidity",
        "optimal": "Optimal",
        "on_track": "On Track",
        "normal": "Normal",
        "consider": "Consider",
        "caution": "Caution",

        // Profile
        "profile": "Profile",
        "my_aquaculture": "My Aquaculture",
        "years_experience": "Years Experience",
        "settings_info": "Settings & Info",
        "app_settings": "App Settings",
        "preferences_config": "Preferences & configuration",
        "help_support": "Help & Support",
        "faqs_contact": "FAQs and contact support",
        "about_aqua": "About Aqua",
        "sign_out": "Sign Out",
        "log_out_account": "Log out of your account",
        "pincode": "Pincode",
        "edit_profile": "Edit Profile",
        "change_photo": "Change Photo",
        "full_name": "Full Name",
        "enter_name": "Enter your name",
        "mobile_number": "Mobile Number",
        "enter_mobile": "Enter your mobile number",
        "address": "Address",
        "enter_address": "Enter your address",
        "enter_pincode": "Enter pincode",
        "settings": "Settings",
        "settings_coming_soon": "Settings functionality coming soon...",

        // Marketplace
        "marketplace": "Marketplace",
        "quality_supplies": "Quality supplies for your aquaculture",
        "search_products": "Search products...",
        "all": "All",
        "items": "items",
        "out_of_stock": "Out of Stock",

        // Cart
        "shopping_cart": "Shopping Cart",
        "cart_empty": "Your cart is empty",
        "add_products_start": "Add some products to get started",
        "continue_shopping": "Continue Shopping",
        "subtotal": "Subtotal",
        "gst": "GST (18%)",
        "shipping": "Shipping",
        "total": "Total",
        "proceed_checkout": "Proceed to Checkout",
        "add_more_free_shipping": "Add more for free shipping",
        "in_cart": "in cart",
        "add_to_cart": "Add to cart",

        // Disease Detection
        "disease_detection": "Disease Detection",
        "point_camera": "Point camera at any fish.",
        "ai_analyze": "AI will analyze it instantly.",
        "analyzing_fish": "Analyzing fish...",
        "analysis_complete": "Analysis Complete",
        "confidence": "Confidence",
        "about": "About",
        "recommendations": "Recommendations",
        "recommended_products": "Recommended Products",
        "analysis_failed": "Analysis Failed",
        "try_again": "Try Again",
        "disease_analysis": "Disease Analysis",

        // Tutorial
        "voice_add_tank": "Voice & Add Tank",
        "voice_add_tank_desc": "Use the microphone for voice commands or tap the plus to add a new tank.",
        "env_conditions_desc": "Monitor real-time weather and environmental data affecting your tanks.",
        "tank_health_metrics": "Tank Health Metrics",
        "tank_health_desc": "Track water quality parameters like pH, oxygen, and salinity at a glance."
    ]

    // Hindi translations
    private let hindiTranslations: [String: String] = [
        // Navigation
        "skip": "छोड़ें",
        "continue": "जारी रखें",
        "get_started": "शुरू करें",
        "done": "पूर्ण",
        "cancel": "रद्द करें",
        "save": "सहेजें",
        "edit": "संपादित करें",
        "back": "वापस",

        // Language Selection
        "choose_language": "अपनी भाषा चुनें",
        "select_preferred_language": "अपनी पसंदीदा भाषा चुनें",

        // Onboarding Titles
        "welcome_title": "एक्वा सेंस में आपका स्वागत है",
        "monitoring_title": "अपनी मछलियों की निगरानी करें",
        "disease_title": "रोग का पता लगाना",
        "marketplace_title": "सब-कुछ-एक-जगह बाज़ार",
        "voice_support_title": "आवाज़-प्राथमिक सहायता",

        // Onboarding Subtitles
        "welcome_subtitle": "आधुनिक मछली पालन प्रबंधन के लिए आपका स्मार्ट साथी",
        "monitoring_subtitle": "आपके सभी टैंकों में पानी की गुणवत्ता, तापमान और मछली के स्वास्थ्य की वास्तविक समय की निगरानी",
        "disease_subtitle": "AI-संचालित पहचान मछली की बीमारियों को जल्दी पहचानने और उपचार सुझाने में मदद करती है",
        "marketplace_subtitle": "उपकरण, दवाइयां, आहार और आपूर्ति तक पहुंच - एक ही जगह पर आपको जो कुछ भी चाहिए",
        "voice_support_subtitle": "खेतिहरों के लिए बनाया गया हैंड्स-फ्री ऑपरेशन - नियंत्रण के लिए बोलें और तत्काल जानकारी पाएं",

        // Tab Labels
        "tab_dashboard": "डैशबोर्ड",
        "tab_disease": "रोग",
        "tab_market": "बाज़ार",
        "tab_profile": "प्रोफ़ाइल",

        // Dashboard
        "active_tanks": "सक्रिय टैंक",
        "total_volume": "कुल मात्रा",
        "avg_temp": "औसत तापमान",
        "my_tanks": "मेरे टैंक",
        "monitor_aquaculture": "अपने जलीय कृषि प्रणालियों की निगरानी करें",
        "tank_conditions": "टैंक की स्थिति",
        "current_stage": "वर्तमान चरण",
        "environmental_conditions": "पर्यावरणीय परिस्थितियां",
        "humidity": "नमी",
        "rain": "बारिश",

        // Water Quality Metrics
        "ph_level": "पीएच स्तर",
        "water_temp": "पानी का तापमान",
        "dissolved_o2": "घुलित O₂",
        "ammonia": "अमोनिया",
        "salinity": "लवणता",
        "turbidity": "मैलापन",
        "optimal": "उत्तम",
        "on_track": "सही दिशा में",
        "normal": "सामान्य",
        "consider": "विचार करें",
        "caution": "सावधानी",

        // Profile
        "profile": "प्रोफ़ाइल",
        "my_aquaculture": "मेरी जलीय कृषि",
        "years_experience": "वर्षों का अनुभव",
        "settings_info": "सेटिंग्स और जानकारी",
        "app_settings": "ऐप सेटिंग्स",
        "preferences_config": "प्राथमिकताएं और कॉन्फ़िगरेशन",
        "help_support": "सहायता और समर्थन",
        "faqs_contact": "अक्सर पूछे जाने वाले प्रश्न और संपर्क सहायता",
        "about_aqua": "एक्वा के बारे में",
        "sign_out": "साइन आउट",
        "log_out_account": "अपने खाते से लॉग आउट करें",
        "pincode": "पिन कोड",
        "edit_profile": "प्रोफ़ाइल संपादित करें",
        "change_photo": "फ़ोटो बदलें",
        "full_name": "पूरा नाम",
        "enter_name": "अपना नाम दर्ज करें",
        "mobile_number": "मोबाइल नंबर",
        "enter_mobile": "अपना मोबाइल नंबर दर्ज करें",
        "address": "पता",
        "enter_address": "अपना पता दर्ज करें",
        "enter_pincode": "पिन कोड दर्ज करें",
        "settings": "सेटिंग्स",
        "settings_coming_soon": "सेटिंग्स जल्द आ रही हैं...",

        // Marketplace
        "marketplace": "बाज़ार",
        "quality_supplies": "आपकी जलीय कृषि के लिए गुणवत्तापूर्ण सामग्री",
        "search_products": "उत्पाद खोजें...",
        "all": "सभी",
        "items": "आइटम",
        "out_of_stock": "स्टॉक में नहीं",

        // Cart
        "shopping_cart": "शॉपिंग कार्ट",
        "cart_empty": "आपका कार्ट खाली है",
        "add_products_start": "शुरू करने के लिए कुछ उत्पाद जोड़ें",
        "continue_shopping": "खरीदारी जारी रखें",
        "subtotal": "उप-योग",
        "gst": "जीएसटी (18%)",
        "shipping": "शिपिंग",
        "total": "कुल",
        "proceed_checkout": "चेकआउट पर जाएं",
        "add_more_free_shipping": "मुफ्त शिपिंग के लिए और जोड़ें",
        "in_cart": "कार्ट में",
        "add_to_cart": "कार्ट में जोड़ें",

        // Disease Detection
        "disease_detection": "रोग पहचान",
        "point_camera": "किसी भी मछली पर कैमरा लगाएं।",
        "ai_analyze": "AI तुरंत इसका विश्लेषण करेगा।",
        "analyzing_fish": "मछली का विश्लेषण हो रहा है...",
        "analysis_complete": "विश्लेषण पूर्ण",
        "confidence": "विश्वास",
        "about": "के बारे में",
        "recommendations": "सिफारिशें",
        "recommended_products": "अनुशंसित उत्पाद",
        "analysis_failed": "विश्लेषण विफल",
        "try_again": "पुनः प्रयास करें",
        "disease_analysis": "रोग विश्लेषण",

        // Tutorial
        "voice_add_tank": "आवाज़ और टैंक जोड़ें",
        "voice_add_tank_desc": "वॉयस कमांड के लिए माइक्रोफोन का उपयोग करें या नया टैंक जोड़ने के लिए प्लस पर टैप करें।",
        "env_conditions_desc": "अपने टैंकों को प्रभावित करने वाले रीयल-टाइम मौसम और पर्यावरणीय डेटा की निगरानी करें।",
        "tank_health_metrics": "टैंक स्वास्थ्य मेट्रिक्स",
        "tank_health_desc": "पीएच, ऑक्सीजन और लवणता जैसे पानी की गुणवत्ता मापदंडों को एक नज़र में ट्रैक करें।"
    ]

    // Bengali translations
    private let bengaliTranslations: [String: String] = [
        // Navigation
        "skip": "এড়িয়ে যান",
        "continue": "চালিয়ে যান",
        "get_started": "শুরু করুন",
        "done": "সম্পন্ন",
        "cancel": "বাতিল",
        "save": "সংরক্ষণ",
        "edit": "সম্পাদনা",
        "back": "পিছনে",

        // Language Selection
        "choose_language": "আপনার ভাষা বেছে নিন",
        "select_preferred_language": "আপনার পছন্দের ভাষা নির্বাচন করুন",

        // Onboarding Titles
        "welcome_title": "অ্যাকোয়া সেন্সে স্বাগতম",
        "monitoring_title": "আপনার মাছের নিরীক্ষণ করুন",
        "disease_title": "রোগ সনাক্তকরণ",
        "marketplace_title": "সব-এক-সাথে বাজার",
        "voice_support_title": "ভয়েস-প্রধান সহায়তা",

        // Onboarding Subtitles
        "welcome_subtitle": "আধুনিক মৎস্যচাষ ব্যবস্থাপনার জন্য আপনার স্মার্ট সঙ্গী",
        "monitoring_subtitle": "আপনার সমস্ত ট্যাঙ্কে পানির গুণমান, তাপমাত্রা এবং মাছের স্বাস্থ্যের রিয়েল-টাইম ট্র্যাকিং",
        "disease_subtitle": "AI-চালিত সনাক্তকরণ মাছের রোগ তাড়াতাড়ি চিহ্নিত করতে এবং চিকিৎসার পরামর্শ দিতে সাহায্য করে",
        "marketplace_subtitle": "যন্ত্রপাতি, ওষুধ, খাদ্য এবং সরবরাহের অ্যাক্সেস - এক জায়গায় আপনার প্রয়োজনীয় সবকিছু",
        "voice_support_subtitle": "চাষীদের জন্য ডিজাইন করা হ্যান্ডস-ফ্রি অপারেশন - নিয়ন্ত্রণ করতে কথা বলুন এবং তাৎক্ষণিক অন্তর্দৃষ্টি পান",

        // Tab Labels
        "tab_dashboard": "ড্যাশবোর্ড",
        "tab_disease": "রোগ",
        "tab_market": "বাজার",
        "tab_profile": "প্রোফাইল",

        // Dashboard
        "active_tanks": "সক্রিয় ট্যাঙ্ক",
        "total_volume": "মোট আয়তন",
        "avg_temp": "গড় তাপমাত্রা",
        "my_tanks": "আমার ট্যাঙ্ক",
        "monitor_aquaculture": "আপনার মৎস্যচাষ সিস্টেম পর্যবেক্ষণ করুন",
        "tank_conditions": "ট্যাঙ্কের অবস্থা",
        "current_stage": "বর্তমান পর্যায়",
        "environmental_conditions": "পরিবেশগত অবস্থা",
        "humidity": "আর্দ্রতা",
        "rain": "বৃষ্টি",

        // Water Quality Metrics
        "ph_level": "পিএইচ মাত্রা",
        "water_temp": "পানির তাপমাত্রা",
        "dissolved_o2": "দ্রবীভূত O₂",
        "ammonia": "অ্যামোনিয়া",
        "salinity": "লবণাক্ততা",
        "turbidity": "ঘোলাটে",
        "optimal": "সর্বোত্তম",
        "on_track": "সঠিক পথে",
        "normal": "স্বাভাবিক",
        "consider": "বিবেচনা করুন",
        "caution": "সতর্কতা",

        // Profile
        "profile": "প্রোফাইল",
        "my_aquaculture": "আমার মৎস্যচাষ",
        "years_experience": "বছরের অভিজ্ঞতা",
        "settings_info": "সেটিংস ও তথ্য",
        "app_settings": "অ্যাপ সেটিংস",
        "preferences_config": "পছন্দ ও কনফিগারেশন",
        "help_support": "সাহায্য ও সমর্থন",
        "faqs_contact": "প্রায়শই জিজ্ঞাসিত প্রশ্ন এবং যোগাযোগ সমর্থন",
        "about_aqua": "অ্যাকোয়া সম্পর্কে",
        "sign_out": "সাইন আউট",
        "log_out_account": "আপনার অ্যাকাউন্ট থেকে লগ আউট করুন",
        "pincode": "পিন কোড",
        "edit_profile": "প্রোফাইল সম্পাদনা",
        "change_photo": "ছবি পরিবর্তন",
        "full_name": "পুরো নাম",
        "enter_name": "আপনার নাম লিখুন",
        "mobile_number": "মোবাইল নম্বর",
        "enter_mobile": "আপনার মোবাইল নম্বর লিখুন",
        "address": "ঠিকানা",
        "enter_address": "আপনার ঠিকানা লিখুন",
        "enter_pincode": "পিন কোড লিখুন",
        "settings": "সেটিংস",
        "settings_coming_soon": "সেটিংস শীঘ্রই আসছে...",

        // Marketplace
        "marketplace": "বাজার",
        "quality_supplies": "আপনার মৎস্যচাষের জন্য মানসম্পন্ন সরবরাহ",
        "search_products": "পণ্য খুঁজুন...",
        "all": "সব",
        "items": "আইটেম",
        "out_of_stock": "স্টক নেই",

        // Cart
        "shopping_cart": "শপিং কার্ট",
        "cart_empty": "আপনার কার্ট খালি",
        "add_products_start": "শুরু করতে কিছু পণ্য যোগ করুন",
        "continue_shopping": "কেনাকাটা চালিয়ে যান",
        "subtotal": "উপমোট",
        "gst": "জিএসটি (১৮%)",
        "shipping": "শিপিং",
        "total": "মোট",
        "proceed_checkout": "চেকআউটে যান",
        "add_more_free_shipping": "বিনামূল্যে শিপিংয়ের জন্য আরও যোগ করুন",
        "in_cart": "কার্টে",
        "add_to_cart": "কার্টে যোগ করুন",

        // Disease Detection
        "disease_detection": "রোগ সনাক্তকরণ",
        "point_camera": "যেকোনো মাছের দিকে ক্যামেরা পয়েন্ট করুন।",
        "ai_analyze": "AI তাৎক্ষণিকভাবে এটি বিশ্লেষণ করবে।",
        "analyzing_fish": "মাছ বিশ্লেষণ করা হচ্ছে...",
        "analysis_complete": "বিশ্লেষণ সম্পূর্ণ",
        "confidence": "আত্মবিশ্বাস",
        "about": "সম্পর্কে",
        "recommendations": "সুপারিশ",
        "recommended_products": "প্রস্তাবিত পণ্য",
        "analysis_failed": "বিশ্লেষণ ব্যর্থ",
        "try_again": "আবার চেষ্টা করুন",
        "disease_analysis": "রোগ বিশ্লেষণ",

        // Tutorial
        "voice_add_tank": "ভয়েস ও ট্যাঙ্ক যোগ",
        "voice_add_tank_desc": "ভয়েস কমান্ডের জন্য মাইক্রোফোন ব্যবহার করুন বা নতুন ট্যাঙ্ক যোগ করতে প্লাসে ট্যাপ করুন।",
        "env_conditions_desc": "আপনার ট্যাঙ্কগুলিকে প্রভাবিত করে এমন রিয়েল-টাইম আবহাওয়া এবং পরিবেশগত ডেটা পর্যবেক্ষণ করুন।",
        "tank_health_metrics": "ট্যাঙ্ক স্বাস্থ্য মেট্রিক্স",
        "tank_health_desc": "পিএইচ, অক্সিজেন এবং লবণাক্ততার মতো পানির গুণমান প্যারামিটার এক নজরে ট্র্যাক করুন।"
    ]
}
