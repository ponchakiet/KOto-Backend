package poncha.kiet.vehicleservice.projection;

import poncha.kiet.vehicleservice.entity.Content;

import java.time.LocalDateTime;
import java.util.List;

public interface VehicleSummary {
    String getId();

    String getAddress();

    String getWard();

    String getProvince();

    double getPrice();

    int getManufactureYear();

    LocalDateTime getCreatedAt();

    List<Content> getContents();
}
