package com.antiagent.app.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.antiagent.app.BuildConfig

/**
 * Tiny in-app localization: English (default), Hinglish and Hindi.
 * Chosen in Settings and applied instantly, no activity restart.
 */
class Str(val lang: String) {

    private fun s(en: String, hi: String, hing: String): String = when (lang) {
        "hi" -> hi
        "hinglish" -> hing
        else -> en
    }

    // chat
    val newChat get() = s("New chat", "नई चैट", "Naya chat")
    val chats get() = s("CHATS", "चैट्स", "CHATS")
    val searchChats get() = s("Search chats", "चैट खोजें", "Chat search karo")
    val noChats get() = s("No chats yet.", "अभी कोई चैट नहीं।", "Abhi koi chat nahi.")
    val you get() = s("You", "आप", "Aap")
    val askHint get() = s("Ask anything…", "कुछ भी पूछें…", "Kuch bhi poocho…")
    val thinking get() = s("Thinking…", "सोच रहा है…", "Soch raha hoon…")
    val listening get() = s("Listening…", "सुन रहा है…", "Sun raha hoon…")
    val stop get() = s("Stop", "रोकें", "Roko")
    val send get() = s("Send", "भेजें", "Bhejo")
    val copy get() = s("Copy", "कॉपी", "Copy")
    val readAloud get() = s("Read aloud", "सुनें", "Suno")
    val regenerate get() = s("Regenerate", "फिर बनाएं", "Dobara banao")
    val retry get() = s("Retry", "फिर कोशिश करें", "Retry karo")
    val close get() = s("Close", "बंद करें", "Band karo")
    val cancel get() = s("Cancel", "रद्द करें", "Cancel")
    val delete get() = s("Delete", "हटाएं", "Delete")
    val add get() = s("Add", "जोड़ें", "Add karo")
    val clear get() = s("Clear", "साफ़ करें", "Clear")

    val emptyTagline get() = s("Your free AI agent", "आपका मुफ़्त AI एजेंट", "Aapka free AI agent")
    val formModel get() = s("Gemini + OpenRouter, free models", "Gemini + OpenRouter, मुफ़्त मॉडल", "Gemini + OpenRouter, free models")

    val sug1 get() = s("Write an Instagram reel caption", "Instagram रील के लिए कैप्शन लिखो", "Instagram reel ke liye caption likho")
    val sug2 get() = s("Make me a 30-day Python learning plan", "मेरे लिए 30 दिन का Python प्लान बनाओ", "Mere liye 30 din ka Python plan banao")
    val sug3 get() = s("5 healthy breakfast ideas", "5 हेल्दी नाश्ते के आइडिया", "5 healthy breakfast ideas")
    val sug4 get() = s("Send a photo and ask what it shows", "फोटो भेजकर पूछो कि इसमें क्या है", "Photo bhej kar poocho isme kya hai")

    // attach
    val attach get() = s("Attach", "अटैच करें", "Attach karo")
    val camera get() = s("Camera", "कैमरा", "Camera")
    val gallery get() = s("Gallery", "गैलरी", "Gallery")
    val file get() = s("File", "फ़ाइल", "File")
    val photoAttached get() = s("Photo attached", "फोटो अटैच है", "Photo attach hai")
    val fileAttached get() = s("File attached", "फ़ाइल अटैच है", "File attach hai")
    val remove get() = s("Remove", "हटाएं", "Hatao")
    val webSearch get() = s("Web", "वेब", "Web")
    val webOn get() = s("Web search is on (Gemini only)", "वेब खोज चालू (सिर्फ Gemini)", "Web search on hai (sirf Gemini)")
    val webHint get() = s("Search the web before answering — Gemini models only.", "जवाब से पहले वेब खोजें — सिर्फ Gemini मॉडल।", "Jawab se pehle web search — sirf Gemini models.")

