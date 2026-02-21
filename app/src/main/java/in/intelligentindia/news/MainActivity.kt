package in.intelligentindia.news

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import in.intelligentindia.news.data.NewsPost
import in.intelligentindia.news.data.NewsSection
import in.intelligentindia.news.ui.NewsViewModel

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<NewsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                val state by viewModel.uiState.collectAsState()
                NewsScreen(
                    state = state,
                    onRefresh = viewModel::loadInitial,
                    onLoadMoreSection = viewModel::loadMoreSection,
                    onLoadMoreLatest = viewModel::loadMoreLatest
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewsScreen(
    state: in.intelligentindia.news.ui.NewsUiState,
    onRefresh: () -> Unit,
    onLoadMoreSection: (NewsSection) -> Unit,
    onLoadMoreLatest: () -> Unit
) {
    Scaffold(
        topBar = {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1F2937))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Breaking News: Intelligent India Magazine is now live.",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                TopAppBar(
                    title = {
                        Column {
                            Text("Intelligent India", fontWeight = FontWeight.Bold)
                            Text("MAGAZINE", style = MaterialTheme.typography.labelSmall, color = Color(0xFFB91C1C))
                        }
                    },
                    actions = {
                        IconButton(onClick = onRefresh) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                )
            }
        }
    ) { padding ->
        if (state.loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            state.featured?.let { featured ->
                item {
                    Text("Featured Story", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    StoryCard(post = featured, prominent = true)
                }
            }

            items(state.sections) { section ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(section.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.postsBySection[section.id].orEmpty()) { post ->
                            Box(Modifier.fillParentMaxWidth(0.88f)) {
                                StoryCard(post = post)
                            }
                        }
                    }
                    Button(onClick = { onLoadMoreSection(section) }) {
                        Text("Load more ${section.label}")
                    }
                }
            }

            item {
                Text("More News", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(state.latest) { post ->
                StoryCard(post = post)
            }
            item {
                Button(onClick = onLoadMoreLatest, modifier = Modifier.fillMaxWidth()) {
                    Text("Load More News")
                }
            }

            state.error?.let { message ->
                item {
                    Text(message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun StoryCard(post: NewsPost, prominent: Boolean = false) {
    val context = LocalContext.current
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(post.sourceUrl)))
            }
    ) {
        Column {
            Image(
                painter = rememberAsyncImagePainter(post.imageUrl),
                contentDescription = post.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (prominent) 240.dp else 180.dp)
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(post.dateText, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Spacer(Modifier.size(4.dp))
                Text(post.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.size(6.dp))
                Text(post.excerpt, style = MaterialTheme.typography.bodyMedium)
                if (post.tags.isNotEmpty()) {
                    Spacer(Modifier.size(8.dp))
                    Text(
                        post.tags.take(3).joinToString("  •  "),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFB91C1C)
                    )
                }
            }
        }
    }
}
