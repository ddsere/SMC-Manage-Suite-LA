package lk.ijse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data

public class EmployeeDTO {
    private String id;
    private String name;
    private String salary;
    private String address;
    private String phone;
}
