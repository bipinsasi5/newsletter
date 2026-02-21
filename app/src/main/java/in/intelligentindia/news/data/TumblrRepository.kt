package in.intelligentindia.news.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.jsoup.Jsoup

class TumblrRepository(
    private val api: TumblrApi,
    private val apiKey: String,
    private val blogDomain: String
) {
    private val formatter = SimpleDateFormat("dd MMM yyyy", Locale("en", "IN"))
    private val fallbackImage =
        "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=1200&auto=format&fit=crop&q=60"

    suspend fun featured(): NewsPost? {
        val tagged = fetch(tag = "Featured", limit = 1, offset = 0)
        return tagged.firstOrNull() ?: latest(limit = 1, offset = 0).firstOrNull()
    }

    suspend fun bySection(section: NewsSection, limit: Int = 4, offset: Int = 0): List<NewsPost> =
        fetch(tag = section.tag, limit = limit, offset = offset)

    suspend fun latest(limit: Int = 8, offset: Int = 0): List<NewsPost> =
        fetch(tag = null, limit = limit, offset = offset)

    private suspend fun fetch(tag: String?, limit: Int, offset: Int): List<NewsPost> {
        val response = api.posts(
            blogIdentifier = blogDomain,
            apiKey = apiKey,
            tag = tag,
            limit = limit,
            offset = offset
        )
        if (response.meta.status != 200) return emptyList()
        return response.response.posts.map(::normalize)
    }

    private fun normalize(post: TumblrPost): NewsPost {
        val title = post.title ?: post.summary ?: "Untitled Story"
        var html = post.body ?: post.caption ?: post.description.orEmpty()

        if (post.type == "link") {
            val link = post.url.orEmpty()
            html = "<p><a href=\"$link\">$link</a></p>${post.description.orEmpty()}"
        }

        val parsed = Jsoup.parse(html)
        val text = parsed.text()
        val firstImage = post.photos?.firstOrNull()?.originalSize?.url
            ?: parsed.selectFirst("img")?.attr("src")
            ?: fallbackImage

        val excerpt = if (text.length > 130) "${text.take(130)}..." else text

        return NewsPost(
            id = post.idString ?: post.id.toString(),
            title = title,
            contentHtml = html,
            excerpt = excerpt,
            imageUrl = firstImage,
            dateText = formatter.format(Date(post.timestamp * 1000)),
            sourceUrl = post.postUrl,
            tags = post.tags
        )
    }
}
