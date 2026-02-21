package in.intelligentindia.news.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import in.intelligentindia.news.data.NewsPost
import in.intelligentindia.news.data.NewsSection
import in.intelligentindia.news.data.TumblrApiFactory
import in.intelligentindia.news.data.TumblrRepository
import in.intelligentindia.news.data.sections
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NewsUiState(
    val loading: Boolean = true,
    val featured: NewsPost? = null,
    val sections: List<NewsSection> = sections,
    val postsBySection: Map<String, List<NewsPost>> = emptyMap(),
    val latest: List<NewsPost> = emptyList(),
    val error: String? = null
)

class NewsViewModel : ViewModel() {
    private val repository = TumblrRepository(
        api = TumblrApiFactory.create(),
        apiKey = "pIigNuNOSzMNtzJeoOaJTsGu3yU9nk9GWi3MFevkN1VtBdyUDb",
        blogDomain = "intelligentindia-blog.tumblr.com"
    )

    private val _uiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    private val sectionOffsets = mutableMapOf<String, Int>()
    private var latestOffset = 9

    init {
        sections.forEach { sectionOffsets[it.id] = 0 }
        loadInitial()
    }

    fun loadInitial() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            runCatching {
                val featuredDeferred = async { repository.featured() }
                val latestDeferred = async { repository.latest(limit = 8, offset = 1) }
                val sectionDeferred = sections.map { section ->
                    async {
                        section.id to repository.bySection(section, limit = 4)
                    }
                }

                val sectionMap = sectionDeferred.awaitAll().toMap()
                sectionMap.forEach { (id, posts) -> sectionOffsets[id] = posts.size }

                _uiState.value = _uiState.value.copy(
                    loading = false,
                    featured = featuredDeferred.await(),
                    latest = latestDeferred.await(),
                    postsBySection = sectionMap
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = throwable.message ?: "Failed to load news"
                )
            }
        }
    }

    fun loadMoreSection(section: NewsSection) {
        viewModelScope.launch {
            val currentOffset = sectionOffsets[section.id] ?: 0
            val newPosts = repository.bySection(section, limit = 4, offset = currentOffset)
            if (newPosts.isEmpty()) return@launch

            val updated = _uiState.value.postsBySection.toMutableMap()
            updated[section.id] = updated[section.id].orEmpty() + newPosts
            sectionOffsets[section.id] = currentOffset + newPosts.size
            _uiState.value = _uiState.value.copy(postsBySection = updated)
        }
    }

    fun loadMoreLatest() {
        viewModelScope.launch {
            val newPosts = repository.latest(limit = 8, offset = latestOffset)
            if (newPosts.isEmpty()) return@launch
            latestOffset += newPosts.size
            _uiState.value = _uiState.value.copy(latest = _uiState.value.latest + newPosts)
        }
    }
}
