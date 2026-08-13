package poncha.kiet.searchservice.service.impl;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;
import poncha.kiet.searchservice.dto.response.FilterSearchResponseDTO;
import poncha.kiet.searchservice.entity.PostDocument;
import poncha.kiet.searchservice.entity.enums.PostStatus;
import poncha.kiet.searchservice.grpc.Empty;
import poncha.kiet.searchservice.grpc.PostDetailResponseList;
import poncha.kiet.searchservice.grpc.PostToSearchServiceGrpc;
import poncha.kiet.searchservice.mapper.PostMapper;
import poncha.kiet.searchservice.service.SearchService;
import poncha.kiet.searchservice.utils.FilterUtils;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static poncha.kiet.searchservice.utils.ValidationUtils.*;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchOperations elasticsearchRestTemplate;
    private final PostMapper postMapper;
    @GrpcClient("postServiceGrpc")
    private PostToSearchServiceGrpc.PostToSearchServiceBlockingStub postServiceBlockingStub;

    @Override
    public Page<PostDocument> searchPost(String keyword, Pageable pageable, Map<String, String> params) {
        NativeQueryBuilder queryBuilder = this.extractQuery(keyword, pageable, params);
        SearchHits<PostDocument> searchHits = elasticsearchOperations
                .search(queryBuilder.build(), PostDocument.class);

        var responses = searchHits.getSearchHits()
                .stream()
                .map(SearchHit::getContent)
                .toList();

        Long totalElements = elasticsearchOperations.count(queryBuilder.build(), PostDocument.class);

        return new PageImpl<>(responses, pageable, totalElements);
    }

    @Override
    public List<FilterSearchResponseDTO> getAggregation(String keyword) {
        return null;
    }

    @Override
    public void migrateData() {
        IndexCoordinates indexCoordinates = IndexCoordinates.of("post_index");

        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(indexCoordinates);
        if (!indexOperations.exists()) {
            indexOperations.create();
            System.out.println("Index created: post_index");
        }

        PostDetailResponseList responses = postServiceBlockingStub.getAllPost(Empty.newBuilder().build());
    }

    private boolean isValidSearchField(Map<String, String> params, String keyword) {
        return params != null && !isNullOrEmpty(params.get(keyword));
    }

    private NativeQueryBuilder extractQuery(String keyword, Pageable pageable, Map<String, String> params) {
        String category = isValidSearchField(params, FilterUtils.CATEGORY) ? params.get(FilterUtils.CATEGORY) : null;
        String province = isValidSearchField(params, FilterUtils.PROVINCE) ? params.get(FilterUtils.PROVINCE) : null;
        String ward = isValidSearchField(params, FilterUtils.WARD) ? params.get(FilterUtils.WARD) : null;
        Integer minYear = isValidSearchField(params, FilterUtils.MIN_YEAR) ? Integer.parseInt(params.get(FilterUtils.MIN_YEAR)) : null;
        Integer maxYear = isValidSearchField(params, FilterUtils.MAX_YEAR) ? Integer.parseInt(params.get(FilterUtils.MAX_YEAR)) : null;
        Double minPrice = isValidSearchField(params, FilterUtils.MIN_PRICE) ? Double.parseDouble(params.get(FilterUtils.MIN_PRICE)) : null;
        Double maxPrice = isValidSearchField(params, FilterUtils.MAX_PRICE) ? Double.parseDouble(params.get(FilterUtils.MAX_PRICE)) : null;

        String sortBy = params != null && !isNullOrEmpty(params.get("sortBy")) ? params.get("sortBy") : null;
        String order = params != null && !isNullOrEmpty(params.get("order")) ? params.get("order") : null;

        var boolQueryBuilder = QueryBuilders.bool();
        boolQueryBuilder
                .must(builder -> builder
                        .term(t -> t
                                .field("status.keyword")
                                .value(PostStatus.SHOW.name())));

        if (keyword != null && !keyword.isEmpty()) {
            var shouldQuery = QueryBuilders.bool();
            boolQueryBuilder
                    .should(QueryBuilders.multiMatch(m -> m
                            .fields("title",
                                    "description",
                                    "vehicleDocument.province",
                                    "vehicleDocument.ward",
                                    "vehicleDocument.address",
                                    "vehicleDocument.categoryDocument.name",
                                    "sectionDocument.contentDocumentList.name")
                            .query(keyword)
                            .fuzziness("AUTO")
                            .boost(1.2f)))
                    .should(QueryBuilders.matchPhrasePrefix(m -> m
                            .field("title")
                            .query(keyword)
                            .maxExpansions(20)
                            .slop(2)))
                    .minimumShouldMatch("1");

            boolQueryBuilder.must(shouldQuery.build()._toQuery());
        }

        NativeQueryBuilder queryBuilder = new NativeQueryBuilder()
                .withQuery(boolQueryBuilder.build()._toQuery());

        queryBuilder.withPageable(pageable);

        queryBuilder.withFilter(f -> f.bool(b -> {
            extractedTermsFilter(category, "category", b);
            extractedRange(minPrice, maxPrice, "price", b);
            extractedRange(minYear, maxYear, "manufactureYear", b);
            return b;
        }));

        if (sortBy != null) {
            SortOrder sortOrder = order != null && order.equals("desc") ? SortOrder.Desc : SortOrder.Asc;
            queryBuilder.withSort(s -> switch (sortBy) {
                case "newest" -> s.field(f -> f.field("createdAt").order(SortOrder.Desc));
                case "price" -> s.field(f -> f.field("price").order(sortOrder));
                default -> s.score(v -> v.order(SortOrder.Desc));
            });
        }

        return queryBuilder;
    }

    public static void extractedTermsFilter(String fieldValues, String field, BoolQuery.Builder b) {
        if (fieldValues == null || fieldValues.isEmpty()) {
            return;
        }

        String[] valuesArray = fieldValues.split(",");
        b.must(m -> {
            BoolQuery.Builder innerBool = new BoolQuery.Builder();
            for (String value : valuesArray) {
                innerBool.should(s -> s
                        .term(t -> t
                                .field(field)
                                .value(value)
                                .caseInsensitive(true)));
            }
            return new Query.Builder().bool(innerBool.build());
        });
    }

    private void orExtractedRange(String durationRanges, String field, BoolQuery.Builder bool) {
        if (durationRanges == null || durationRanges.isBlank()) return;

        String[] ranges = durationRanges.split(",");

        bool.must(m -> {
            BoolQuery.Builder orBool = new BoolQuery.Builder();
            for (String range : ranges) {
                Pattern pattern = Pattern.compile("^(\\d+)(?:\\+|-(\\d+))?$");
                Matcher matcher = pattern.matcher(range.trim());

                if (matcher.matches()) {
                    Double from = matcher.group(1) != null ? Double.parseDouble(matcher.group(1)) * 3600 : null;
                    Double to = matcher.group(2) != null ? Double.parseDouble(matcher.group(2)) * 3600 : null;

                    orBool.should(s -> s.range(r -> {
                        r.term(t -> t.field(field).gte(from != null ? from.toString() : null).lte(to != null ? to.toString() : null));
                        return r;
                    }));
                }
            }
            return new  Query.Builder().bool(orBool.build());
        });
    }

    private void extractedRange(Number min, Number max, String field, BoolQuery.Builder bool) {
        if (min != null || max != null) {
            bool.must(m -> m
                    .range(r -> r.term(t -> t.field(field)
                            .gte(min != null ? min.toString() : null)
                            .lte(max != null ? max.toString() : null))
                    )
            );
        }
    }
}
