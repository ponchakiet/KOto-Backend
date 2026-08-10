package poncha.kiet.vehicleservice.service.Impl;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import poncha.kiet.vehicleservice.entity.ContentType;
import poncha.kiet.vehicleservice.entity.Image;
import poncha.kiet.vehicleservice.entity.Vehicle;
import poncha.kiet.vehicleservice.entity.Video;
import poncha.kiet.vehicleservice.grpc.*;
import poncha.kiet.vehicleservice.repository.VehicleRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@GrpcService
@RequiredArgsConstructor
public class VehicleGrpcServiceImpl extends VehicleToPostServiceGrpc.VehicleToPostServiceImplBase {
    private final VehicleRepository vehicleRepository;

    @Override
    public void getPoster(VehicleRequest request, StreamObserver<Content> responseObserver) {
        Optional<Vehicle> optionalVehicle = vehicleRepository.findById(request.getId());
        if (optionalVehicle.isPresent())
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("Vehicle not found with ID: " + request.getId())
                            .asRuntimeException());

        if (!optionalVehicle.get().getUserId().equals(request.getUserId()))
            responseObserver.onError(
                    Status.PERMISSION_DENIED
                            .withDescription("Don't have permission for this resource")
                            .asRuntimeException());

        Vehicle vehicle = optionalVehicle.get();
        if (vehicle.getContents() == null)
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Don't have image " + request.getId())
                            .asRuntimeException());

        Image image = vehicle.getPoster();
        Content content = Content.newBuilder()
                .setType(ContentType.IMAGE.name())
                .setUrl(image.getUrl())
                .setId(image.getId())
                .build();

        responseObserver.onNext(content);
        responseObserver.onCompleted();
    }

    @Override
    public void checkExisted(VehicleRequest request, StreamObserver<CheckExistedResponse> responseObserver) {
        Optional<Vehicle> optionalVehicle = vehicleRepository.findById(request.getId());
        if (!optionalVehicle.isPresent()) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("Vehicle not found with ID: " + request.getId())
                            .asRuntimeException());
        }

        if (!optionalVehicle.get().getUserId().equals(request.getUserId())) {
            responseObserver.onError(
                    Status.PERMISSION_DENIED
                            .withDescription("Don't have permission for this source")
                            .asRuntimeException());
        }

        responseObserver.onNext(CheckExistedResponse.newBuilder()
                .setIsExisted(true)
                .setId(request.getId())
                .build());

        responseObserver.onCompleted();
    }

    @Override
    public void getVehicleDetail(VehicleRequest request, StreamObserver<VehicleResponse> responseObserver) {
        Optional<Vehicle> optionalVehicle = vehicleRepository.findById(request.getId());
        if (!optionalVehicle.isPresent()) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("Vehicle not found with ID: " + request.getId())
                            .asRuntimeException());
        }
        Vehicle vehicle = optionalVehicle.get();

        List<Content> contentList = vehicle.getContents().stream()
                .map(c -> {
                    Content.Builder builder = Content.newBuilder();

                    if (c instanceof Video video) {
                        builder.setId(video.getId())
                                .setDuration(video.getDuration())
                                .setName(video.getName())
                                .setType("Video")
                                .setUrl(video.getUrl());
                    } else if (c instanceof Image image) {
                        builder.setId(image.getId())
                                .setDuration(0)
                                .setUrl(image.getUrl())
                                .setName(image.getName())
                                .setType("Image");
                    } else {
                        throw new IllegalArgumentException("Unknown content type: " + c.getClass().getName());
                    }
                    return builder.build();
                }).toList();

        VehicleResponse response = VehicleResponse.newBuilder()
                .addAllContentList(contentList)
                .setAddress(vehicle.getAddress())
                .setName(vehicle.getName())
                .setDescription(vehicle.getDescription())
                .setProvince(vehicle.getProvince())
                .setWard(vehicle.getWard())
                .setManufactureYear(vehicle.getManufactureYear())
                .setPrice(vehicle.getPrice())
                .putAllProperties(vehicle.getProperties())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