    // models
    val chooseModel get() = s("Choose model", "मॉडल चुनें", "Model chuno")
    val modelsBuiltIn get() = s("Built-in list", "बिल्ट-इन सूची", "Built-in list")
    val modelsLive get() = s("Live from your key", "आपकी key से लाइव", "Aapki key se live")
    val refreshModels get() = s("Refresh models", "मॉडल रिफ्रेश करें", "Models refresh karo")
    val agent get() = s("Agent", "एजेंट", "Agent")
    val agentGeneral get() = s("General", "सामान्य", "General")
    val agentCoder get() = s("Coder", "कोडर", "Coder")
    val agentWriter get() = s("Writer", "लेखक", "Writer")
    val agentTeacher get() = s("Teacher", "शिक्षक", "Teacher")
    val agentTranslator get() = s("Translator", "अनुवादक", "Translator")

    fun agentName(id: String): String = when (id) {
        "coder" -> agentCoder
        "writer" -> agentWriter
        "teacher" -> agentTeacher
        "translator" -> agentTranslator
        else -> agentGeneral
    }

    // errors
    val errorTitle get() = s("Could not get a reply", "जवाब नहीं मिला", "Jawab nahi mila")
    val errorNoKeyGemini get() = s(
        "Add your free Gemini API key in Settings to start.",
        "शुरू करने के लिए सेटिंग्स में अपनी मुफ़्त Gemini key डालें।",
        "Shuru karne ke liye Settings me free Gemini key daalo."
    )
    val errorNoKeyOpenRouter get() = s(
        "Add your free OpenRouter API key in Settings, or pick a Gemini model.",
        "सेटिंग्स में मुफ़्त OpenRouter key डालें, या Gemini मॉडल चुनें।",
        "Settings me free OpenRouter key daalo, ya Gemini model chuno."
    )
    val errorEmpty get() = s(
        "The model returned an empty answer. Try again or pick another model.",
        "मॉडल ने खाली जवाब दिया। फिर कोशिश करें या दूसरा मॉडल चुनें।",
        "Model ne khali jawab diya. Dobara try karo ya doosra model chuno."
    )
    val errorFailed get() = s(
        "Check your key, internet and model, then try again.",
        "अपनी key, इंटरनेट और मॉडल जांचें, फिर कोशिश करें।",
        "Apni key, internet aur model check karo, phir try karo."
    )
    val detail get() = s("Details", "विवरण", "Details")
    val openSettings get() = s("Open Settings", "सेटिंग्स खोलें", "Settings kholo")

