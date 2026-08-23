package poncha.kiet.searchservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import poncha.kiet.searchservice.dto.response.FilterSearchResponseDTO;
import poncha.kiet.searchservice.entity.PostDocument;

import java.util.List;
import java.util.Map;

public interface SearchService {
    Page<PostDocument> searchPost(String keyword, Pageable pageable, Map<String, String> params);

    List<FilterSearchResponseDTO> getAggregation(String keyword, Map<String, String> params);
}
