// ============================================================================
// ANTI AGENT (ANTIGEM UI CLONE) - ALL IN ONE COMPLETE UI SOURCE CODE
// Package: com.antiagent.app.ui / com.antiagent.app
// ============================================================================


// ==================== [ FILE: MainActivity.kt ] ====================

package com.antiagent.app

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.antiagent.app.ui.AntiAgentTheme
import com.antiagent.app.ui.BrowserScreen
import com.antiagent.app.ui.ChatScreen
import com.antiagent.app.ui.DrawerContent
import com.antiagent.app.ui.LocalStr
import com.antiagent.app.ui.NotesScreen
import com.antiagent.app.ui.SettingsScreen
import com.antiagent.app.ui.Str
import com.antiagent.app.ui.TerminalScreen
import com.antiagent.app.ui.ToolsScreen
import com.antiagent.app.ui.UsageScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Optional one-time key import: the assistant can pass keys with
        // `am start -n com.antiagent.app/.MainActivity --es aa_gemini ...`.
        // Keys are stored only in this app's private SharedPreferences and are
        // never written into the APK, the repo or any log.
        val g = intent.getStringExtra("aa_gemini")
        val o = intent.getStringExtra("aa_openrouter")
        if (!g.isNullOrBlank() || !o.isNullOrBlank()) {
            val p = com.antiagent.app.data.Prefs(this)
            if (!g.isNullOrBlank()) p.geminiKey = g.trim()
            if (!o.isNullOrBlank()) p.openRouterKey = o.trim()
            intent.removeExtra("aa_gemini")
            intent.removeExtra("aa_openrouter")
        }

        setContent {
            val vm: AppViewModel = viewModel()
            AntiAgentTheme(vm.theme) {
                CompositionLocalProvider(
                    LocalStr provides remember(vm.language) { Str(vm.language) }
                ) {
                    StatusBars()
                    AppRoot(vm)
                }
            }
        }
    }
}

private fun Context.findActivity(): Activity? {
    var c: Context = this
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}

@Composable
private fun StatusBars() {
    val colors = MaterialTheme.colorScheme
    val view = LocalView.current
    val light = colors.background.luminance() > 0.5f
    SideEffect {
        val window = view.context.findActivity()?.window ?: return@SideEffect
        window.statusBarColor = colors.background.toArgb()
        window.navigationBarColor = colors.background.toArgb()
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = light
        controller.isAppearanceLightNavigationBars = light
    }
}

@Composable
fun AppRoot(vm: AppViewModel) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }
    BackHandler(enabled = vm.screen != "chat" && !drawerState.isOpen) {
        vm.screen = "chat"
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(vm) {
                scope.launch { drawerState.close() }
            }
        }
    ) {
        when (vm.screen) {
            "settings" -> SettingsScreen(vm) { vm.screen = "chat" }
            "notes" -> NotesScreen(vm) { vm.screen = "chat" }
            "usage" -> UsageScreen(vm) { vm.screen = "chat" }
            "browser" -> BrowserScreen(vm) { vm.screen = "chat" }
            "terminal" -> TerminalScreen(vm) { vm.screen = "chat" }
            "tools" -> ToolsScreen(vm) { vm.screen = "chat" }
            else -> ChatScreen(vm) { scope.launch { drawerState.open() } }
        }
    }
}



// ==================== [ FILE: Theme.kt ] ====================

package com.antiagent.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object Themes {
    const val MIDNIGHT = "midnight"
    const val CARBON = "carbon"
    const val OCEAN = "ocean"
    const val DAYLIGHT = "daylight"
    const val SAND = "sand"

    val choices = listOf(
        MIDNIGHT to "Midnight",
        CARBON to "Carbon",
        OCEAN to "Ocean",
        DAYLIGHT to "Daylight",
        SAND to "Sand"
    )
}

private val Midnight = darkColorScheme(
    primary = Color(0xFF8B5CF6),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2E2350),
    onPrimaryContainer = Color(0xFFEDE7FF),
    secondary = Color(0xFF22D3EE),
    onSecondary = Color(0xFF04262B),
    secondaryContainer = Color(0xFF123B42),
    onSecondaryContainer = Color(0xFFC9F6FC),
    background = Color(0xFF0B0F14),
    onBackground = Color(0xFFE7EBF3),
    surface = Color(0xFF11161D),
    onSurface = Color(0xFFE7EBF3),
    surfaceVariant = Color(0xFF1B222C),
    onSurfaceVariant = Color(0xFFA9B4C4),
    outline = Color(0xFF3A4453),
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF2A0A0A)
)

private val Carbon = darkColorScheme(
    primary = Color(0xFF34D399),
    onPrimary = Color(0xFF05281B),
    primaryContainer = Color(0xFF0E3B2B),
    onPrimaryContainer = Color(0xFFD3FBE9),
    secondary = Color(0xFFFBBF24),
    onSecondary = Color(0xFF2B1D00),
    secondaryContainer = Color(0xFF3A2C08),
    onSecondaryContainer = Color(0xFFFDEDC2),
    background = Color(0xFF0E0E10),
    onBackground = Color(0xFFE9E9EC),
    surface = Color(0xFF161619),
    onSurface = Color(0xFFE9E9EC),
    surfaceVariant = Color(0xFF222226),
    onSurfaceVariant = Color(0xFFB4B4BC),
    outline = Color(0xFF43434B),
    error = Color(0xFFFF7B72),
    onError = Color(0xFF2A0A0A)
)

private val Ocean = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = Color(0xFF04283A),
    primaryContainer = Color(0xFF0B3B52),
    onPrimaryContainer = Color(0xFFD6F2FF),
    secondary = Color(0xFF818CF8),
    onSecondary = Color(0xFF0B1240),
    secondaryContainer = Color(0xFF1B2354),
    onSecondaryContainer = Color(0xFFE0E3FF),
    background = Color(0xFF071019),
    onBackground = Color(0xFFE2EDF5),
    surface = Color(0xFF0D1822),
    onSurface = Color(0xFFE2EDF5),
    surfaceVariant = Color(0xFF16242F),
    onSurfaceVariant = Color(0xFFA5B8C6),
    outline = Color(0xFF354A58),
    error = Color(0xFFFF7B72),
    onError = Color(0xFF2A0A0A)
)

private val Daylight = lightColorScheme(
    primary = Color(0xFF6D28D9),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE7FF),
    onPrimaryContainer = Color(0xFF22005D),
    secondary = Color(0xFF0E7490),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFF3FA),
    onSecondaryContainer = Color(0xFF00363D),
    background = Color(0xFFF7F7FB),
    onBackground = Color(0xFF17181C),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF17181C),
    surfaceVariant = Color(0xFFECEAF2),
    onSurfaceVariant = Color(0xFF55525F),
    outline = Color(0xFFB9B5C4),
    error = Color(0xFFC0281F),
    onError = Color.White
)

private val Sand = lightColorScheme(
    primary = Color(0xFFB45309),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEDD5),
    onPrimaryContainer = Color(0xFF3A1A00),
    secondary = Color(0xFF0F766E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF00352F),
    background = Color(0xFFFAF6EF),
    onBackground = Color(0xFF201A12),
    surface = Color(0xFFFFFDF8),
    onSurface = Color(0xFF201A12),
    surfaceVariant = Color(0xFFF0E7D8),
    onSurfaceVariant = Color(0xFF5E5344),
    outline = Color(0xFFC7B99F),
    error = Color(0xFFB91C1C),
    onError = Color.White
)

@Composable
fun AntiAgentTheme(choice: String, content: @Composable () -> Unit) {
    val scheme = when (choice) {
        Themes.CARBON -> Carbon
        Themes.OCEAN -> Ocean
        Themes.DAYLIGHT -> Daylight
        Themes.SAND -> Sand
        else -> Midnight
    }
    MaterialTheme(colorScheme = scheme, typography = Typography(), content = content)
}