    // settings
    val settingsTitle get() = s("Settings", "सेटिंग्स", "Settings")
    val connections get() = s("AI connections", "AI कनेक्शन", "AI connections")
    val connectionsHint get() = s(
        "The app is free. Create free API keys below — they stay on this phone.",
        "ऐप मुफ़्त है। नीचे मुफ़्त API key बनाएं — ये इसी फोन में रहती हैं।",
        "App free hai. Neeche free API key banao — ye isi phone me rehti hain."
    )
    val geminiKey get() = s("Gemini API key", "Gemini API key", "Gemini API key")
    val openRouterKey get() = s("OpenRouter API key", "OpenRouter API key", "OpenRouter API key")
    val geminiHelp get() = s(
        "Free key: aistudio.google.com/apikey → sign in → Create API key → copy → paste here.",
        "मुफ़्त key: aistudio.google.com/apikey → साइन इन → Create API key → कॉपी → यहाँ पेस्ट करें।",
        "Free key: aistudio.google.com/apikey → sign in → Create API key → copy → yahan paste."
    )
    val openRouterHelp get() = s(
        "Free key: openrouter.ai/keys → sign in → Create key. Free models end with :free.",
        "मुफ़्त key: openrouter.ai/keys → साइन इन → Create key. मुफ़्त मॉडल :free पर खत्म होते हैं।",
        "Free key: openrouter.ai/keys → sign in → Create key. Free models :free par khatam hote hain."
    )
    val test get() = s("Test", "जांचें", "Test")
    val testing get() = s("Checking…", "जांच हो रही है…", "Check ho raha hai…")
    val connectedN get() = s("Connected", "कनेक्ट हो गया", "Connected")
    val pasteKeyFirst get() = s("Paste the key first.", "पहले key पेस्ट करें।", "Pehle key paste karo.")
    val model get() = s("Model", "मॉडल", "Model")
    val modelsLoaded get() = s("models loaded", "मॉडल लोड हुए", "models load hue")
    val language get() = s("App language", "ऐप की भाषा", "App language")
    val replyLanguage get() = s("AI reply language", "AI जवाब की भाषा", "AI jawab ki language")
    val autoLang get() = s("Auto (same as me)", "ऑटो (मेरी भाषा)", "Auto (meri bhasha)")
    val english get() = s("English", "English", "English")
    val hindi get() = s("हिन्दी", "हिन्दी", "Hindi")
    val hinglish get() = s("Hinglish", "Hinglish", "Hinglish")
    val systemPrompt get() = s("System prompt", "सिस्टम प्रॉम्प्ट", "System prompt")
    val systemPromptHint get() = s("Change how the AI behaves.", "AI का व्यवहार बदलें।", "AI ka behaviour badlo.")
    val creativity get() = s("Creativity", "रचनात्मकता", "Creativity")
    val autoSpeak get() = s("Read replies aloud", "जवाब बोलकर सुनाएं", "Jawab bol kar sunao")
    val autoSpeakHint get() = s("Speak every new reply", "हर नए जवाब को बोलें", "Har naye jawab ko bolo")
    val themeLabel get() = s("Theme", "थीम", "Theme")
    val resetPrompt get() = s("Reset to default prompt", "डिफ़ॉल्ट प्रॉम्प्ट पर लौटें", "Default prompt par wapas")
    val clearChats get() = s("Delete all chats", "सभी चैट हटाएं", "Sab chats delete karo")
    val clearChatsConfirm get() = s("Delete all chats? This cannot be undone.", "सभी चैट हटाएं? इसे वापस नहीं लाया जा सकता।", "Sab chats delete karo? Wapas nahi aayengi.")
    val diagnostics get() = s("Diagnostics", "डायग्नोस्टिक्स", "Diagnostics")
    val diagnosticsHint get() = s("Last request info (keys are never shown).", "पिछले अनुरोध की जानकारी (key कभी नहीं दिखती)।", "Last request ki info (key kabhi nahi dikhti).")
    val noDiagnostics get() = s("No requests yet.", "अभी कोई अनुरोध नहीं।", "Abhi koi request nahi.")
    val about get() = s(
        "Anti Agent v${BuildConfig.VERSION_NAME} — free, no subscription. Keys stay on this phone.",
        "Anti Agent v${BuildConfig.VERSION_NAME} — मुफ़्त, कोई सब्सक्रिप्शन नहीं। Keys इसी फोन में रहती हैं।",
        "Anti Agent v${BuildConfig.VERSION_NAME} — free, koi subscription nahi. Keys isi phone me rehti hain."
    )

    // notes
    val notesTitle get() = s("Notes & Tasks", "नोट्स और काम", "Notes & Tasks")
    val notesPlaceholder get() = s("Write a new note or task…", "नया नोट या काम लिखें…", "Naya note ya task likho…")
    val notesEmpty get() = s(
        "No notes yet. Write your tasks and ideas here — saved on this phone.",
        "अभी कोई नोट नहीं। अपने काम और आइडिया यहाँ लिखें — इसी फोन में सेव होंगे।",
        "Abhi koi note nahi. Apne tasks aur ideas yahan likho — isi phone me save honge."
    )
    val deleteNote get() = s("Delete note", "नोट हटाएं", "Note delete karo")

