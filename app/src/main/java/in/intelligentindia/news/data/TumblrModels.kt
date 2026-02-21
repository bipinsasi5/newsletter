package in.intelligentindia.news.data

import com.google.gson.annotations.SerializedName

data class TumblrEnvelope(
    val meta: TumblrMeta,
    val response: TumblrResponse
)

data class TumblrMeta(
    val status: Int
)

data class TumblrResponse(
    val posts: List<TumblrPost>
)

data class TumblrPost(
    val id: Long,
    @SerializedName("id_string") val idString: String?,
    val title: String?,
    val summary: String?,
    val body: String?,
    val caption: String?,
    val description: String?,
    val timestamp: Long,
    @SerializedName("post_url") val postUrl: String,
    val tags: List<String> = emptyList(),
    val slug: String?,
    val type: String,
    val url: String?,
    val photos: List<TumblrPhoto>?
)

data class TumblrPhoto(
    @SerializedName("original_size") val originalSize: TumblrPhotoSize?
)

data class TumblrPhotoSize(
    val url: String
)

data class NewsPost(
    val id: String,
    val title: String,
    val contentHtml: String,
    val excerpt: String,
    val imageUrl: String,
    val dateText: String,
    val sourceUrl: String,
    val tags: List<String>
)

data class NewsSection(
    val id: String,
    val label: String,
    val tag: String
)

val sections = listOf(
    NewsSection("politics", "Politics", "Politics"),
    NewsSection("travel", "Travel", "Travel"),
    NewsSection("top", "Top Stories", "Top Stories"),
    NewsSection("tech", "Technology", "Technology"),
    NewsSection("entertainment", "Entertainment", "Entertainment")
)
