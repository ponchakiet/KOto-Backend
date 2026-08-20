package poncha.kiet.vehicleservice.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import poncha.kiet.vehicleservice.dto.request.DealerRequestDTO;
import poncha.kiet.vehicleservice.entity.Dealer;
import poncha.kiet.vehicleservice.mapper.DealerMapper;
import poncha.kiet.vehicleservice.repository.DealerRepository;
import poncha.kiet.vehicleservice.service.DealerService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DealerServiceImpl implements DealerService {
    private final DealerRepository dealerRepository;
    private final DealerMapper dealerMapper;

    @Override
    public void createDealer(DealerRequestDTO dealerRequestDTO) {
        Dealer dealer = dealerMapper.toEntity(dealerRequestDTO);
        dealer.setId(UUID.randomUUID().toString());
        dealerRepository.save(dealer);
    }
}