// ==================== [ FILE: Str.kt ] ====================

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

    // ---- v2 projects / tools / usage ----
    val workspace get() = s("Workspace", "वर्कस्पेस", "Workspace")
    val workspaceHint get() = s(
        "Browser, console, tools and usage — all free, on this phone.",
        "ब्राउज़र, कंसोल, टूल्स और इस्तेमाल — सब मुफ़्त, इसी फ़ोन में।",
        "Browser, console, tools aur usage — sab free, isi phone me."
    )
    val projects get() = s("Projects", "प्रोजेक्ट्स", "Projects")
    val allProjects get() = s("All", "सभी", "All")
    val newProject get() = s("New project", "नया प्रोजेक्ट", "Naya project")
    val projectName get() = s("Project name", "प्रोजेक्ट का नाम", "Project ka naam")
    val projectHint get() = s(
        "Each project has its own chats, model and behaviour.",
        "हर प्रोजेक्ट की अपनी चैट, मॉडल और रवैया होता है।",
        "Har project ki apni chat, model aur behaviour hota hai."
    )
    val deleteProject get() = s("Delete project", "प्रोजेक्ट हटाएं", "Project delete karo")
    val deleteProjectConfirm get() = s(
        "Delete this project? Its chats move to Chat.",
        "यह प्रोजेक्ट हटाएं? इसकी चैट Chat में चली जाएँगी।",
        "Ye project delete karo? Iski chats Chat me chali jayengi."
    )
    val pickModel get() = s("Model for this project", "इस प्रोजेक्ट का मॉडल", "Is project ka model")
    val pickAgent get() = s("Agent", "एजेंट", "Agent")
    val create get() = s("Create", "बनाएं", "Banao")

    val usage get() = s("Usage", "इस्तेमाल", "Usage")
    val usageHint get() = s(
        "Requests and tokens used on this phone. Nothing is sent anywhere.",
        "इस फोन par हुए अनुरोध और टोकन। कहीं नहीं भेजे जाते।",
        "Is phone par hue requests aur tokens. Kahin nahi bheje jate."
    )
    val requests get() = s("Requests", "अनुरोध", "Requests")
    val tokensIn get() = s("Input tokens", "इनपुट टोकन", "Input tokens")
    val tokensOut get() = s("Output tokens", "आउटपुट टोकन", "Output tokens")
    val today get() = s("Today", "आज", "Aaj")
    val allTime get() = s("All time", "कुल", "Total")
    val balance get() = s("OpenRouter balance", "OpenRouter बैलेंस", "OpenRouter balance")
    val balanceUsed get() = s("Used", "इस्तेमाल", "Used")
    val balanceLimit get() = s("Limit", "सीमा", "Limit")
    val balanceLeft get() = s("Remaining", "बचा हुआ", "Bacha hua")
    val refreshBalance get() = s("Refresh balance", "बैलेंस रिफ्रेश", "Balance refresh")
    val noBalance get() = s("Add an OpenRouter key to see balance.", "बैलेंस के लिए OpenRouter key डालें।", "Balance ke liye OpenRouter key daalo.")
    val geminiQuotaNote get() = s(
        "Google does not share the remaining Gemini free quota with apps. Local counters above show what this app used.",
        "Google ऐप्स को Gemini की बची हुई फ्री सीमा नहीं बताता। ऊपर के अंक इस ऐप के इस्तेमाल दिखाते हैं।",
        "Google apps ko Gemini ki bachi free limit nahi batata. Upar ke anke is app ka usage dikhate hain."
    )
    val resetUsage get() = s("Reset counters", "गिनती रीसेट", "Counters reset")

    val browser get() = s("Browser", "ब्राउज़र", "Browser")
    val terminal get() = s("Terminal", "टर्मिनल", "Terminal")
    val tools get() = s("Extensions", "एक्सटेंशन", "Extensions")
    val mcp get() = s("MCP", "MCP", "MCP")
    val address get() = s("Type a URL or search", "URL या खोज लिखें", "URL ya search likho")
    val go get() = s("Go", "जाएं", "Jao")
    val readPage get() = s("Read page", "पेज पढ़ें", "Page padho")
    val askAboutPage get() = s("Ask AI about page", "पेज के बारे में पूछें", "Page ke baare me poocho")
    val browserHint get() = s(
        "Use the address bar for a link or a search. Read page sends the text to chat.",
        "लिंक या खोज के लिए ऊपर वाला बॉक्स इस्तेमाल करें। Read page पेज का टेक्स्ट चैट में भेजता है।",
        "Link ya search ke liye upar wala box use karo. Read page page ka text chat me bhejta hai."
    )
    val automation get() = s("AI browser action", "AI ब्राउज़र एक्शन", "AI browser action")
    val automationHint get() = s(
        "Describe what to click or fill. AI writes JavaScript; you approve before it runs.",
        "बताएं कि क्या क्लिक या भरना है। AI JavaScript लिखेगा; चलाने से पहले आपko पुष्टि करनी होगी।",
        "Batao ki kya click ya fill karna hai. AI JS likhega; chalane se pehle confirmation hoga."
    )
    val runAction get() = s("Run action", "एक्शन चलाएं", "Action chalao")
    val confirmJs get() = s("Run this JavaScript on the page?", "इस JavaScript को पेज पर चलाएं?", "Ye JS page par chalayein?")

    val terminalHint get() = s(
        "A safe local console: help, date, echo, calc, notes, ls, clear. Not a full Linux shell.",
        "सुरक्षित लोकल कंसोल: help, date, echo, calc, notes, ls, clear। पूरा Linux shell नहीं।",
        "Safe local console: help, date, echo, calc, notes, ls, clear. Full Linux shell nahi."
    )
    val commandHint get() = s("Type a command", "कमांड लिखें", "Command likho")
    val clearOutput get() = s("Clear output", "आउटपुट साफ़ करें", "Output clear")

    val localTools get() = s("Built-in tools", "बिल्ट-इन टूल्स", "Built-in tools")
    val externalTools get() = s("MCP servers", "MCP सर्वर", "MCP servers")
    val addServer get() = s("Add MCP server", "MCP सर्वर जोड़ें", "MCP server add karo")
    val serverName get() = s("Server name", "सर्वर का नाम", "Server ka naam")
    val serverUrl get() = s("Server URL", "सर्वर URL", "Server URL")
    val testServer get() = s("Test", "जांचें", "Test")
    val noServers get() = s("No MCP servers yet.", "अभी कोई MCP सर्वर नहीं।", "Abhi koi MCP server nahi.")
    val toolWeb get() = s("Web search", "वेब खोज", "Web search")
    val toolCalc get() = s("Calculator", "कैलकुलेटर", "Calculator")
    val toolNotes get() = s("Notes", "नोट्स", "Notes")
    val toolTime get() = s("Date & time", "तारीख और समय", "Date & time")
    val toolBrowser get() = s("Browser control", "ब्राउज़र नियंत्रण", "Browser control")

    val imageNotSupported get() = s(
        "This model cannot read images.",
        "यह मॉडल फ़ोटो नहीं पढ़ सकता।",
        "Ye model photo nahi padh sakta."
    )
    val imageNotSupportedBody get() = s(
        "Pick a vision model (Gemini Flash/Pro or Gemma 3) and try again, or remove the photo.",
        "विज़न मॉडल चुनें (Gemini Flash/Pro या Gemma 3) और फिर कोशिश करें, या फ़ोटो हटाएं।",
        "Vision model chuno (Gemini Flash/Pro ya Gemma 3) aur dobara try karo, ya photo hatao."
    )
}

val LocalStr = staticCompositionLocalOf { Str("en") }



// ==================== [ FILE: Markdown.kt ] ====================

package com.antiagent.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private sealed class Block {
    data class Heading(val level: Int, val text: String) : Block()
    data class Code(val code: String) : Block()
    data class Bullet(val items: List<String>) : Block()
    data class Num(val items: List<String>) : Block()
    data class Quote(val text: String) : Block()
    data class Para(val text: String) : Block()
}

private fun parseMarkdown(src: String): List<Block> {
    val lines = src.replace("\r\n", "\n").split("\n")
    val blocks = mutableListOf<Block>()
    val para = StringBuilder()

    fun flushPara() {
        if (para.isNotBlank()) {
            blocks.add(Block.Para(para.toString().trim()))
            para.clear()
        }
    }

    val orderedRegex = Regex("^\\d+[.)]\\s+(.*)$")
    var i = 0
    while (i < lines.size) {
        val raw = lines[i]
        val t = raw.trim()
        when {
            t.startsWith("```") -> {
                flushPara()
                val code = StringBuilder()
                i++
                while (i < lines.size && !lines[i].trim().startsWith("```")) {
                    code.append(lines[i]).append("\n")
                    i++
                }
                blocks.add(Block.Code(code.toString().trimEnd()))
            }
            t.startsWith("### ") -> {
                flushPara()
                blocks.add(Block.Heading(3, t.removePrefix("### ")))
            }
            t.startsWith("## ") -> {
                flushPara()
                blocks.add(Block.Heading(2, t.removePrefix("## ")))
            }
            t.startsWith("# ") -> {
                flushPara()
                blocks.add(Block.Heading(1, t.removePrefix("# ")))
            }
            t.startsWith("- ") || t.startsWith("* ") || t.startsWith("• ") -> {
                flushPara()
                val items = mutableListOf<String>()
                while (i < lines.size) {
                    val tt = lines[i].trim()
                    if (tt.startsWith("- ") || tt.startsWith("* ") || tt.startsWith("• ")) {
                        items.add(tt.substring(2))
                        i++
                    } else {
                        break
                    }
                }
                if (items.isNotEmpty()) blocks.add(Block.Bullet(items))
                continue
            }
            orderedRegex.matches(t) -> {
                flushPara()
                val items = mutableListOf<String>()
                while (i < lines.size) {
                    val m = orderedRegex.find(lines[i].trim())
                    if (m != null) {
                        items.add(m.groupValues[1])
                        i++
                    } else {
                        break
                    }
                }
                if (items.isNotEmpty()) blocks.add(Block.Num(items))
                continue
            }
            t.startsWith("> ") -> {
                flushPara()
                blocks.add(Block.Quote(t.removePrefix("> ")))
            }
            t.isBlank() -> flushPara()
            t == "---" || t == "***" -> flushPara()
            else -> {
                if (para.isNotEmpty()) para.append("\n")
                para.append(t)
            }
        }
        i++
    }
    flushPara()
    return blocks
}

fun mdInline(src: String, color: Color): AnnotatedString = buildAnnotatedString {
    var i = 0
    while (i < src.length) {
        when {
            src.startsWith("**", i) -> {
                val end = src.indexOf("**", i + 2)
                if (end > i + 2) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = color)) {
                        append(src.substring(i + 2, end))
                    }
                    i = end + 2
                } else {
                    append("**")
                    i += 2
                }
            }
            src[i] == '`' -> {
                val end = src.indexOf('`', i + 1)
                if (end > i + 1) {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = color.copy(alpha = 0.10f),
                            color = color
                        )
                    ) {
                        append(src.substring(i + 1, end))
                    }
                    i = end + 1
                } else {
                    append('`')
                    i++
                }
            }
            src.startsWith("~~", i) -> {
                val end = src.indexOf("~~", i + 2)
                if (end > i + 2) {
                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough, color = color)) {
                        append(src.substring(i + 2, end))
                    }
                    i = end + 2
                } else {
                    append("~~")
                    i += 2
                }
            }
            src[i] == '*' && i + 1 < src.length && src[i + 1] != ' ' -> {
                val end = src.indexOf('*', i + 1)
                if (end > i + 1) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = color)) {
                        append(src.substring(i + 1, end))
                    }
                    i = end + 1
                } else {
                    append('*')
                    i++
                }
            }
            else -> {
                append(src[i])
                i++
            }
        }
    }
}

