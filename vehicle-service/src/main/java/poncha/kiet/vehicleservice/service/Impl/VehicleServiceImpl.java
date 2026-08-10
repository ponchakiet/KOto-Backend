package poncha.kiet.vehicleservice.service.Impl;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import poncha.kiet.vehicle.grpc.FileChunk;
import poncha.kiet.vehicle.grpc.UploadResponse;
import poncha.kiet.vehicle.grpc.UploadServiceGrpc;
import poncha.kiet.vehicleservice.dto.request.UploadImageRequestDTO;
import poncha.kiet.vehicleservice.dto.response.VehicleSummaryResponseDTO;
import poncha.kiet.vehicleservice.entity.*;
import poncha.kiet.vehicleservice.exception.ResourceNotFoundException;
import poncha.kiet.vehicleservice.dto.request.VehicleRequestDTO;
import poncha.kiet.vehicleservice.dto.response.VehicleResponseDTO;
import poncha.kiet.vehicleservice.grpc.UserRequest;
import poncha.kiet.vehicleservice.grpc.UserResponse;
import poncha.kiet.vehicleservice.grpc.UserToVehicleServiceGrpc;
import poncha.kiet.vehicleservice.mapper.VehicleMapper;
import poncha.kiet.vehicleservice.projection.VehicleSummary;
import poncha.kiet.vehicleservice.repository.ProjectRepository;
import poncha.kiet.vehicleservice.repository.VehicleRepository;
import poncha.kiet.vehicleservice.service.VehicleService;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {
    @GrpcClient("userServiceGrpc")
    private UserToVehicleServiceGrpc.UserToVehicleServiceBlockingStub serviceBlockingStub;
    @GrpcClient("uploadServiceGrpc")
    private UploadServiceGrpc.UploadServiceStub uploadServiceStub;
    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;
    private final ProjectRepository projectRepository;

    @Override
    public VehicleResponseDTO findVehicleById(String id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle Not Found"));

        return vehicleMapper.toDto(vehicle);
    }

    @Override
    public List<VehicleResponseDTO> findVehicleByType(String type, String userId) {
        List<VehicleResponseDTO> response = vehicleRepository.findAllByTypeAndUserId(VehicleType.valueOf(type), userId)
                .stream()
                .map(a -> vehicleMapper.toDto(a))
                .toList();

        return response;
    }

    @Override
    public Page<VehicleSummaryResponseDTO> findAll(String userId, Pageable pageable) {
        Page<VehicleSummary> page = vehicleRepository.findAllByUserIdAndType(userId, VehicleType.PERSIST, pageable);

        return page.map(vehicleMapper::toSummary);
    }

    @Override
    public void createVehicle(VehicleRequestDTO vehicleRequestDTO) {
        UserResponse userResponse = serviceBlockingStub.getUserById(UserRequest.newBuilder()
                .setId(vehicleRequestDTO.getUserId())
                .build());
        Vehicle vehicle = vehicleMapper.toEntity(vehicleRequestDTO);
        vehicle.getContents().forEach(Content::generateId);
        vehicle.setId(UUID.randomUUID().toString());
        vehicleRepository.save(vehicle);
    }

    @Override
    public void linkVehicleToProject(String vehicleId, String projectId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Not Found Project: " + projectId));

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Not Found Vehicle: " + vehicleId));

        vehicle.setProjectId(projectId);
        vehicleRepository.save(vehicle);
    }

    @Override
    public Map<String, String> uploadImage(String userId, UploadImageRequestDTO request) {
        Vehicle vehicle;
        if (request.getVehicleId() == null) {
            vehicle = Vehicle.builder()
                    .userId(userId)
                    .id(UUID.randomUUID().toString())
                    .build();
        } else
            vehicle = vehicleRepository.findById(request.getVehicleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Not found vehicle"));
        final UploadResponse[] responses = new UploadResponse[1];
        MultipartFile file = request.getFile();
        CountDownLatch finishLatch = new CountDownLatch(1);
        Map<String, String> response = new HashMap<>();
        StreamObserver<UploadResponse> uploadResponseStreamObserver = new StreamObserver<UploadResponse>() {
            @Override
            public void onNext(UploadResponse uploadResponse) {
                responses[0] = uploadResponse;
            }

            @Override
            public void onError(Throwable throwable) {
                finishLatch.countDown();
                throw new RuntimeException(throwable);
            }

            @Override
            public void onCompleted() {
                finishLatch.countDown();
            }
        };
        StreamObserver<FileChunk> requestObserver = uploadServiceStub.upload(uploadResponseStreamObserver);

        try (InputStream inputStream = file.getInputStream()) {
            byte[] buffer = new byte[1024 * 1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                FileChunk chunk = FileChunk.newBuilder()
                        .setContent(ByteString.copyFrom(buffer, 0, bytesRead))
                        .setFileName(file.getOriginalFilename())
                        .build();
                requestObserver.onNext(chunk);
            }
            requestObserver.onCompleted();
            if (!finishLatch.await(10, TimeUnit.SECONDS))
                throw new RuntimeException("Upload timed out");

            if (responses[0] == null)
                throw new RuntimeException("No response received from upload service");

            UploadResponse uploadResponse = responses[0];

            if (uploadResponse.getType().equals(ContentType.IMAGE)) {
                Image image = Image.builder()
                        .id(UUID.randomUUID().toString())
                        .url(uploadResponse.getUrl())
                        .name(file.getOriginalFilename())
                        .createdAt(LocalDateTime.now())
                        .build();
                vehicle.addContent(image);
            } else {
                Video video = Video.builder()
                        .id(UUID.randomUUID().toString())
                        .url(uploadResponse.getUrl())
                        .name(file.getOriginalFilename())
                        .duration(uploadResponse.getDuration())
                        .createdAt(LocalDateTime.now())
                        .build();
                vehicle.addContent(video);
            }
            response.put("vehicleId", vehicle.getId());
            response.put("url", uploadResponse.getUrl());

            vehicleRepository.save(vehicle);

            return response;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file", e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateVehicle(VehicleRequestDTO request) {
        UserResponse userResponse = serviceBlockingStub.getUserById(UserRequest.newBuilder()
                .setId(request.getUserId())
                .build());

        Vehicle vehicle = vehicleRepository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Not foung vehicle"));

        vehicleMapper.update(request, vehicle);
        vehicleRepository.save(vehicle);
    }
}
