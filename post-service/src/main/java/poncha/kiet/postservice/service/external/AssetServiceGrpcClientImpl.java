package poncha.kiet.postservice.service.external;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import poncha.kiet.postservice.grpc.*;
import poncha.kiet.postservice.service.VehicleServiceGrpcClient;

@Service
@Slf4j
public class AssetServiceGrpcClientImpl implements VehicleServiceGrpcClient {
    @GrpcClient("vehicleServiceGrpc")
    private VehicleToPostServiceGrpc.VehicleToPostServiceBlockingStub vehicleToPostServiceBlockingStub;

    @CircuitBreaker(name = "vehicleServiceCB")
    @Retry(name = "vehicleServiceRetry")
    @Override
    public VehicleResponse getVehicleDetail(VehicleRequest request) {
        return vehicleToPostServiceBlockingStub.getVehicleDetail(request);
    }

    @Override
    public CheckExistedResponse checkExisted(VehicleRequest request) {
        return vehicleToPostServiceBlockingStub.checkExisted(request);
    }

    @CircuitBreaker(name = "vehicleServiceCB", fallbackMethod = "fallbackGetPoster")
    @Retry(name = "vehicleServiceRetry")
    @Override
    public Content getPoster(VehicleRequest request) {
        return vehicleToPostServiceBlockingStub.getPoster(request);
    }

    public Content fallbackGetPoster(VehicleRequest request, Throwable throwable) {
        log.error("Fallback triggered for {} due to {}", request.getId(), throwable.toString());
        return Content.newBuilder()
                .build();
    }
}
