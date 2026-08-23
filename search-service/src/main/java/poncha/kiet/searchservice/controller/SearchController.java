package poncha.kiet.searchservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import poncha.kiet.searchservice.dto.response.PageResponse;
import poncha.kiet.searchservice.dto.response.Response;
import poncha.kiet.searchservice.entity.PostDocument;
import poncha.kiet.searchservice.service.AsyncService;
import poncha.kiet.searchservice.service.SearchService;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/search")
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;
    private final AsyncService asyncService;

    @GetMapping
    public ResponseEntity<Response<PageResponse<PostDocument>>> search(
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam Map<String, String> params
    ) {
        return ResponseEntity.ok(Response.<PageResponse<PostDocument>>builder()
                .success(true)
                .message("Search successfully")
                .data(PageResponse.build(searchService.searchPost(keyword, PageRequest.of(page, size), params)))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/filters")
    public ResponseEntity<Response<?>> getFilters(
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") String size,
            @RequestParam Map<String, String> params
    ) {
        return ResponseEntity.ok(Response.builder()
                .success(true)
                .message("Get filters successfully")
                .data(searchService.getAggregation(keyword, params))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/async")
    public ResponseEntity<Response<?>> asyncData() {
        asyncService.migrateData();
        return ResponseEntity.ok(Response.builder()
                .success(true)
                .message("Async data successfully")
                .timestamp(LocalDateTime.now())
                .build());
    }

}
