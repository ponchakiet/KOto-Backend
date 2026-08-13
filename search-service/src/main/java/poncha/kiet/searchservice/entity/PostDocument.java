package poncha.kiet.searchservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import poncha.kiet.searchservice.entity.enums.PostStatus;
import poncha.kiet.searchservice.entity.enums.PostType;

import java.time.LocalDateTime;

@Document(indexName = "post_index")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostDocument {
    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime lastUpdated;

    @Field(type = FieldType.Text)
    private String userId;

    @Field(type = FieldType.Double)
    private double price;

    @Field(type = FieldType.Keyword)
    private PostType type;

    @Field(type = FieldType.Keyword)
    private PostStatus status;

    @Field(type = FieldType.Object)
    private VehicleDocument vehicleDetail;

    @Field(type = FieldType.Object)
    private UserInfoDocument userInfoDocument;

    @Data
    @NoArgsConstructor
    @Builder
    @AllArgsConstructor
    public static class UserInfoDocument {
        private String id;
        private String firstName;
        private String lastName;
        private String avt;
    }
}
