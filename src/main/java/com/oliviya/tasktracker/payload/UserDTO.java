package com.oliviya.tasktracker.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long userId;
    @NotBlank(message = "User name is required")
    @Size(max = 100)
    private String userName;
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String userEmail;
    private String userRole;
}
