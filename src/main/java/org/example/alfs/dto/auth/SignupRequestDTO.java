package org.example.alfs.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Schema(description = "Credentials required to sign up new user")
public class SignupRequestDTO {

    @NotBlank(message = "Username is required")
    @Schema(description = "New username", example = "cool_username", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(description = "New password", example = "itsAsecret123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

}
