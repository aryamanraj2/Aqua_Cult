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
    @Published var currentLanguage: String = "English"
    
    static let shared = LocalizationManager()
    
    private init() {}
    
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
        "voice_support_subtitle": "Hands-free operation designed for cultivators - speak to control and get instant insights"
    ]
    
    // Hindi translations
    private let hindiTranslations: [String: String] = [
        // Navigation
        "skip": "छोड़ें",
        "continue": "जारी रखें",
        "get_started": "शुरू करें",
        
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
        "voice_support_subtitle": "खेतिहरों के लिए बनाया गया हैंड्स-फ्री ऑपरेशन - नियंत्रण के लिए बोलें और तत्काल जानकारी पाएं"
    ]
    
    // Bengali translations
    private let bengaliTranslations: [String: String] = [
        // Navigation
        "skip": "এড়িয়ে যান",
        "continue": "চালিয়ে যান",
        "get_started": "শুরু করুন",
        
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
        "voice_support_subtitle": "চাষীদের জন্য ডিজাইন করা হ্যান্ডস-ফ্রি অপারেশন - নিয়ন্ত্রণ করতে কথা বলুন এবং তাৎক্ষণিক অন্তর্দৃষ্টি পান"
    ]
}
