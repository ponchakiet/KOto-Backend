package poncha.kiet.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import poncha.kiet.userservice.dto.response.UserResponseDTO;
import poncha.kiet.userservice.entity.User;
import poncha.kiet.userservice.exception.ResourceNotFoundException;
import poncha.kiet.userservice.mapper.UserMapper;
import poncha.kiet.userservice.repository.UserRepository;
import poncha.kiet.userservice.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public Page<UserResponseDTO> findAll(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.map(userMapper::toDto);
    }

    @Override
    public UserResponseDTO findById(String id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Not found user with id: " + id));

        return userMapper.toDto(user);
    }
}
