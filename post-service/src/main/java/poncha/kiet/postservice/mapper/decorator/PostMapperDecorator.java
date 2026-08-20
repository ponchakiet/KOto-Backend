package poncha.kiet.postservice.mapper.decorator;

import lombok.NoArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import poncha.kiet.postservice.dto.response.PostDetailResponseDTO;
import poncha.kiet.postservice.entity.Post;
import poncha.kiet.postservice.grpc.*;
import poncha.kiet.postservice.mapper.PostMapper;
import poncha.kiet.postservice.mapper.UserMapper;
import poncha.kiet.postservice.mapper.VehicleMapper;

@NoArgsConstructor
public abstract class PostMapperDecorator implements PostMapper {
    @Autowired
    private PostMapper postMapper;
    @GrpcClient("vehicleServiceGrpc")
    private VehicleToPostServiceGrpc.VehicleToPostServiceBlockingStub vehicleToPostServiceBlockingStub;
    @GrpcClient("userServiceGrpc")
    private UserToPostServiceGrpc.UserToPostServiceBlockingStub userToPostServiceBlockingStub;
    @Autowired
    private VehicleMapper vehicleMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public PostDetailResponseDTO toDetailResponse(Post post) {
        PostDetailResponseDTO response = postMapper.toDetailResponse(post);
        VehicleResponse vehicleResponse = vehicleToPostServiceBlockingStub.getVehicleDetail(VehicleRequest.newBuilder()
                .setId(post.getVehicleId())
                .build());

        UserPostInfoResponse userPostInfoResponse = userToPostServiceBlockingStub.getUserInfo(UserInfoRequest.newBuilder()
                .setId(post.getUserId())
                .build());

        response.setVehicleDetail(vehicleMapper.toDto(vehicleResponse));
        response.setUserInfo(userMapper.toResponseDTO(userPostInfoResponse));

        return response;
    }
}
