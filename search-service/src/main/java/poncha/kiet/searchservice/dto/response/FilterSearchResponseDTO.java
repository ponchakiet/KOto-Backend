package poncha.kiet.searchservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class FilterSearchResponseDTO {
    private String label;
    private String value;
    private List<FilterSearchItem> items;

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class FilterSearchItem {
        public String label;
        public Object value;
        public Long count;
    }
}
