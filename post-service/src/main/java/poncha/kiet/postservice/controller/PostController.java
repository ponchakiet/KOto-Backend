package poncha.kiet.postservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poncha.kiet.postservice.dto.request.PostRequestDTO;
import poncha.kiet.postservice.dto.response.PostResponseDTO;
import poncha.kiet.postservice.dto.response.Response;
import poncha.kiet.postservice.service.PostService;

import java.time.LocalDateTime;

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

    @PostMapping
    public ResponseEntity<Response<Void>> createPost(@RequestBody PostRequestDTO request) {
        postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Response.<Void>builder()
                .success(true)
                .message("Create post successfully")
                .timestamp(LocalDateTime.now())
                .build());
    }
}
