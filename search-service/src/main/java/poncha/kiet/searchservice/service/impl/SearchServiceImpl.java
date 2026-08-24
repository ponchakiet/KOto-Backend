package poncha.kiet.searchservice.service.impl;

import co.elastic.clients.elasticsearch._types.ScriptSource;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import poncha.kiet.searchservice.dto.response.FilterSearchResponseDTO;
import poncha.kiet.searchservice.entity.PostDocument;
import poncha.kiet.searchservice.entity.enums.PostStatus;
import poncha.kiet.searchservice.entity.enums.PostType;
import poncha.kiet.searchservice.mapper.PostMapper;
import poncha.kiet.searchservice.service.SearchService;
import poncha.kiet.searchservice.utils.FilterUtils;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static poncha.kiet.searchservice.utils.ValidationUtils.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchOperations elasticsearchRestTemplate;
    private final PostMapper postMapper;

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

    private void addOptionalAggregation(NativeQueryBuilder builder, String aggName, String paramValue,
                                        Function<String, Aggregation> aggregationSupplier) {
        if (paramValue != null && !paramValue.isEmpty()) {
            builder.withAggregation(aggName, aggregationSupplier.apply(paramValue));
        }
    }

    @Override
    public List<FilterSearchResponseDTO> getAggregation(String keyword, Map<String, String> params) {
        NativeQueryBuilder queryBuilder = this.extractQuery(keyword, PageRequest.of(0, 10), params);
        addOptionalAggregation(queryBuilder,
                FilterUtils.WARD,
                params.get(FilterUtils.PROVINCE),
                province -> Aggregation.of(a -> a
                        .filter(fa -> fa
                                .term(t -> t
                                        .field("vehicleDetail.province")
                                        .value(province)))
                        .aggregations("ward", agg -> agg
                                .terms(t -> t
                                        .field("vehicleDetail.ward")
                                        .size(20)))));
        queryBuilder.withAggregation(FilterUtils.PROPERTIES, Aggregation.of(a -> a
                .terms(t -> t
                        .script(s -> s
                                .source(new ScriptSource.Builder()
                                        .scriptString("""
                                                            if (params._source?.vehicleDetail?.properties != null) {
                                                                return new ArrayList(params._source.vehicleDetail.properties.keySet());
                                                            }
                                                            return [];
                                                """)
                                        .build()))
                        .size(50))
        ));

        NativeQuery query = queryBuilder.build();

        SearchHits<PostDocument> searchHits = elasticsearchOperations.search(query, PostDocument.class);

        List<org.springframework.data.elasticsearch.client.elc.Aggregation> aggregations = new ArrayList<>();

        if (searchHits.hasAggregations()) {
            ((List<ElasticsearchAggregation>) searchHits.getAggregations().aggregations())
                    .forEach(elsAgg -> aggregations.add(elsAgg.aggregation()));
        }

        List<FilterSearchResponseDTO> responses = new ArrayList<>();

        for (org.springframework.data.elasticsearch.client.elc.Aggregation agg : aggregations) {
            String aggName = agg.getName();
            Aggregate innerAgg = agg.getAggregate();
            List<FilterSearchResponseDTO.FilterSearchItem> filterResponseItems = new ArrayList<>();

            switch (innerAgg._kind()) {
                case Sterms:
                    StringTermsAggregate stringTermsAgg = (StringTermsAggregate) innerAgg._get();
                    List<StringTermsBucket> stringBuckets = (List<StringTermsBucket>) stringTermsAgg.buckets()._get();
                    if (aggName.equals(FilterUtils.PROPERTIES))
                        extractAggregationWithFetch(filterResponseItems, stringBuckets, keyword, params);
                    else
                        extractAggregation(aggName, filterResponseItems, stringBuckets);
                    break;
                case Filter:
                    FilterAggregate filterAggregate = (FilterAggregate) innerAgg._get();
                    Map<String, Aggregate> mpAgg = filterAggregate.aggregations();
                    for (var x : mpAgg.entrySet()) {
                        StringTermsAggregate filterTermAggregate = (StringTermsAggregate) x.getValue()._get();
                        List<StringTermsBucket> filterBucket = (List<StringTermsBucket>) filterTermAggregate.buckets()._get();

                        FilterSearchResponseDTO.FilterSearchItem item = FilterSearchResponseDTO.FilterSearchItem
                                .builder()
                                .label(aggName)
                                .build();
                        filterResponseItems.add(item);
                        extractSubValue(item, filterBucket);
                    }
                    break;
                case Range:
                    List<RangeBucket> rangeBuckets = (List<RangeBucket>) innerAgg.range().buckets()._get();
                    for (RangeBucket bucket : rangeBuckets) {
                        filterResponseItems.add(FilterSearchResponseDTO.FilterSearchItem
                                .builder()
                                .label(bucket.key())
                                .value(bucket.key())
                                .count(bucket.docCount())
                                .build());
                    }
                    break;
                default:
                    break;
            }
            responses.add(FilterSearchResponseDTO
                    .builder()
                    .label(aggName)
                    .value(FilterUtils.AGGS_VALUE.get(aggName))
                    .items(filterResponseItems)
                    .build());
        }

        return responses;
    }

    private void extractAggregation(String key, List<FilterSearchResponseDTO.FilterSearchItem> filterResponseItems, List<StringTermsBucket> stringBuckets) {
        for (StringTermsBucket bucket : stringBuckets) {
            FilterSearchResponseDTO.FilterSearchItem item = FilterSearchResponseDTO.FilterSearchItem
                    .builder()
                    .label(key)
                    .value(bucket.key().stringValue())
                    .count(bucket.docCount())
                    .build();
            if (!bucket.aggregations().isEmpty()) {
                StringTermsAggregate nameAgg = (StringTermsAggregate) bucket.aggregations().get("name")._get();
                item.setLabel(nameAgg.buckets().array().get(0).key().stringValue());
            }
            filterResponseItems.add(item);
        }
    }

    private void extractSubValue(FilterSearchResponseDTO.FilterSearchItem filterResponseItems,
                                 List<StringTermsBucket> stringBuckets) {
        Set<String> value = new HashSet<>();
        for (StringTermsBucket bucket : stringBuckets) {
            value.add(bucket.key().stringValue());
        }
        filterResponseItems.setValue(value);
    }

    private void extractAggregationWithFetch(List<FilterSearchResponseDTO.FilterSearchItem> filterResponseItems,
                                             List<StringTermsBucket> stringBuckets, String keyword, Map<String, String> params) {
        for (StringTermsBucket bucket : stringBuckets) {
            String propertyKey = bucket.key().stringValue();
            NativeQuery query = this.extractQuery(keyword, PageRequest.of(0, 10), params)
                    .withAggregation("values_for_key", Aggregation.of(a -> a
                            .terms(t -> t
                                    .script(s -> s
                                            .source(new ScriptSource.Builder()
                                                    .scriptString("""
                                                                    def props = params._source.vehicleDetail?.properties;
                                                                    if (props != null && props.containsKey('%s')) {
                                                                        return props['%s'];
                                                                    }
                                                                    return null;
                                                            """.formatted(propertyKey, propertyKey))
                                                    .build())
                                    )
                                    .size(50)
                            )
                    ))
                    .build();
            FilterSearchResponseDTO.FilterSearchItem item = FilterSearchResponseDTO.FilterSearchItem
                    .builder()
                    .label(propertyKey)
                    .build();
            filterResponseItems.add(item);

            SearchHits<PostDocument> searchHits = elasticsearchOperations.search(query, PostDocument.class);
            List<org.springframework.data.elasticsearch.client.elc.Aggregation> aggregations = new ArrayList<>();
            if (searchHits.hasAggregations()) {
                ((List<ElasticsearchAggregation>) searchHits.getAggregations().aggregations())
                        .forEach(elsAgg -> aggregations.add(elsAgg.aggregation()));
            }
            for (var x : aggregations) {
                StringTermsAggregate stringTermsAgg = (StringTermsAggregate) x.getAggregate()._get();
                List<StringTermsBucket> subBuckets = (List<StringTermsBucket>) stringTermsAgg.buckets()._get();
                extractSubValue(item, subBuckets);
            }
        }
    }

    private boolean isValidSearchField(Map<String, String> params, String keyword) {
        return params != null && !isNullOrEmpty(params.get(keyword));
    }

    private NativeQueryBuilder extractQuery(String keyword, Pageable pageable, Map<String, String> params) {
        String category = isValidSearchField(params, FilterUtils.CATEGORY) ? params.get(FilterUtils.CATEGORY) : null;
        String province = isValidSearchField(params, FilterUtils.PROVINCE) ? params.get(FilterUtils.PROVINCE) : null;
        String ward = isValidSearchField(params, FilterUtils.WARD) ? params.get(FilterUtils.WARD) : null;
        String type = isValidSearchField(params, FilterUtils.TYPE) ? params.get(FilterUtils.TYPE) : PostType.SELL.name();
        Integer minYear = isValidSearchField(params, FilterUtils.MIN_YEAR) ? Integer.parseInt(params.get(FilterUtils.MIN_YEAR)) : null;
        Integer maxYear = isValidSearchField(params, FilterUtils.MAX_YEAR) ? Integer.parseInt(params.get(FilterUtils.MAX_YEAR)) : null;
        Double minPrice = isValidSearchField(params, FilterUtils.MIN_PRICE) ? Double.parseDouble(params.get(FilterUtils.MIN_PRICE)) : null;
        Double maxPrice = isValidSearchField(params, FilterUtils.MAX_PRICE) ? Double.parseDouble(params.get(FilterUtils.MAX_PRICE)) : null;

        String sortBy = isValidSearchField(params, FilterUtils.ORDER_BY) ? params.get(FilterUtils.ORDER_BY) : null;
        String order = isValidSearchField(params, FilterUtils.ORDER) ? params.get(FilterUtils.ORDER) : null;

        Map<String, String> restParams = params.entrySet().stream()
                .filter(e -> !FilterUtils.KNOW_KEY.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        var boolQueryBuilder = QueryBuilders.bool();

        if (keyword != null && !keyword.isEmpty()) {
            var shouldQuery = QueryBuilders.bool();
            boolQueryBuilder
                    .should(QueryBuilders.multiMatch(m -> m
                            .fields("title",
                                    "description",
                                    "vehicleDetail.province",
                                    "vehicleDetail.ward",
                                    "vehicleDetail.address",
                                    "vehicleDocument.categoryDocument.name")
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

        if (category != null) {
            boolQueryBuilder
                    .must(builder -> builder
                            .term(t -> t
                                    .field("vehicleDetail.category.id.keyword")
                                    .value(category)));
        }

        NativeQueryBuilder queryBuilder = new NativeQueryBuilder()
                .withQuery(boolQueryBuilder.build()._toQuery());

        queryBuilder.withPageable(pageable);

        queryBuilder.withFilter(f -> f.bool(b -> {
            extractedTermsFilter(PostStatus.SHOW.name(), "status", b);
            extractedTermsFilter(province, "vehicleDetail.province", b);
            extractedTermsFilter(ward, "vehicleDetail.ward", b);
            extractedTermsFilter(type, "type", b);
            extractedRange(minPrice, maxPrice, "price", b);
            extractedRange(minYear, maxYear, "manufactureYear", b);

            if (restParams != null) {
                for (var x : restParams.entrySet()) {
                    extractedTermsFilter(x.getValue(), String.format("vehicleDetail.properties.%s", x.getKey()), b);
                }
            }

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
                                .value(value)));
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
            return new Query.Builder().bool(orBool.build());
        });
    }

    private void extractedRange(Number min, Number max, String field, BoolQuery.Builder bool) {
        if (min != null || max != null) {
            bool.must(m -> m.range(r -> {
                r.term(t -> {
                    t.field(field);
                    if (min != null) {
                        t.gte(JsonData.of(min).toString());
                    }
                    if (max != null) {
                        t.lte(JsonData.of(max).toString());
                    }
                    return t;
                });
                return r;
            }));
        }
    }
}