@Composable
fun Markdown(
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    val blocks = remember(text) { parseMarkdown(text) }
    Column(modifier) {
        for (block in blocks) {
            when (block) {
                is Block.Heading -> Text(
                    text = mdInline(block.text, color),
                    fontSize = when (block.level) {
                        1 -> 21.sp
                        2 -> 19.sp
                        else -> 17.sp
                    },
                    fontWeight = FontWeight.Bold,
                    color = color,
                    modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                )
                is Block.Code -> CodeBlock(block.code)
                is Block.Bullet -> Column {
                    for (item in block.items) {
                        Row(Modifier.padding(vertical = 1.dp)) {
                            Text("•  ", color = color)
                            Text(
                                text = mdInline(item, color),
                                color = color,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                is Block.Num -> Column {
                    for ((idx, item) in block.items.withIndex()) {
                        Row(Modifier.padding(vertical = 1.dp)) {
                            Text("${idx + 1}.  ", color = color)
                            Text(
                                text = mdInline(item, color),
                                color = color,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                is Block.Quote -> Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = mdInline(block.text, color),
                        fontStyle = FontStyle.Italic,
                        color = color
                    )
                }
                is Block.Para -> Text(
                    text = mdInline(block.text, color),
                    color = color,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
private fun CodeBlock(code: String) {
    val clipboard = LocalClipboardManager.current
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 2.dp, bottom = 0.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { clipboard.setText(AnnotatedString(code)) }) {
                    Text("Copy", fontSize = 12.sp)
                }
            }
            Text(
                text = code,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
            )
        }
    }
}



// ==================== [ FILE: Drawer.kt ] ====================

package com.antiagent.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antiagent.app.AppViewModel
import com.antiagent.app.data.Personas
import com.antiagent.app.data.Projects

@Composable
fun DrawerContent(vm: AppViewModel, close: () -> Unit) {
    val s = LocalStr.current
    val colors = MaterialTheme.colorScheme
    val query = vm.searchQuery.trim()
    var showNewProject by remember { mutableStateOf(false) }
    var confirmDeleteProject by remember { mutableStateOf(false) }

    val chats = remember(vm.listVersion, query, vm.activeProject) {
        vm.conversations
            .filter { c ->
                (vm.activeProject == Projects.ALL || c.projectId == vm.activeProject) &&
                    (query.isBlank() ||
                        c.title.contains(query, ignoreCase = true) ||
                        c.messages.any { it.text.contains(query, ignoreCase = true) })
            }
            .sortedByDescending { it.updatedAt }
    }

    ModalDrawerSheet(drawerContainerColor = colors.surface) {
        Column(Modifier.padding(start = 20.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)) {
            Text(
                "Anti Agent",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Text(s.formModel, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }

        Text(
            s.projects,
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(start = 26.dp, bottom = 2.dp)
        )

        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            ProjectChip(s.allProjects, vm.activeProject == Projects.ALL) {
                vm.switchProject(Projects.ALL)
            }
            vm.projects.forEach { p ->
                ProjectChip(p.name, vm.activeProject == p.id) { vm.switchProject(p.id) }
            }
            ProjectChip("+ " + s.newProject, false) { showNewProject = true }
        }

        Row(Modifier.padding(horizontal = 16.dp)) {
            Text(
                s.projectHint,
                fontSize = 10.5.sp,
                color = colors.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            if (vm.activeProject != "chat" && vm.activeProject != Projects.ALL) {
                Text(
                    s.deleteProject,
                    fontSize = 10.5.sp,
                    color = colors.error,
                    modifier = Modifier.clickable { confirmDeleteProject = true }
                )
            }
        }

        OutlinedTextField(
            value = vm.searchQuery,
            onValueChange = { vm.searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            placeholder = { Text(s.searchChats, fontSize = 12.5.sp) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        NavigationDrawerItem(
            label = { Text(s.newChat) },
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            selected = false,
            onClick = {
                vm.newChat()
                close()
            },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        HorizontalDivider(Modifier.padding(vertical = 8.dp), color = colors.outline.copy(alpha = 0.4f))

        Text(
            s.chats,
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(start = 26.dp, bottom = 4.dp)
        )

        if (chats.isEmpty()) {
            Text(
                if (vm.conversations.isEmpty()) s.noChats else s.noResults,
                fontSize = 12.sp,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(start = 26.dp, top = 6.dp, bottom = 6.dp)
            )
            Spacer(Modifier.weight(1f))
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(items = chats, key = { it.id }) { c ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                vm.openChat(c.id)
                                vm.searchQuery = ""
                                close()
                            }
                            .padding(start = 26.dp, end = 6.dp, top = 2.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = c.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (c.id == vm.currentId) colors.primary else colors.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { vm.deleteChat(c.id) }, modifier = Modifier.size(34.dp)) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = s.deleteChat,
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = colors.outline.copy(alpha = 0.4f))

        NavigationDrawerItem(
            label = { Text(s.notesTitle) },
            icon = { Icon(Icons.Filled.Notes, contentDescription = null) },
            selected = false,
            onClick = {
                vm.screen = "notes"
                close()
            },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
        )
        NavigationDrawerItem(
            label = { Text(s.settingsTitle) },
            icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
            selected = false,
            onClick = {
                vm.screen = "settings"
                close()
            },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
        )
        Spacer(Modifier.height(10.dp))
    }

    if (showNewProject) {
        NewProjectDialog(vm, onClose = { showNewProject = false })
    }
    if (confirmDeleteProject) {
        AlertDialog(
            onDismissRequest = { confirmDeleteProject = false },
            title = { Text(s.deleteProject) },
            text = { Text(s.deleteProjectConfirm) },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteProject(vm.activeProject)
                    confirmDeleteProject = false
                }) { Text(s.delete, color = colors.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteProject = false }) { Text(s.cancel) }
            }
        )
    }
}

@Composable
private fun ProjectChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) colors.primary else colors.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            fontSize = 12.sp,
            maxLines = 1,
            color = if (selected) colors.onPrimary else colors.onSurfaceVariant
        )
    }
}

@Composable
private fun NewProjectDialog(vm: AppViewModel, onClose: () -> Unit) {
    val s = LocalStr.current
    val colors = MaterialTheme.colorScheme
    var name by remember { mutableStateOf("") }
    var model by remember { mutableStateOf(vm.modelId) }
    var agent by remember { mutableStateOf("general") }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(s.newProject) },
        text = {
            Column {
                Text(s.projectHint, fontSize = 11.5.sp, color = colors.onSurfaceVariant)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(s.projectName) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(s.pickAgent, fontSize = 11.sp, color = colors.onSurfaceVariant)
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    Personas.all.forEach { p ->
                        ProjectChip(s.agentName(p.id), agent == p.id) { agent = p.id }
                        Spacer(Modifier.size(4.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(s.pickModel, fontSize = 11.sp, color = colors.onSurfaceVariant)
                Column(
                    Modifier
                        .heightIn(max = 190.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    vm.models.forEach { m ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { model = m.id }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${m.label} · ${m.providerLabel}",
                                fontSize = 12.sp,
                                color = if (m.id == model) colors.primary else colors.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                vm.addProject(name, model, agent)
                onClose()
            }) { Text(s.create) }
        },
        dismissButton = {
            TextButton(onClick = onClose) { Text(s.cancel) }
        }
    )
}



// ==================== [ FILE: ChatScreen.kt ] ====================

package com.antiagent.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.antiagent.app.AppViewModel
import com.antiagent.app.data.ChatMessage
import com.antiagent.app.data.Personas
import com.antiagent.app.util.FileUtil
import com.antiagent.app.util.ImageUtil
import com.antiagent.app.voice.Speaker
import com.antiagent.app.voice.VoiceInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(vm: AppViewModel, onMenu: () -> Unit) {
    val s = LocalStr.current
    val ctx = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var showModels by remember { mutableStateOf(false) }
    var showAttach by remember { mutableStateOf(false) }
    var showDeleteChat by remember { mutableStateOf(false) }
    var listening by remember { mutableStateOf(false) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    val voice = remember { VoiceInput(ctx) }
    val speaker = remember { Speaker(ctx) }
    DisposableEffect(Unit) {
        onDispose {
            voice.destroy()
            speaker.shutdown()
        }
    }

    LaunchedEffect(vm.lastAnswer) {
        if (vm.autoSpeak && vm.lastAnswer.isNotBlank()) speaker.speak(vm.lastAnswer)
    }
    LaunchedEffect(vm.error) {
        val msg = vm.error
        if (msg != null) {
            snackbar.showSnackbar(msg)
            vm.error = null
        }
    }

    val beginVoice: () -> Unit = {
        listening = true
        voice.start(
            onText = { text ->
                vm.draft = (vm.draft + " " + text).trim()
                listening = false
            },
            onError = { _ ->
                vm.error = s.voiceError
                listening = false
            }
        )
    }

    val micPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) beginVoice() else vm.error = s.micPermission
    }

    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val b64 = withContext(Dispatchers.IO) { ImageUtil.encode(ctx, uri) }
                if (b64 != null) vm.pendingImage = b64 else vm.error = s.photoError
            }
        }
    }

    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok ->
        val uri = cameraUri
        if (ok && uri != null) {
            scope.launch {
                val b64 = withContext(Dispatchers.IO) { ImageUtil.encode(ctx, uri) }
                if (b64 != null) vm.pendingImage = b64 else vm.error = s.photoError
            }
        }
    }

    val launchCamera: () -> Unit = {
        try {
            val dir = File(ctx.cacheDir, "images").apply { mkdirs() }
            val file = File(dir, "cap_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(ctx, ctx.packageName + ".fileprovider", file)
            cameraUri = uri
            takePicture.launch(uri)
        } catch (_: Exception) {
            vm.error = s.photoError
        }
    }

    val pickFile = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val res = withContext(Dispatchers.IO) { FileUtil.readText(ctx, uri) }
                when {
                    res == null -> vm.error = s.fileError
                    res.second == null -> vm.error = s.fileTooBig
                    else -> vm.attachFile(res.first, res.second ?: "")
                }
            }
        }
    }

    val conv = vm.conversations.firstOrNull { it.id == vm.currentId }
    val rows = conv?.messages?.toList() ?: emptyList()
    val modelsSnapshot = remember(vm.modelsVersion) { vm.models }
    val total = rows.size + if (vm.sending) 1 else 0
    val listState = rememberLazyListState()

    LaunchedEffect(total, vm.streamingText.length, vm.chatErrorKind) {
        val target = total + if (vm.chatErrorKind != null) 1 else 0
        if (target > 0) {
            try {
                listState.scrollToItem(target - 1)
            } catch (_: Exception) {
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onMenu) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
                    }
                },
                title = {
                    Column(Modifier.clickable { showModels = true }) {
                        Text(
                            "Anti Agent",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = colors.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                vm.activeModel.label,
                                fontSize = 11.sp,
                                color = colors.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 210.dp)
                            )
                            Icon(
                                Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = colors.primary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { vm.screen = "browser" }) {
                        Icon(Icons.Filled.Public, contentDescription = s.browser)
                    }
                    TextButton(onClick = { vm.screen = "usage" }) {
                        Text("Usage", fontSize = 11.sp)
                    }
                    IconButton(onClick = { vm.newChat() }) {
                        Icon(Icons.Filled.Add, contentDescription = s.newChat)
                    }
                    IconButton(onClick = { showDeleteChat = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = s.deleteChat)
                    }
                    IconButton(onClick = { vm.screen = "settings" }) {
                        Icon(Icons.Filled.Settings, contentDescription = s.settingsTitle)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface,
                    titleContentColor = colors.onSurface,
                    navigationIconContentColor = colors.onSurface,
                    actionIconContentColor = colors.onSurface
                )
            )
        },
        bottomBar = {
            Composer(
                vm = vm,
                listening = listening,
                onAttach = { showAttach = true },
                onMic = {
                    if (listening) {
                        voice.stop()
                        listening = false
                    } else if (ContextCompat.checkSelfPermission(
                            ctx,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        beginVoice()
                    } else {
                        micPermission.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onStop = { vm.stop() }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            PersonaRow(vm)
            Box(Modifier.weight(1f)) {
                if (rows.isEmpty() && !vm.sending && vm.chatErrorKind == null) {
                    EmptyState(
                        needsKey = vm.geminiKey.isBlank() && vm.openRouterKey.isBlank(),
                        onPick = { suggestion -> vm.draft = suggestion },
                        onSettings = { vm.screen = "settings" }
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(count = rows.size, key = { it }) { index ->
                            val message = rows[index]
                            MessageRow(
                                message = message,
                                isLastAssistant = !vm.sending &&
                                    index == rows.lastIndex &&
                                    message.role == "assistant",
                                onSpeak = { speaker.speak(it) },
                                onRegenerate = { vm.regenerate() }
                            )
                        }
                        if (vm.fallbackNotice.isNotBlank()) {
                            item(key = "fallback") {
                                Text(
                                    vm.fallbackNotice,
                                    fontSize = 11.sp,
                                    color = colors.onSurfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (vm.sending) {
                            item(key = "streaming") {
                                if (vm.streamingText.isBlank()) {
                                    TypingBubble()
                                } else {
                                    MessageRow(
                                        message = ChatMessage("assistant", vm.streamingText),
                                        isLastAssistant = false,
                                        onSpeak = {},
                                        onRegenerate = {}
                                    )
                                }
                            }
                        }
                        val errorKind = vm.chatErrorKind
                        if (errorKind != null) {
                            item(key = "error") {
                                ErrorBubble(
                                    kind = errorKind,
                                    detail = vm.chatErrorDetail,
                                    onRetry = { vm.retry() },
                                    onSettings = { vm.screen = "settings" }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showModels) {
        AlertDialog(
            onDismissRequest = { showModels = false },
            title = { Text(s.chooseModel) },
            text = {
                Column(
                    Modifier
                        .heightIn(max = 390.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    var lastProvider = ""
                    modelsSnapshot.forEach { m ->
                        if (m.provider != lastProvider) {
                            lastProvider = m.provider
                            Text(
                                m.providerLabel,
                                fontSize = 11.sp,
                                color = colors.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                            )
                            HorizontalDivider(color = colors.outline.copy(alpha = 0.3f))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    vm.selectModel(m.id)
                                    showModels = false
                                }
                                .padding(vertical = 9.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                m.label,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            if (m.id == vm.modelId) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showModels = false }) { Text(s.close) }
            },
            dismissButton = {
                TextButton(onClick = {
                    vm.connect(vm.activeModel.provider)
                    showModels = false
                }) { Text(s.refreshModels) }
            }
        )
    }

    if (showAttach) {
        AlertDialog(
            onDismissRequest = { showAttach = false },
            title = { Text(s.attach) },
            text = {
                Column {
                    AttachRow(Icons.Filled.PhotoCamera, s.camera) {
                        showAttach = false
                        launchCamera()
                    }
                    AttachRow(Icons.Filled.Image, s.gallery) {
                        showAttach = false
                        pickImage.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                    AttachRow(Icons.Filled.Description, s.file) {
                        showAttach = false
                        pickFile.launch(arrayOf("text/*", "application/json", "application/xml"))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAttach = false }) { Text(s.cancel) }
            }
        )
    }

    if (showDeleteChat) {
        AlertDialog(
            onDismissRequest = { showDeleteChat = false },
            title = { Text(s.deleteChat) },
            text = { Text(s.deleteChatConfirm) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteChat = false
                    vm.deleteChat(vm.currentId)
                }) { Text(s.delete) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteChat = false }) { Text(s.cancel) }
            }
        )
    }
}

@Composable
private fun AttachRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Text(label, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun PersonaRow(vm: AppViewModel) {
    val s = LocalStr.current
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Personas.all.forEach { p ->
            val selected = vm.persona == p.id
            Box(
                Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (selected) colors.primary else colors.surfaceVariant)
                    .clickable { vm.selectPersona(p.id) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    s.agentName(p.id),
                    fontSize = 12.sp,
                    color = if (selected) colors.onPrimary else colors.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ErrorBubble(
    kind: String,
    detail: String?,
    onRetry: () -> Unit,
    onSettings: () -> Unit
) {
    val s = LocalStr.current
    val colors = MaterialTheme.colorScheme
    val headline = when (kind) {
        "no_key_gemini" -> s.errorNoKeyGemini
        "no_key_openrouter" -> s.errorNoKeyOpenRouter
        "empty" -> s.errorEmpty
        "image_unsupported" -> s.imageNotSupported
        else -> s.errorFailed
    }
    val extra = if (kind == "image_unsupported") s.imageNotSupportedBody else null
    Surface(
        color = colors.error.copy(alpha = 0.10f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, colors.error.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(s.errorTitle, fontWeight = FontWeight.Bold, color = colors.error, fontSize = 14.sp)
            Spacer(Modifier.height(4.dp))
            Text(headline, fontSize = 13.sp, color = colors.onSurface)
            if (extra != null) {
                Spacer(Modifier.height(4.dp))
                Text(extra, fontSize = 12.sp, color = colors.onSurfaceVariant)
            }
            if (detail != null && kind == "failed") {
                Spacer(Modifier.height(6.dp))
                Text(s.detail, fontSize = 11.sp, color = colors.onSurfaceVariant)
                Text(
                    detail,
                    fontSize = 10.5.sp,
                    color = colors.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 90.dp)
                        .verticalScroll(rememberScrollState())
                )
            }
            Row(Modifier.padding(top = 6.dp)) {
                TextButton(onClick = onRetry) { Text(s.retry) }
                if (kind.startsWith("no_key") || kind == "failed" || kind == "image_unsupported") {
                    TextButton(onClick = onSettings) { Text(s.openSettings) }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(needsKey: Boolean, onPick: (String) -> Unit, onSettings: () -> Unit) {
    val s = LocalStr.current
    val colors = MaterialTheme.colorScheme
    val suggestions = listOf(s.sug1, s.sug2, s.sug3, s.sug4)
    Box(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.primary),
                contentAlignment = Alignment.Center
            ) {
                Text("A", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = colors.onPrimary)
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "Anti Agent",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Text(s.emptyTagline, fontSize = 13.sp, color = colors.onSurfaceVariant)
            Text(s.formModel, fontSize = 11.sp, color = colors.onSurfaceVariant)
            if (needsKey) {
                Spacer(Modifier.height(14.dp))
                Box(
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.primary)
                        .clickable(onClick = onSettings)
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    Text(
                        s.addFreeKey,
                        color = colors.onPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                suggestions.forEach { text ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surfaceVariant)
                            .clickable { onPick(text) }
                            .padding(12.dp)
                    ) {
                        Text(text, fontSize = 13.sp, color = colors.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
private fun TypingBubble() {
    val s = LocalStr.current
    val colors = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Surface(
            color = colors.surfaceVariant,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp)
        ) {
            Text(
                s.thinking,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                fontSize = 13.sp,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MessageRow(
    message: ChatMessage,
    isLastAssistant: Boolean,
    onSpeak: (String) -> Unit,
    onRegenerate: () -> Unit
) {
    val s = LocalStr.current
    val colors = MaterialTheme.colorScheme
    val clipboard = LocalClipboardManager.current
    val isUser = message.role == "user"

    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isUser) colors.primary else colors.surfaceVariant,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 360.dp)
        ) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
                message.image?.let { b64 ->
                    val bmp = remember(b64) { ImageUtil.decode(b64) }
                    if (bmp != null) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Photo",
                            modifier = Modifier
                                .heightIn(max = 220.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                }
                val textColor = if (isUser) colors.onPrimary else colors.onSurface
                if (isUser) {
                    Text(
                        message.text,
                        color = textColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    SelectionContainer {
                        Markdown(message.text, color = textColor)
                    }
                }
            }
        }

        if (!isUser && message.text.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = { clipboard.setText(AnnotatedString(message.text)) },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Text(s.copy, fontSize = 11.sp)
                }
                TextButton(
                    onClick = { onSpeak(message.text) },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Text(s.readAloud, fontSize = 11.sp)
                }
                if (isLastAssistant) {
                    TextButton(
                        onClick = onRegenerate,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text(s.regenerate, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun Composer(
    vm: AppViewModel,
    listening: Boolean,
    onAttach: () -> Unit,
    onMic: () -> Unit,
    onStop: () -> Unit
) {
    val s = LocalStr.current
    val colors = MaterialTheme.colorScheme
    Surface(color = colors.surface, tonalElevation = 3.dp) {
        Column(
            Modifier
                .imePadding()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            if (vm.pendingImage != null || vm.pendingFileName != null) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val b64 = vm.pendingImage
                    if (b64 != null) {
                        val bmp = remember(b64) { ImageUtil.decode(b64) }
                        if (bmp != null) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = s.photoAttached,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(s.photoAttached, fontSize = 12.sp, color = colors.onSurfaceVariant)
                        }
                    } else {
                        Icon(
                            Icons.Filled.Description,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            vm.pendingFileName ?: s.fileAttached,
                            fontSize = 12.sp,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 220.dp)
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { vm.clearAttachment() }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = s.remove,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.Bottom) {
                IconButton(onClick = onAttach) {
                    Icon(
                        Icons.Filled.AttachFile,
                        contentDescription = s.attach,
                        tint = colors.onSurfaceVariant
                    )
                }
                TextField(
                    value = vm.draft,
                    onValueChange = { vm.draft = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(s.askHint, fontSize = 14.sp, maxLines = 1)
                    },
                    maxLines = 6,
                    shape = RoundedCornerShape(22.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = colors.surfaceVariant,
                        unfocusedContainerColor = colors.surfaceVariant,
                        disabledContainerColor = colors.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.onSurface)
                )
                IconButton(onClick = {
                    if (vm.activeModel.provider != "gemini" && !vm.webSearch) {
                        vm.error = s.webNeedsGemini
                    } else {
                        vm.changeWebSearch(!vm.webSearch)
                    }
                }) {
                    Icon(
                        Icons.Filled.Public,
                        contentDescription = s.webSearch,
                        tint = if (vm.webSearch) colors.primary else colors.onSurfaceVariant
                    )
                }
                IconButton(onClick = onMic) {
                    Icon(
                        Icons.Filled.Mic,
                        contentDescription = s.listening,
                        tint = if (listening) colors.error else colors.onSurfaceVariant
                    )
                }
                if (vm.sending) {
                    FilledIconButton(
                        onClick = onStop,
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = colors.error)
                    ) {
                        Icon(
                            Icons.Filled.Stop,
                            contentDescription = s.stop,
                            tint = colors.onError
                        )
                    }
                } else {
                    FilledIconButton(
                        onClick = { vm.send() },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = colors.primary)
                    ) {
                        Icon(
                            Icons.Filled.Send,
                            contentDescription = s.send,
                            tint = colors.onPrimary
                        )
                    }
                }
            }
        }
    }
}



// ==================== [ FILE: SettingsScreen.kt ] ====================

package com.antiagent.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antiagent.app.AppViewModel
import com.antiagent.app.data.Models
import com.antiagent.app.data.Personas
import com.antiagent.app.data.Prefs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: AppViewModel, onBack: () -> Unit) {
    val s = LocalStr.current
    val colors = MaterialTheme.colorScheme
    val clipboard = LocalClipboardManager.current
    var showModels by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text(s.settingsTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = s.close)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface,
                    titleContentColor = colors.onSurface,
                    navigationIconContentColor = colors.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 10.dp, bottom = 48.dp)
        ) {
            // ------------------------------------------------------ workspace
            SectionTitle(s.workspace, s.workspaceHint)
            NavRow(s.usage, "usage", vm)
            NavRow(s.browser, "browser", vm)
            NavRow(s.terminal, "terminal", vm)
            NavRow(s.tools, "tools", vm)
            NavRow(s.notesTitle, "notes", vm)

            Spacer(Modifier.height(14.dp))

            // ---------------------------------------------------------- keys
            SectionTitle(s.connections, s.connectionsHint)

            ProviderBlock(
                s = s,
                label = s.geminiKey,
                value = vm.geminiKey,
                placeholder = "AIza…",
                help = s.geminiHelp,
                changed = { vm.changeGeminiKey(it) },
                status = vm.statusGemini,
                testing = vm.testingProvider == "gemini",
                onTest = { vm.testProvider("gemini") },
                onClear = { vm.clearKey("gemini") }
            )
            if (vm.geminiKey.trim().startsWith("sk-or-")) {
                Hint(s.keyLooksOpenRouter, colors.error)
            }

            Spacer(Modifier.height(14.dp))

            ProviderBlock(
                s = s,
                label = s.openRouterKey,
                value = vm.openRouterKey,
                placeholder = "sk-or-v1-…",
                help = s.openRouterHelp,
                changed = { vm.changeOpenRouterKey(it) },
                status = vm.statusOpenRouter,
                testing = vm.testingProvider == "openrouter",
                onTest = { vm.testProvider("openrouter") },
                onClear = { vm.clearKey("openrouter") }
            )
            if (vm.openRouterKey.trim().startsWith("AIza")) {
                Hint(s.keyLooksGemini, colors.error)
            }

            // --------------------------------------------------------- models
            Spacer(Modifier.height(22.dp))
            SectionTitle(s.model)
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceVariant)
                    .clickable { showModels = true }
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(vm.activeModel.label, fontSize = 14.sp, color = colors.onSurface)
                    Text(
                        "${vm.activeModel.providerLabel} • ${vm.models.size} ${s.modelsLoaded}",
                        fontSize = 11.sp,
                        color = colors.onSurfaceVariant
                    )
                }
                Text(s.chooseModel, fontSize = 12.sp, color = colors.primary)
            }
            Spacer(Modifier.height(6.dp))
            TextButton(onClick = { vm.connect(vm.activeModel.provider) }) {
                Text(
                    if (vm.testingProvider.isNotEmpty()) s.testing else s.refreshModels,
                    fontSize = 12.sp
                )
            }

            // --------------------------------------------------------- agents
            Spacer(Modifier.height(18.dp))
            SectionTitle(s.agentsLabel, s.agentsHint)
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Personas.all.forEach { p ->
                    ChoiceChip(
                        label = s.agentName(p.id),
                        selected = vm.persona == p.id,
                        onClick = { vm.selectPersona(p.id) }
                    )
                }
            }

            // ------------------------------------------------------- language
            Spacer(Modifier.height(22.dp))
            SectionTitle(s.language, s.languageHint)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ChoiceChip(s.english, vm.language == "en") { vm.changeLanguage("en") }
                ChoiceChip(s.hindi, vm.language == "hi") { vm.changeLanguage("hi") }
                ChoiceChip(s.hinglish, vm.language == "hinglish") { vm.changeLanguage("hinglish") }
            }

            Spacer(Modifier.height(16.dp))
            SectionTitle(s.replyLanguage, s.replyLanguageHint)
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ChoiceChip(s.autoLang, vm.replyLang == "auto") { vm.changeReplyLang("auto") }
                ChoiceChip(s.english, vm.replyLang == "en") { vm.changeReplyLang("en") }
                ChoiceChip(s.hindi, vm.replyLang == "hi") { vm.changeReplyLang("hi") }
                ChoiceChip(s.hinglish, vm.replyLang == "hinglish") { vm.changeReplyLang("hinglish") }
            }

            // --------------------------------------------------- system prompt
            Spacer(Modifier.height(22.dp))
            SectionTitle(s.systemPrompt, s.systemPromptHint)
            OutlinedTextField(
                value = vm.systemPrompt,
                onValueChange = { vm.changeSystemPrompt(it) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                maxLines = 8,
                textStyle = MaterialTheme.typography.bodySmall.copy(color = colors.onSurface)
            )
            TextButton(onClick = { vm.changeSystemPrompt(Prefs.DEFAULT_SYSTEM_PROMPT) }) {
                Text(s.resetPrompt, fontSize = 12.sp)
            }

            // ------------------------------------------------------- creativity
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    s.creativity,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "%.1f".format(vm.temperature),
                    fontSize = 12.sp,
                    color = colors.primary
                )
            }
            Slider(
                value = vm.temperature,
                onValueChange = { vm.changeTemperature(it) },
                valueRange = 0f..1.5f,
                steps = 14
            )

            // --------------------------------------------------------- speech
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        s.autoSpeak,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    Text(s.autoSpeakHint, fontSize = 11.5.sp, color = colors.onSurfaceVariant)
                }
                Switch(checked = vm.autoSpeak, onCheckedChange = { vm.changeAutoSpeak(it) })
            }

            // ---------------------------------------------------------- themes
            Spacer(Modifier.height(22.dp))
            SectionTitle(s.themeLabel)
            Themes.choices.forEach { (id, label) ->
                val selected = vm.theme == id
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { vm.changeTheme(id) }
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(themeDot(id))
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(label, fontSize = 14.sp, color = colors.onSurface, modifier = Modifier.weight(1f))
                    if (selected) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // ------------------------------------------------------------ data
            Spacer(Modifier.height(22.dp))
            SectionTitle(s.dataSection, s.savedOnPhone)
            Text(
                s.clearChats,
                fontSize = 13.sp,
                color = colors.error,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { confirmClear = true }
                    .padding(vertical = 8.dp, horizontal = 4.dp)
            )

            // ----------------------------------------------------- diagnostics
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        s.diagnostics,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    Text(s.diagnosticsHint, fontSize = 11.sp, color = colors.onSurfaceVariant)
                }
                TextButton(onClick = { clipboard.setText(AnnotatedString(vm.diagnosticsText())) }) {
                    Text(s.copy, fontSize = 12.sp)
                }
            }
            val log = vm.diagnosticsText().ifBlank { s.noDiagnostics }
            Text(
                log,
                fontSize = 10.5.sp,
                fontFamily = FontFamily.Monospace,
                color = colors.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 170.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surfaceVariant)
                    .verticalScroll(rememberScrollState())
                    .padding(9.dp)
            )

            // ----------------------------------------------------------- about
            Spacer(Modifier.height(20.dp))
            SectionTitle(s.aboutSection)
            Text(s.about, fontSize = 11.5.sp, color = colors.onSurfaceVariant)
        }
    }

    if (showModels) {
        ModelDialog(vm, onClose = { showModels = false })
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text(s.clearChats) },
            text = { Text(s.clearChatsConfirm) },
            confirmButton = {
                TextButton(onClick = {
                    confirmClear = false
                    vm.clearAllChats()
                }) { Text(s.delete, color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text(s.cancel) }
            }
        )
    }
}

@Composable
private fun NavRow(label: String, target: String, vm: AppViewModel) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colors.surfaceVariant)
            .clickable { vm.screen = target }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.5.sp, color = colors.onSurface, modifier = Modifier.weight(1f))
        Text("›", fontSize = 20.sp, color = colors.onSurfaceVariant)
    }
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun SectionTitle(title: String, hint: String? = null) {
    val colors = MaterialTheme.colorScheme
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = colors.onSurface
    )
    if (!hint.isNullOrBlank()) {
        Text(hint, fontSize = 11.5.sp, color = colors.onSurfaceVariant)
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun Hint(text: String, color: Color) {
    Text(
        text,
        fontSize = 11.sp,
        color = color,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun ProviderBlock(
    s: Str,
    label: String,
    value: String,
    placeholder: String,
    help: String,
    changed: (String) -> Unit,
    status: String,
    testing: Boolean,
    onTest: () -> Unit,
    onClear: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var visible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = changed,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (visible) s.hide else s.show
                )
            }
        }
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = onTest, enabled = !testing) {
            Text(if (testing) s.testing else s.test, fontSize = 12.sp)
        }
        TextButton(onClick = onClear) {
            Text(s.clearKey, fontSize = 12.sp, color = colors.onSurfaceVariant)
        }
    }
    Text(s.testHint, fontSize = 10.5.sp, color = colors.onSurfaceVariant)
    Text(help, fontSize = 11.sp, color = colors.onSurfaceVariant)

    if (status.isNotBlank()) {
        val isError = status.startsWith("ERROR:")
        val color = when {
            isError -> colors.error
            status.startsWith("OK") || status.startsWith("CONNECTED") -> colors.primary
            else -> colors.onSurfaceVariant
        }
        Text(
            statusText(status, s),
            fontSize = 11.5.sp,
            color = color,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.10f))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

private fun statusText(status: String, s: Str): String = when {
    status == "PASTE_KEY" -> s.pasteKeyFirst
    status == "TESTING" -> s.testing
    status.startsWith("CONNECTED:") -> {
        val n = status.removePrefix("CONNECTED:").toIntOrNull() ?: 0
        "${s.connectedN} • $n ${s.modelsLoaded}"
    }
    status == "OK" || status.startsWith("OK:") -> s.testOk
    status.startsWith("ERROR:") -> s.testFailed + " " + status.removePrefix("ERROR:").take(220)
    else -> status
}

@Composable
private fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) colors.primary else colors.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 7.dp)
    ) {
        Text(
            label,
            fontSize = 12.sp,
            color = if (selected) colors.onPrimary else colors.onSurfaceVariant
        )
    }
}

@Composable
private fun ModelDialog(vm: AppViewModel, onClose: () -> Unit) {
    val s = LocalStr.current
    val colors = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(s.chooseModel) },
        text = {
            Column(
                Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                var lastProvider = ""
                vm.models.forEach { m ->
                    if (m.provider != lastProvider) {
                        lastProvider = m.provider
                        Text(
                            m.providerLabel,
                            fontSize = 11.sp,
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                        )
                        HorizontalDivider(color = colors.outline.copy(alpha = 0.3f))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                vm.selectModel(m.id)
                                onClose()
                            }
                            .padding(vertical = 9.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            m.label,
                            fontSize = 13.sp,
                            color = colors.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        if (m.id == vm.modelId) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onClose) { Text(s.close) }
        },
        dismissButton = {
            TextButton(onClick = { vm.connect(vm.activeModel.provider) }) {
                Text(s.refreshModels, fontSize = 12.sp)
            }
        }
    )
}

private fun themeDot(id: String): Color = when (id) {
    Themes.CARBON -> Color(0xFF34D399)
    Themes.OCEAN -> Color(0xFF38BDF8)
    Themes.DAYLIGHT -> Color(0xFF6D28D9)
    Themes.SAND -> Color(0xFFB45309)
    else -> Color(0xFF8B5CF6)
}



// ==================== [ FILE: NotesScreen.kt ] ====================

package com.antiagent.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antiagent.app.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(vm: AppViewModel, onBack: () -> Unit) {
    val s = LocalStr.current
    val colors = MaterialTheme.colorScheme
    var text by remember { mutableStateOf("") }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text(s.notesTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = s.close)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface,
                    titleContentColor = colors.onSurface,
                    navigationIconContentColor = colors.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .padding(top = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(s.notesPlaceholder, fontSize = 14.sp) },
                    maxLines = 3,
                    shape = RoundedCornerShape(14.dp)
                )
                Spacer(Modifier.width(8.dp))
                FilledIconButton(
                    onClick = {
                        vm.addNote(text)
                        text = ""
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = colors.primary)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = s.add)
                }
            }

            Spacer(Modifier.height(10.dp))

            if (vm.notes.isEmpty()) {
                Text(
                    s.notesEmpty,
                    fontSize = 13.sp,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(items = vm.notes, key = { it.id }) { note ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = note.done,
                                onCheckedChange = { vm.toggleNote(note.id) }
                            )
                            Text(
                                text = note.text,
                                fontSize = 14.sp,
                                color = if (note.done) colors.onSurfaceVariant else colors.onSurface,
                                textDecoration = if (note.done) TextDecoration.LineThrough else null,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { vm.deleteNote(note.id) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = s.deleteNote,
                                    tint = colors.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}



// ==================== [ FILE: TerminalScreen.kt ] ====================

package com.antiagent.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antiagent.app.AppViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TermLine(val text: String, val command: Boolean = false, val error: Boolean = false)

private const val AI_SYSTEM =
    "You are a shell-like assistant inside a safe Android console. Answer the command with a " +
        "short, plain-text result (no markdown, no preamble). If it is a calculation, give the " +
        "number. If it is a question, answer in one or two lines."

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(vm: AppViewModel, onBack: () -> Unit) {
    val s = LocalStr.current
    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val lines = remember { mutableStateListOf<TermLine>() }
    var input by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()

    if (lines.isEmpty()) {
        lines.add(TermLine("Anti Agent Console — safe local commands. Type 'help'.", error = false))
        lines.add(TermLine(s.terminalHint))
    }

    fun print(line: TermLine) {
        lines.add(line)
    }

    fun runCommand(raw: String) {
        val cmd = raw.trim()
        if (cmd.isBlank()) return
        print(TermLine("$ ${cmd}", command = true))
        input = ""
        val parts = cmd.split(Regex("\\s+"), limit = 2)
        val name = parts[0].lowercase(Locale.getDefault())
        val arg = parts.getOrNull(1) ?: ""
        when (name) {
            "help", "?" -> print(TermLine(
                "help | date | echo <text> | calc <expr> | notes [add <text>] | " +
                    "ls | clear | ai <prompt> | send <prompt>"
            ))
            "date", "time" -> print(TermLine(
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            ))
            "echo" -> print(TermLine(arg))
            "clear", "cls" -> lines.clear()
            "ls" -> {
                print(TermLine("projects/  notes/  chats/  browser/  tools/"))
                print(TermLine("${vm.projects.size} projects · ${vm.notes.size} notes · ${vm.conversations.size} chats"))
            }
            "notes" -> {
                if (arg.startsWith("add ")) {
                    vm.addNote(arg.removePrefix("add "))
                    print(TermLine("Note added. (${vm.notes.size} total)"))
                } else {
                    if (vm.notes.isEmpty()) print(TermLine("(no notes)"))
                    else vm.notes.takeLast(10).forEach { print(TermLine("· ${it.text}")) }
                }
            }
            "calc" -> {
                val result = try {
                    evalExpr(arg)
                } catch (e: Exception) {
                    "error: ${e.message}"
                }
                print(TermLine(result, error = result.startsWith("error")))
            }
            "ai" -> {
                if (arg.isBlank()) {
                    print(TermLine("usage: ai <prompt>", error = true))
                } else if (!vm.hasAnyKey()) {
                    print(TermLine("Add an API key in Settings first.", error = true))
                } else {
                    busy = true
                    scope.launch {
                        try {
                            print(TermLine(vm.aiAction(AI_SYSTEM, arg).take(2000)))
                        } catch (e: Exception) {
                            print(TermLine(e.message ?: "failed", error = true))
                        } finally {
                            busy = false
                        }
                    }
                }
            }
            "send" -> {
                if (arg.isBlank()) print(TermLine("usage: send <prompt>", error = true))
                else {
                    vm.sendFromExternal(arg)
                    print(TermLine("Sent to chat."))
                }
            }
            else -> print(TermLine("command not found: $name — type 'help'", error = true))
        }
    }

    Scaffold(
        containerColor = Color(0xFF07090D),
        topBar = {
            TopAppBar(
                title = { Text(s.terminal, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = s.close)
                    }
                },
                actions = {
                    TextButton(onClick = { lines.clear() }) { Text(s.clearOutput) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface,
                    titleContentColor = colors.onSurface,
                    navigationIconContentColor = colors.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scroll)
                    .padding(vertical = 8.dp)
            ) {
                lines.forEach { line ->
                    Text(
                        line.text,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.5.sp,
                        color = when {
                            line.error -> Color(0xFFFF6B6B)
                            line.command -> Color(0xFF22D3EE)
                            else -> Color(0xFFD6DEE8)
                        },
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text("$ ", color = Color(0xFF22D3EE), fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    placeholder = { Text(s.commandHint, fontSize = 12.sp) },
                    singleLine = true,
                    enabled = !busy,
                    textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                    keyboardActions = KeyboardActions(onDone = { runCommand(input) }),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
                TextButton(enabled = !busy, onClick = { runCommand(input) }) {
                    Text(if (busy) "…" else s.send)
                }
            }
        }
    }
}

/** Tiny safe arithmetic evaluator: digits, + - * / ( ) and decimals only. */
private fun evalExpr(expr: String): String {
    if (expr.isBlank()) return "0"
    if (!expr.matches(Regex("[0-9+\\-*/(). ]+"))) throw IllegalArgumentException("only math allowed")
    val value = evaluate(expr)
    return if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
}

private fun evaluate(input: String): Double = ExprParser(input.replace(" ", "")).parse()

private class ExprParser(private val s: String) {
    private var pos = 0

    fun parse(): Double {
        val v = expr()
        if (pos < s.length) throw IllegalArgumentException("unexpected character")
        return v
    }

    private fun expr(): Double {
        var v = term()
        while (pos < s.length && (s[pos] == '+' || s[pos] == '-')) {
            val op = s[pos++]
            val r = term()
            v = if (op == '+') v + r else v - r
        }
        return v
    }

    private fun term(): Double {
        var v = factor()
        while (pos < s.length && (s[pos] == '*' || s[pos] == '/')) {
            val op = s[pos++]
            val r = factor()
            v = if (op == '*') v * r else v / r
        }
        return v
    }

    private fun factor(): Double {
        if (pos >= s.length) throw IllegalArgumentException("incomplete")
        if (s[pos] == '(') {
            pos++
            val v = expr()
            if (pos >= s.length || s[pos] != ')') throw IllegalArgumentException("missing )")
            pos++
            return v
        }
        var sign = 1.0
        while (pos < s.length && (s[pos] == '-' || s[pos] == '+')) {
            if (s[pos] == '-') sign *= -1
            pos++
        }
        val start = pos
        while (pos < s.length && (s[pos].isDigit() || s[pos] == '.')) pos++
        if (start == pos) throw IllegalArgumentException("number expected")
        return sign * s.substring(start, pos).toDouble()
    }
}



// ==================== [ FILE: BrowserScreen.kt ] ====================

package com.antiagent.app.ui

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.antiagent.app.AppViewModel
import kotlinx.coroutines.launch

private const val JS_SYSTEM =
    "You control a web page inside an Android WebView. Return ONLY a single line of valid " +
        "JavaScript to carry out the user's request on the current page. No explanation, no " +
        "markdown, no code fences. Use document.querySelector / getElementById / click() / " +
        "value assignment. Never navigate to a different origin or exfiltrate data."
private const val ASK_SYSTEM =
    "You are a helpful assistant. Answer the user's question using the page text provided. " +
        "Be concise and cite the page when useful."

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(vm: AppViewModel, onBack: () -> Unit) {
    val s = LocalStr.current
    val colors = MaterialTheme.colorScheme
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var address by remember { mutableStateOf("https://www.google.com") }
    var loadedUrl by remember { mutableStateOf("https://www.google.com") }
    var progress by remember { mutableStateOf(0) }
    var pageText by remember { mutableStateOf("") }
    var actionDraft by remember { mutableStateOf("") }
    var pendingJs by remember { mutableStateOf<String?>(null) }
    var asking by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    // Keep a single real WebView instance across recompositions.
    val webView = remember {
        WebView(ctx.applicationContext).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            settings.userAgentString =
                "Mozilla/5.0 (Linux; Android 12; AntiAgent) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/120 Mobile Safari/537.36"
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    pageText = ""
                    evaluateJavascript("document.body.innerText") { value ->
                        pageText = if (value == null) "" else
                            value.trim('"').replace("\\n", "\n").take(20000)
                    }
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    progress = newProgress
                }
            }
            loadUrl(loadedUrl)
        }
    }

    fun navigate(raw: String) {
        val q = raw.trim()
        if (q.isBlank()) return
        address = q
        val url = when {
            q.contains(" ") && !q.contains(".") ->
                "https://duckduckgo.com/?q=" + android.net.Uri.encode(q)
            q.startsWith("http://") || q.startsWith("https://") -> q
            else -> "https://" + q
        }
        loadedUrl = url
        address = url
        pageText = ""
        webView.loadUrl(url)
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(s.browser, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = s.close)
                        }
                    },
                    actions = {
                        TextButton(onClick = {
                            webView.evaluateJavascript("history.back()") { navigate(loadedUrl) }
                        }) { Text("‹", fontSize = 20.sp) }
                        TextButton(onClick = { webView.reload() }) { Text("⟳", fontSize = 18.sp) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colors.surface,
                        titleContentColor = colors.onSurface,
                        navigationIconContentColor = colors.onSurface
                    )
                )
                if (progress in 1..99) {
                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier.fillMaxWidth().height(2.dp),
                        color = colors.primary,
                        trackColor = colors.surfaceVariant
                    )
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // ------------------------------------------------------- address bar
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(s.address, fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.width(6.dp))
                Button(onClick = { navigate(address) }) { Text(s.go) }
            }

            // ------------------------------------------------------------ webview
            AndroidView(
                factory = { webView },
                modifier = Modifier.weight(1f).fillMaxWidth()
            )

            // ---------------------------------------------------------- ai actions
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(s.automation, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.onSurface)
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = {
                        webView.evaluateJavascript("document.body.innerText") { value ->
                            pageText = if (value == null) "" else
                                value.trim('"').replace("\\n", "\n").take(20000)
                        }
                        message = s.browserHint
                    }) { Text(s.readPage, fontSize = 11.5.sp) }
                }
                Text(s.automationHint, fontSize = 10.5.sp, color = colors.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = actionDraft,
                        onValueChange = { actionDraft = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("e.g. click the Login button", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Button(
                        enabled = actionDraft.isNotBlank() && !asking,
                        onClick = {
                            asking = true
                            message = ""
                            scope.launch {
                                try {
                                    val draft = vm.aiAction(JS_SYSTEM, actionDraft.trim())
                                    pendingJs = draft
                                        .removePrefix("```javascript")
                                        .removePrefix("```js")
                                        .removePrefix("```")
                                        .removeSuffix("```")
                                        .trim()
                                } catch (e: Exception) {
                                    message = e.message ?: "failed"
                                } finally {
                                    asking = false
                                }
                            }
                        }
                    ) {
                        Text(if (asking) "…" else s.runAction, fontSize = 11.5.sp)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = {
                        if (pageText.isBlank()) {
                            message = s.browserHint
                        } else {
                            scope.launch {
                                vm.sendFromExternal(
                                    "Read this web page (${loadedUrl}):\n\n" +
                                        pageText.take(6000) +
                                        "\n\nQuestion: " +
                                        (actionDraft.trim().ifBlank { "Summarize this page." })
                                )
                                message = "Sent to chat."
                            }
                        }
                    }) { Text(s.askAboutPage, fontSize = 11.5.sp) }
                    Spacer(Modifier.weight(1f))
                    if (message.isNotBlank()) {
                        Text(message, fontSize = 10.5.sp, color = colors.onSurfaceVariant, maxLines = 2)
                    }
                }
            }
        }
    }

    pendingJs?.let { js ->
        AlertDialog(
            onDismissRequest = { pendingJs = null },
            title = { Text(s.confirmJs) },
            text = {
                Text(
                    js.take(600),
                    fontSize = 11.5.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = colors.onSurface
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    webView.evaluateJavascript("(function(){ $js })()") { result ->
                        message = "Result: " + (result ?: "done")
                    }
                    pendingJs = null
                }) { Text(s.runAction) }
            },
            dismissButton = {
                TextButton(onClick = { pendingJs = null }) { Text(s.cancel) }
            }
        )
    }
}



