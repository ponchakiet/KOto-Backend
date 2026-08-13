package poncha.kiet.searchservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import poncha.kiet.searchservice.entity.enums.VehicleType;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDocument {
    @Id
    private String id;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Keyword)
    private String province;

    @Field(type = FieldType.Keyword)
    private String ward;

    @Field(type = FieldType.Keyword)
    private String address;

    @Field(type = FieldType.Integer)
    private int manufactureYear;

    @Field(type = FieldType.Double)
    private double price;

    @Field(type = FieldType.Flattened)
    private Map<String, String> properties;

    @Field(type = FieldType.Text)
    private String[] otherInfo;

    @Field(type = FieldType.Text)
    private String projectId;

    @Field(type = FieldType.Text)
    private String userId;

    @Field(type = FieldType.Object)
    private CategoryDocument category;

    @Field(type = FieldType.Keyword)
    private VehicleType type;

    @Field(type = FieldType.Object)
    private MediaDocument media;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MediaDocument {
        private String id;
        private String url;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryDocument {
        private String id;
        private String name;
    }
}
