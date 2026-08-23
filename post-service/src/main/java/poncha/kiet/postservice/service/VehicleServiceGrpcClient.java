package poncha.kiet.postservice.service;

import poncha.kiet.postservice.grpc.CheckExistedResponse;
import poncha.kiet.postservice.grpc.Content;
import poncha.kiet.postservice.grpc.VehicleRequest;
import poncha.kiet.postservice.grpc.VehicleResponse;

public interface VehicleServiceGrpcClient {
    VehicleResponse getVehicleDetail(VehicleRequest request);

    CheckExistedResponse checkExisted(VehicleRequest request);

    Content getPoster(VehicleRequest request);
}