// ==================== [ FILE: ToolsScreen.kt ] ====================

package com.antiagent.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antiagent.app.AppViewModel
import com.antiagent.app.util.Mcp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(vm: AppViewModel, onBack: () -> Unit) {
    val s = LocalStr.current
    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    var showAdd by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf("") }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text(s.tools, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = s.close)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface,
                    titleContentColor = colors.onSurface,
                    navigationIconContentColor = colors.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 10.dp, bottom = 48.dp)
        ) {
            // ------------------------------------------------ built-in tools
            Text(s.localTools, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.onSurface)
            Text("Toggles change what the AI is told it can use.", fontSize = 11.5.sp, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))

            ToolToggle(s.toolTime, "time", vm)
            ToolToggle(s.toolCalc, "calc", vm)
            ToolToggle(s.toolNotes, "notes", vm)
            ToolToggle(s.toolWeb, "web", vm)
            ToolToggle(s.toolBrowser, "browser", vm)

            Spacer(Modifier.height(22.dp))

            // -------------------------------------------------- MCP servers
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(s.externalTools, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.onSurface)
                    Text(
                        "Connect your own MCP server over HTTP. Keys stay on this phone.",
                        fontSize = 11.5.sp,
                        color = colors.onSurfaceVariant
                    )
                }
                Button(onClick = { showAdd = true }) { Text(s.addServer, fontSize = 11.5.sp) }
            }
            Spacer(Modifier.height(8.dp))

            if (vm.mcpServers.isEmpty()) {
                Text(s.noServers, fontSize = 12.sp, color = colors.onSurfaceVariant)
            } else {
                vm.mcpServers.forEach { server ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(server.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.onSurface)
                                Text(server.url, fontSize = 10.5.sp, color = colors.onSurfaceVariant, maxLines = 1)
                            }
                            Switch(
                                checked = server.enabled,
                                onCheckedChange = { vm.toggleMcpServer(server.id) }
                            )
                        }
                        Row {
                            TextButton(onClick = {
                                scope.launch {
                                    testResult = "Testing ${server.name}…"
                                    try {
                                        testResult = Mcp.ping(server.url)
                                    } catch (e: Exception) {
                                        testResult = "Failed: " + (e.message ?: "unknown")
                                    }
                                }
                            }) { Text(s.testServer, fontSize = 11.5.sp) }
                            TextButton(onClick = { vm.deleteMcpServer(server.id) }) {
                                Text(s.delete, fontSize = 11.5.sp, color = colors.error)
                            }
                        }
                    }
                }
            }

            if (testResult.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    testResult,
                    fontSize = 11.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = colors.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "Extensions run through built-in tools and optional MCP servers. " +
                    "A full desktop-style extension store needs a backend, which this free, " +
                    "offline app intentionally does not use.",
                fontSize = 11.sp,
                color = colors.onSurfaceVariant
            )
        }
    }

    if (showAdd) {
        var name by remember { mutableStateOf("") }
        var url by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text(s.addServer) },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(s.serverName) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        label = { Text(s.serverUrl) },
                        placeholder = { Text("https://my-server.example/mcp") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.addMcpServer(name, url)
                    showAdd = false
                }) { Text(s.add) }
            },
            dismissButton = {
                TextButton(onClick = { showAdd = false }) { Text(s.cancel) }
            }
        )
    }
}

