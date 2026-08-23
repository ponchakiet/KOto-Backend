package poncha.kiet.searchservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;
import poncha.kiet.searchservice.entity.PostDocument;
import poncha.kiet.searchservice.grpc.*;
import poncha.kiet.searchservice.mapper.PostMapper;
import poncha.kiet.searchservice.service.AsyncService;

@Service
@Slf4j
@RequiredArgsConstructor
public class AsyncServiceImpl implements AsyncService {
    private final ElasticsearchOperations elasticsearchOperations;
    private final PostMapper postMapper;

    @GrpcClient("postServiceGrpc")
    private PostToSearchServiceGrpc.PostToSearchServiceBlockingStub postServiceBlockingStub;

    @Override
    public void migrateData() {
        IndexOperations indexOps = elasticsearchOperations.indexOps(PostDocument.class);
        if (!indexOps.exists()) {
            indexOps.create();
            indexOps.putMapping(indexOps.createMapping(PostDocument.class));
        }
        PostDetailResponseList responses = postServiceBlockingStub.getAllPost(Empty.newBuilder().build());

        for (var post : responses.getResponseList()) {
            PostDocument postDocument = postMapper.toDocument(post);
            elasticsearchOperations.save(postDocument);
            log.info("Create post - {}", post.getId());
        }
    }

    @Override
    public void createPost(String postId) {
        IndexCoordinates indexCoordinates = IndexCoordinates.of("post-index");
        IndexOperations indexOperations = elasticsearchOperations.indexOps(indexCoordinates);
        if (!indexOperations.exists()) {
            indexOperations.create();
            System.out.println("Index created: post-index");
        }

        PostDetailResponse responses = postServiceBlockingStub.getPostById(
                PostDetailRequest.newBuilder()
                        .setId(postId).build());

        PostDocument postDocument = postMapper.toDocument(responses);

        log.info("Create post - {}", postDocument.getId());
        elasticsearchOperations.save(postDocument);
    }

    @Override
    public void updatePost(String postId) {
        PostDetailResponse responses = postServiceBlockingStub.getPostById(
                PostDetailRequest.newBuilder()
                        .setId(postId)
                        .build());

        PostDocument postNewDocument = postMapper.toDocument(responses);
        elasticsearchOperations.save(postNewDocument);
    }

    @Override
    public void deletePost(String postId) {
        elasticsearchOperations.delete(String.valueOf(postId), IndexCoordinates.of("post-index"));
    }
}
