package com.example.pelarikalcer.ui.screens.feed

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ModeComment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.pelarikalcer.data.remote.CommentItem
import com.example.pelarikalcer.data.remote.PostItem
import com.example.pelarikalcer.data.remote.SupabaseClient
import com.example.pelarikalcer.ui.theme.*
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    currentUsername: String,
    currentUserEmail: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var posts by remember { mutableStateOf<List<PostItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showCreateDialog by remember { mutableStateOf(false) }

    // Selected post for commenting & Full Image Preview
    var selectedPostForComments by remember { mutableStateOf<PostItem?>(null) }
    var comments by remember { mutableStateOf<List<CommentItem>>(emptyList()) }
    var newCommentText by remember { mutableStateOf("") }
    var isSendingComment by remember { mutableStateOf(false) }
    var fullImagePreviewUrl by remember { mutableStateOf<String?>(null) }

    // Refresh posts from Supabase
    val refreshPosts: () -> Unit = {
        scope.launch {
            isLoading = true
            posts = SupabaseClient.fetchPosts(currentUserEmail)
            isLoading = false
        }
    }

    LaunchedEffect(currentUserEmail) {
        refreshPosts()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavy)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(CardSurface, DeepNavy)))
                    .padding(horizontal = 24.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PelariFeed",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeonGreen
                        )
                        Text(
                            text = "Linimasa & cerita sesi lari komunitas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }

                    FloatingActionButton(
                        onClick = { showCreateDialog = true },
                        containerColor = NeonGreen,
                        contentColor = DeepNavy,
                        modifier = Modifier.size(46.dp),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Buat Post")
                    }
                }
            }

            // Feed Posts List
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NeonGreen)
                }
            } else if (posts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.RssFeed, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Belum ada postingan.", color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text("Jadilah yang pertama membagikan hasil larimu!", color = TextMuted, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(posts, key = { it.postId }) { post ->
                        PostCard(
                            post = post,
                            onLikeToggle = {
                                scope.launch {
                                    val newIsLiked = !post.isLikedByMe
                                    val newCount = if (newIsLiked) post.likeCount + 1 else (post.likeCount - 1).coerceAtLeast(0)
                                    posts = posts.map { p ->
                                        if (p.postId == post.postId) p.copy(isLikedByMe = newIsLiked, likeCount = newCount) else p
                                    }
                                    SupabaseClient.toggleLike(post.postId, currentUserEmail, post.isLikedByMe)
                                }
                            },
                            onCommentClick = {
                                selectedPostForComments = post
                                scope.launch {
                                    comments = SupabaseClient.fetchComments(post.postId)
                                }
                            },
                            onImageClick = { url ->
                                fullImagePreviewUrl = url
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }

        // Create Post Modal Dialog
        if (showCreateDialog) {
            CreatePostModal(
                username = currentUsername,
                userEmail = currentUserEmail,
                onDismiss = { showCreateDialog = false },
                onPostCreated = {
                    showCreateDialog = false
                    refreshPosts()
                }
            )
        }

        // Comments Bottom Sheet Modal
        selectedPostForComments?.let { post ->
            ModalBottomSheet(
                onDismissRequest = { selectedPostForComments = null },
                containerColor = DarkSurface,
                scrimColor = Color.Black.copy(alpha = 0.6f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        "Komentar (${comments.size})",
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (comments.isEmpty()) {
                        Text("Belum ada komentar. Tulis komentar pertama!", color = TextMuted, fontSize = 13.sp, modifier = Modifier.padding(vertical = 16.dp))
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 300.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(comments) { c ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(NeonGreen.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            (c.username.firstOrNull()?.uppercaseChar() ?: '?').toString(),
                                            fontWeight = FontWeight.Bold,
                                            color = NeonGreen,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(c.username, fontWeight = FontWeight.Bold, color = NeonGreen, fontSize = 13.sp)
                                        Text(c.text, color = TextPrimary, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Write Comment Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newCommentText,
                            onValueChange = { newCommentText = it },
                            placeholder = { Text("Tulis komentar...", color = TextMuted, fontSize = 13.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = TextMuted,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (newCommentText.isNotBlank() && !isSendingComment) {
                                    isSendingComment = true
                                    val txt = newCommentText
                                    newCommentText = ""
                                    scope.launch {
                                        SupabaseClient.addComment(post.postId, currentUserEmail, currentUsername, txt)
                                        comments = SupabaseClient.fetchComments(post.postId)
                                        posts = posts.map { p ->
                                            if (p.postId == post.postId) p.copy(commentCount = p.commentCount + 1) else p
                                        }
                                        isSendingComment = false
                                    }
                                }
                            },
                            enabled = newCommentText.isNotBlank()
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Kirim", tint = NeonGreen)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Full Image Preview Modal
        fullImagePreviewUrl?.let { url ->
            FullImagePreviewModal(
                imageUrl = url,
                onDismiss = { fullImagePreviewUrl = null }
            )
        }
    }
}

@Composable
fun FullImagePreviewModal(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.94f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            val bitmap = remember(imageUrl) { decodeBase64ToBitmap(imageUrl) }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Full Preview",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Full Preview",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(44.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
            }
        }
    }
}

@Composable
fun PostCard(
    post: PostItem,
    onLikeToggle: () -> Unit,
    onCommentClick: () -> Unit,
    onImageClick: (String) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // User Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            Brush.linearGradient(listOf(NeonGreen.copy(alpha = 0.3f), AccentOrange.copy(alpha = 0.3f))),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        (post.username.firstOrNull()?.uppercaseChar() ?: '?').toString(),
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(post.username, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                    Text("PelariKalcer Member", color = TextMuted, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Caption
            if (post.caption.isNotBlank()) {
                Text(post.caption, color = TextPrimary, fontSize = 14.sp, lineHeight = 20.sp)
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Linked Run Stats Pill (if available)
            if (post.distanceKm > 0) {
                Surface(
                    color = DeepNavy,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DirectionsRun, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "${String.format("%.2f", post.distanceKm)} km",
                                fontWeight = FontWeight.ExtraBold,
                                color = NeonGreen,
                                fontSize = 14.sp
                            )
                        }

                        if (post.paceMinPerKm > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Speed, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    String.format("%d'%02d\"/km", post.paceMinPerKm.toInt(), ((post.paceMinPerKm % 1) * 60).toInt()),
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (post.durationSeconds > 0) {
                            val m = (post.durationSeconds % 3600) / 60
                            val s = post.durationSeconds % 60
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = StreakOrange, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    String.format("%02d:%02d", m, s),
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Post Image (Base64 data or URL) - Click to Open Full Image Preview
            if (post.imageUrl.isNotBlank()) {
                val bitmap = remember(post.imageUrl) { decodeBase64ToBitmap(post.imageUrl) }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black)
                        .clickable { onImageClick(post.imageUrl) }
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Foto Post",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        AsyncImage(
                            model = post.imageUrl,
                            contentDescription = "Foto Post",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    // Hint Icon for full preview
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            .padding(6.dp)
                    ) {
                        Icon(Icons.Default.Fullscreen, contentDescription = "Perbesar", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Like & Comment Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onLikeToggle) {
                    Icon(
                        imageVector = if (post.isLikedByMe) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.isLikedByMe) DangerRed else TextMuted
                    )
                }
                Text("${post.likeCount}", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.width(20.dp))

                IconButton(onClick = onCommentClick) {
                    Icon(
                        imageVector = Icons.Outlined.ModeComment,
                        contentDescription = "Komentar",
                        tint = TextMuted
                    )
                }
                Text("${post.commentCount}", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CreatePostModal(
    username: String,
    userEmail: String,
    onDismiss: () -> Unit,
    onPostCreated: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var captionText by remember { mutableStateOf("") }
    var selectedImageBase64 by remember { mutableStateOf<String?>(null) }
    var isPosting by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val base64 = uriToBase64(context, it)
            selectedImageBase64 = base64
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text("Buat Postingan Baru 🏃", fontWeight = FontWeight.Bold, color = NeonGreen)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = captionText,
                    onValueChange = { captionText = it },
                    placeholder = { Text("Tulis cerita larimu hari ini...", color = TextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = TextMuted,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Photo preview or picker button
                if (selectedImageBase64 != null) {
                    val bmp = remember(selectedImageBase64) { decodeBase64ToBitmap(selectedImageBase64!!) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        bmp?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = "Preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        IconButton(
                            onClick = { selectedImageBase64 = null },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Hapus Foto", tint = Color.White)
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGreen),
                        border = BorderStroke(1.dp, NeonGreen)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pilih Foto Galeri")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (captionText.isNotBlank() || selectedImageBase64 != null) {
                        isPosting = true
                        scope.launch {
                            val newPost = PostItem(
                                userEmail = userEmail,
                                username = username,
                                imageUrl = selectedImageBase64 ?: "",
                                caption = captionText
                            )
                            val ok = SupabaseClient.createPost(newPost)
                            if (ok) {
                                Toast.makeText(context, "Berhasil memposting!", Toast.LENGTH_SHORT).show()
                                onPostCreated()
                            } else {
                                Toast.makeText(context, "Gagal memposting. Coba lagi.", Toast.LENGTH_SHORT).show()
                            }
                            isPosting = false
                        }
                    }
                },
                enabled = !isPosting && (captionText.isNotBlank() || selectedImageBase64 != null),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = DeepNavy)
            ) {
                if (isPosting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = DeepNavy, strokeWidth = 2.dp)
                } else {
                    Text("Posting", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextMuted)
            }
        }
    )
}

fun uriToBase64(context: android.content.Context, uri: Uri): String? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val outputStream = ByteArrayOutputStream()
        // Scale down to max 800px width/height to keep payload size optimal for fast sync
        val scaled = scaleBitmap(bitmap, 800)
        scaled.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
        val bytes = outputStream.toByteArray()
        "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun scaleBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    if (width <= maxDimension && height <= maxDimension) return bitmap

    val ratio = width.toFloat() / height.toFloat()
    val newWidth: Int
    val newHeight: Int
    if (width > height) {
        newWidth = maxDimension
        newHeight = (maxDimension / ratio).toInt()
    } else {
        newHeight = maxDimension
        newWidth = (maxDimension * ratio).toInt()
    }
    return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
}

fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
    return try {
        val cleanStr = if (base64Str.contains(",")) base64Str.substringAfter(",") else base64Str
        val decodedBytes = Base64.decode(cleanStr, Base64.NO_WRAP)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        null
    }
}
