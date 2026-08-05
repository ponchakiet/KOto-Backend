package poncha.kiet.postservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import poncha.kiet.postservice.dto.request.PostRequestDTO;
import poncha.kiet.postservice.dto.response.Response;
import poncha.kiet.postservice.service.PostService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
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
