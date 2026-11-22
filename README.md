# Anti Agent

Free AI agent app for Android — built entirely on a phone, built free on GitHub Actions.

## Features (v1)

- Chat with streaming replies
- Free models: Google Gemini (free API key) + OpenRouter free models
- Model picker, saved chat history, auto titles
- Voice input (phone speech recognition) and read-aloud (phone TTS)
- Photo questions (Gemini vision / OpenRouter vision models)
- Notes & tasks, three themes
- No server, no subscription: everything runs on the device with your own free API key

## Build

Push to `main` (or run the workflow manually). GitHub Actions builds the debug APK and
attaches `AntiAgent.apk` to a release. Open **Releases → latest → AntiAgent.apk** on the phone.

## Get free API keys

- Gemini: https://aistudio.google.com/apikey → Create API key
- OpenRouter: https://openrouter.ai/keys → Create key (use `:free` models)

Paste the keys inside the app (Settings). They are stored only on the phone.
