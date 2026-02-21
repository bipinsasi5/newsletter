# Intelligent India Android App

A native Android news app (Jetpack Compose + Retrofit) for **Intelligent India** powered directly by the Tumblr API values from your provided template.

## Features

- Breaking-news style top header.
- Featured story section (tag-based fallback to latest post).
- Category sections matching your template:
  - Politics
  - Travel
  - Top Stories
  - Technology
  - Entertainment
- “More News” feed with pagination.
- Load-more actions for each section.
- Story cards with image, date, excerpt, and tags.
- Tap any story to open the original post URL.

## API details reused from template

- Blog: `intelligentindia-blog.tumblr.com`
- API key: same key as the template JavaScript
- Endpoint: `https://api.tumblr.com/v2/blog/{blogIdentifier}/posts`
- Featured tag: `Featured`

## Tech stack

- Kotlin
- Jetpack Compose (Material 3)
- Retrofit + Gson
- OkHttp logging interceptor
- Coil (image loading)
- Jsoup (HTML parsing/excerpt extraction)

## Build and run

1. Open this folder in Android Studio (Hedgehog/Koala+).
2. Let Gradle sync.
3. Run the `app` module on an emulator or physical Android device.

> The project is configured with `compileSdk 34`, `targetSdk 34`, and `minSdk 24`.

## Quick visual preview

You can preview the Android app experience in-browser (mobile frame + live Tumblr data) using:

```bash
python3 -m http.server 4173
# then open http://localhost:4173/preview/
```