@Composable
private fun ToolToggle(label: String, id: String, vm: AppViewModel) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(colors.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = colors.onSurface, modifier = Modifier.weight(1f))
        Switch(checked = id in vm.enabledTools, onCheckedChange = { vm.toggleTool(id) })
    }
}



// ==================== [ FILE: UsageScreen.kt ] ====================

package com.antiagent.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antiagent.app.AppViewModel
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageScreen(vm: AppViewModel, onBack: () -> Unit) {
    val s = LocalStr.current
    val colors = MaterialTheme.colorScheme
    val today = rememberToday()

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text(s.usage, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = s.close)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface,
                    titleContentColor = colors.onSurface,
                    navigationIconContentColor = colors.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 10.dp, bottom = 48.dp)
        ) {
            Text(s.usage, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.onSurface)
            Text(s.usageHint, fontSize = 11.5.sp, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))

            // ------------------------------------------ OpenRouter balance
            Text(s.balance, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = colors.onSurface)
            Spacer(Modifier.height(4.dp))
            val info = vm.keyInfoOpenRouter
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { vm.refreshKeyInfo() }) {
                    Text(if (info.isBlank()) s.refreshBalance else s.refreshBalance, fontSize = 12.sp)
                }
            }
            when {
                vm.openRouterKey.isBlank() -> Text(s.noBalance, fontSize = 12.sp, color = colors.onSurfaceVariant)
                info.isBlank() -> Text("…", fontSize = 12.sp, color = colors.onSurfaceVariant)
                info.startsWith("ERROR:") -> Text(
                    info.removePrefix("ERROR:"),
                    fontSize = 11.5.sp,
                    color = colors.error
                )
                else -> {
                    val parsed = remember(info) { parseOrInfo(info) }
                    BalanceBox(
                        used = parsed.first,
                        limit = parsed.second,
                        left = parsed.third,
                        raw = parsed.fourth
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = colors.outline.copy(alpha = 0.35f))
            Spacer(Modifier.height(12.dp))

            // ------------------------------------------ local counters
            Text(s.today + " · " + today, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = colors.onSurface)
            Spacer(Modifier.height(6.dp))

            val rows = vm.usage.filter { it.day == today }.sortedByDescending { it.requests + it.inputTokens + it.outputTokens }
            val allRows = vm.usage.sortedByDescending { it.requests + it.inputTokens + it.outputTokens }
            val totalReq = allRows.sumOf { it.requests }
            val totalIn = allRows.sumOf { it.inputTokens }
            val totalOut = allRows.sumOf { it.outputTokens }

            StatLine(s.requests, totalReq.toString(), s.today + ": " + rows.sumOf { it.requests })
            StatLine(s.tokensIn, formatNum(totalIn), s.today + ": " + formatNum(rows.sumOf { it.inputTokens }))
            StatLine(s.tokensOut, formatNum(totalOut), s.today + ": " + formatNum(rows.sumOf { it.outputTokens }))

            Spacer(Modifier.height(14.dp))
            Text(s.allTime + " · " + s.projects, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = colors.onSurface)
            Spacer(Modifier.height(4.dp))
            if (allRows.isEmpty()) {
                Text("—", fontSize = 12.sp, color = colors.onSurfaceVariant)
            } else {
                allRows.forEach { row ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            row.key,
                            fontSize = 11.5.sp,
                            color = colors.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                        Text(
                            "${row.provider} · ${s.requests}: ${row.requests} · " +
                                "${s.tokensIn}: ${formatNum(row.inputTokens)} · " +
                                "${s.tokensOut}: ${formatNum(row.outputTokens)}",
                            fontSize = 10.5.sp,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Text(s.geminiQuotaNote, fontSize = 11.sp, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            TextButton(onClick = { vm.resetUsage() }) {
                Text(s.resetUsage, fontSize = 12.sp, color = colors.error)
            }
        }
    }
}

@Composable
private fun rememberToday(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

private fun formatNum(n: Long): String = when {
    n >= 1_000_000 -> "%.1fM".format(n / 1_000_000.0)
    n >= 1_000 -> "%.1fk".format(n / 1_000.0)
    else -> n.toString()
}

/** used, limit, remaining, extra label */
private fun parseOrInfo(raw: String): Quadruple<String, String, String, String> {
    return try {
        val root = JSONObject(raw)
        val d = root.optJSONObject("data") ?: root
        val used = d.optString("usage", "—")
        val limit = d.optString("limit", "—")
        val label = d.optString("label", "")
        val left = if (used != "—" && limit != "—" && limit != "null") {
            val u = used.toDoubleOrNull()
            val l = limit.toDoubleOrNull()
            if (u != null && l != null) "%.4f".format(l - u) else "—"
        } else if (limit == "null" || limit == "—") "Unlimited (free tier)" else "—"
        Quadruple(used, limit, left, label)
    } catch (_: Exception) {
        Quadruple("—", "—", "—", raw.take(180))
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
private fun BalanceBox(used: String, limit: String, left: String, raw: String) {
    val s = LocalStr.current
    val colors = MaterialTheme.colorScheme
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceVariant)
            .padding(12.dp)
    ) {
        Text(raw, fontSize = 11.sp, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text(s.balanceUsed, fontSize = 10.sp, color = colors.onSurfaceVariant)
                Text(used, fontSize = 14.sp, color = colors.onSurface, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f)) {
                Text(s.balanceLimit, fontSize = 10.sp, color = colors.onSurfaceVariant)
                Text(limit, fontSize = 14.sp, color = colors.onSurface, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f)) {
                Text(s.balanceLeft, fontSize = 10.sp, color = colors.onSurfaceVariant)
                Text(left, fontSize = 12.sp, color = colors.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StatLine(label: String, value: String, sub: String) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text(value, fontSize = 15.sp, color = colors.onSurface, fontWeight = FontWeight.Bold)
            Text(sub, fontSize = 10.sp, color = colors.onSurfaceVariant)
        }
    }
}


