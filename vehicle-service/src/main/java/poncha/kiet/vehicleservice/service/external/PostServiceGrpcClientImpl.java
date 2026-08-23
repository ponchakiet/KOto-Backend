package poncha.kiet.vehicleservice.service.external;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import poncha.kiet.vehicleservice.grpc.PostCheckAttachRequest;
import poncha.kiet.vehicleservice.grpc.PostCheckAttachResponse;
import poncha.kiet.vehicleservice.grpc.PostToVehicleServiceGrpc;
import poncha.kiet.vehicleservice.service.PostServiceGrpcClient;

@Service
@Slf4j
public class PostServiceGrpcClientImpl implements PostServiceGrpcClient {
    @GrpcClient("postServiceGrpc")
    private PostToVehicleServiceGrpc.PostToVehicleServiceBlockingStub postServiceBlockingStub;

    @Override
    @Retry(name = "postServiceRetry")
    @CircuitBreaker(name = "postServiceCB", fallbackMethod = "fallbackABC")
    public PostCheckAttachResponse checkAttachedPost(PostCheckAttachRequest request) {
        log.error("Retry...");

        return postServiceBlockingStub.checkAttachedPost(request);
    }

    // fallback triggered by Resilience4j
    public PostCheckAttachResponse fallbackABC(PostCheckAttachRequest request, Throwable t) {
        log.error("Fallback triggered for {} due to {}", request.getId(), t.toString());
        return PostCheckAttachResponse.newBuilder().setIsAttached(false).build();
    }
}
