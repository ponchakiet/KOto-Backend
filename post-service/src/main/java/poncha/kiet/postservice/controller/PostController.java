package poncha.kiet.postservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poncha.kiet.postservice.dto.request.PostRequestDTO;
import poncha.kiet.postservice.dto.response.*;
import poncha.kiet.postservice.service.PostService;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @GetMapping("/public/posts/{postId}")
    public ResponseEntity<Response<PostResponseDTO>> getPost(@PathVariable("postId") String postId) {
        return ResponseEntity.ok(Response.<PostResponseDTO>builder()
                .success(true)
                .message("Get post successfully")
                .data(postService.findPostById(postId))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/public/posts")
    public ResponseEntity<Response<PageResponse<PostResponseDTO>>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String type
    ) {
        return ResponseEntity.ok(Response.<PageResponse<PostResponseDTO>>builder()
                .success(true)
                .message("Get posts successfully")
                .data(PageResponse.build(postService.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")), type)))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/public/posts/status")
    public ResponseEntity<Response<PageResponse<PostOverviewResponseDTO>>> getPostsByStatus(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String status,
            @RequestParam(defaultValue = "") String kw
    ) {
        String userId = "ssss";
        return ResponseEntity.ok(Response.<PageResponse<PostOverviewResponseDTO>>builder()
                .success(true)
                .message("Get posts by status successfully")
                .data(PageResponse.build(postService.findAllByStatus(status, kw, userId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PutMapping("/posts/{postId}")
    public ResponseEntity<Response<Map<String, String>>> updatePost(@PathVariable("postId") String postId,
                                                                    @RequestBody PostRequestDTO postRequestDTO) throws AccessDeniedException {
        String userId = "ssss";
        return ResponseEntity.ok(Response.<Map<String, String>>builder()
                .success(true)
                .message("Update post successfully")
                .data(postService.updatePost(postId, userId, postRequestDTO))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Response<Void>> deletePost(@PathVariable("postId") String postId) {
        String userId = "ssss";
        postService.inActivePost(userId, postId);
        return ResponseEntity.ok(Response.<Void>builder()
                .success(true)
                .message("Delete post successfully")
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/public/posts/history/{vehicleId}")
    public ResponseEntity<Response<PageResponse<PostHistoryResponseDTO>>> getHistoryPost(
            @PathVariable("vehicleId") String vehicleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String userId = "ssss";
        return ResponseEntity.ok(Response.<PageResponse<PostHistoryResponseDTO>>builder()
                .success(true)
                .message("Get history post successfully")
                .data(PageResponse.build(postService.findHistoryPost(vehicleId, userId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/public/posts/count-status")
    public ResponseEntity<Response<List<StatusCountResponseDTO>>> getSummaryPostStatus() {
        String userId = "ssss";
        return ResponseEntity.ok(Response.<List<StatusCountResponseDTO>>builder()
                .success(true)
                .message("Get summary post status successfully")
                .data(postService.countStatusPost(userId))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping("/posts")
    public ResponseEntity<Response<Void>> createPost(@RequestBody PostRequestDTO request) {
        postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Response.<Void>builder()
                .success(true)
                .message("Create post successfully")
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<Response<Void>> likePost(
            @PathVariable("postId") String postId,
            @RequestParam("userId") String userId) {
        postService.likePost(userId, postId);
        return ResponseEntity.ok(Response.<Void>builder()
                .success(true)
                .message("Like post successfully")
                .timestamp(LocalDateTime.now())
                .build());
    }

    @DeleteMapping("/posts/{postId}/unlike")
    public ResponseEntity<Response<Void>> unlikePost(
            @PathVariable("postId") String postId,
            @RequestParam("userId") String userId) {
        postService.unlikePost(userId, postId);
        return ResponseEntity.ok(Response.<Void>builder()
                .success(true)
                .message("Unlike post successfully")
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<Response<PageResponse<CommentResponseDTO>>> getComments(
            @PathVariable("postId") String postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(Response.<PageResponse<CommentResponseDTO>>builder()
                .success(true)
                .message("Get comments successfully")
                .data(PageResponse.build(postService.findCommentsByPostId(postId, PageRequest.of(page, size))))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<Response<Map<String, String>>> createComment(
            @PathVariable("postId") String postId,
            @RequestBody Map<String, String> params) {
        return ResponseEntity.ok(Response.<Map<String, String>>builder()
                .success(true)
                .message("Get comments successfully")
                .data(postService.createComment(postId, params))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @DeleteMapping("/posts/{postId}/comments")
    public ResponseEntity<Response<Void>> deleteComment(
            @PathVariable("postId") String postId,
            @RequestBody Map<String, String> params) {
        postService.deleteComment(postId, params);
        return ResponseEntity.ok(Response.<Void>builder()
                .success(true)
                .message("Get comments successfully")
                .timestamp(LocalDateTime.now())
                .build());
    }
}
