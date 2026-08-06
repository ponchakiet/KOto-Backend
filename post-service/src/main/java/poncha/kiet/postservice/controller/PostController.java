package poncha.kiet.postservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poncha.kiet.postservice.dto.request.PostRequestDTO;
import poncha.kiet.postservice.dto.response.CommentResponseDTO;
import poncha.kiet.postservice.dto.response.PageResponse;
import poncha.kiet.postservice.dto.response.PostResponseDTO;
import poncha.kiet.postservice.dto.response.Response;
import poncha.kiet.postservice.service.PostService;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @GetMapping("/{postId}")
    public ResponseEntity<Response<PostResponseDTO>> getPost(@PathVariable("postId") String postId) {
        return ResponseEntity.ok(Response.<PostResponseDTO>builder()
                .success(true)
                .message("Get post successfully")
                .data(postService.findPostById(postId))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping
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

    @PostMapping
    public ResponseEntity<Response<Void>> createPost(@RequestBody PostRequestDTO request) {
        postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Response.<Void>builder()
                .success(true)
                .message("Create post successfully")
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping("/{postId}/like")
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

    @DeleteMapping("/{postId}/unlike")
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

    @GetMapping("/{postId}/comments")
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

    @PostMapping("/{postId}/comments")
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

    @DeleteMapping("/{postId}/comments")
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