    // misc
    val micPermission get() = s(
        "Microphone permission is needed for voice input.",
        "बोलकर लिखने के लिए माइक्रोफ़ोन की अनुमति चाहिए।",
        "Bol kar likhne ke liye mic permission chahiye."
    )
    val voiceUnavailable get() = s(
        "Voice recognition is not available on this phone.",
        "इस फोन पर वॉइस रिकग्निशन उपलब्ध नहीं है।",
        "Is phone par voice recognition available nahi hai."
    )
    val voiceError get() = s("Voice input failed. Try again.", "वॉइस इनपुट नहीं हुआ। फिर कोशिश करें।", "Voice input fail hua. Dobara try karo.")
    val photoError get() = s("Could not read this photo.", "यह फोटो पढ़ी नहीं जा सकी।", "Ye photo padhi nahi ja saki.")
    val fileError get() = s("Could not read this file.", "यह फ़ाइल पढ़ी नहीं जा सकी।", "Ye file padhi nahi ja saki.")
    val fileTooBig get() = s("File is too large (max 200 KB).", "फ़ाइल बहुत बड़ी है (अधिकतम 200 KB)।", "File bahut badi hai (max 200 KB).")
    val deleteChat get() = s("Delete chat", "चैट हटाएं", "Chat delete karo")
    val deleteChatConfirm get() = s("Delete this chat? This cannot be undone.", "यह चैट हटाएं? इसे वापस नहीं लाया जा सकता।", "Ye chat delete karo? Wapas nahi aayegi.")
    val webNeedsGemini get() = s("Web search works with Gemini models.", "वेब खोज Gemini मॉडल के साथ काम करती है।", "Web search Gemini models ke saath kaam karti hai.")

    // ---- v1.1 additions ----
    val show get() = s("Show", "दिखाएं", "Dikhao")
    val hide get() = s("Hide", "छिपाएं", "Chhupao")
    val clearKey get() = s("Clear key", "Key हटाएं", "Key hatao")
    val noResults get() = s("No chats found.", "कोई चैट नहीं मिली।", "Koi chat nahi mili.")
    val testOk get() = s("Works — the model replied.", "काम कर रहा है — मॉडल ने जवाब दिया।", "Kaam kar raha hai — model ne jawab diya.")
    val testFailed get() = s("This key did not work:", "यह key काम नहीं कर रही:", "Ye key kaam nahi kar rahi:")
    val testHint get() = s(
        "Test sends one small free request to check the key.",
        "Test एक छोटा मुफ़्त अनुरोध भेजकर key जांचता है।",
        "Test ek chhota free request bhej kar key check karta hai."
    )
    val keyLooksOpenRouter get() = s(
        "This looks like an OpenRouter key (sk-or-…). Paste it in the OpenRouter field.",
        "यह OpenRouter key (sk-or-…) लगती है। इसे OpenRouter वाले खाने में डालें।",
        "Ye OpenRouter key (sk-or-…) lagti hai. Isse OpenRouter field me daalo."
    )
    val keyLooksGemini get() = s(
        "This looks like a Gemini key (AIza…). Paste it in the Gemini field.",
        "यह Gemini key (AIza…) लगती है। इसे Gemini वाले खाने में डालें।",
        "Ye Gemini key (AIza…) lagti hai. Isse Gemini field me daalo."
    )
    val languageHint get() = s(
        "Language of menus, buttons and settings.",
        "मेन्यू, बटन और सेटिंग्स की भाषा।",
        "Menu, buttons aur settings ki bhasha."
    )
    val replyLanguageHint get() = s(
        "Language the AI answers in.",
        "AI किस भाषा में जवाब देगा।",
        "AI kis bhasha me jawab dega."
    )
    val addFreeKey get() = s("Add a free API key", "मुफ़्त API key जोड़ें", "Free API key daalo")
    val dataSection get() = s("Data", "डेटा", "Data")
    val aboutSection get() = s("About", "ऐप के बारे में", "About")
    val savedOnPhone get() = s(
        "Chats, notes and keys are stored only on this phone.",
        "चैट, नोट्स और keys सिर्फ इसी फोन में सेव होती हैं।",
        "Chats, notes aur keys sirf isi phone me save hoti hain."
    )
    val agentsLabel get() = s("Agent", "एजेंट", "Agent")
    val agentsHint get() = s(
        "Choose how the AI should behave.",
        "AI का रवैया चुनें।",
        "AI ka behaviour chuno."
    )
    val messagesLabel get() = s("messages", "संदेश", "messages")
    val welcomeBack get() = s("Welcome back", "वापसी पर स्वागत है", "Wapas aagaye")
}

val LocalStr = staticCompositionLocalOf { Str("en") }
