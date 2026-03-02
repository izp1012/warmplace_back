package com.warmplace.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SignupRequest {

    @NotBlank(message = "사용자 이름은 필수입니다")
    @Size(min = 3, max = 50, message = "사용자 이름은 3자 이상 50자 이하여야 합니다")
    private String username;

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "유효한 이메일 형식이 아닙니다")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
    private String password;

    @Size(max = 100, message = "닉네임은 100자 이하여야 합니다")
    private String nickname;
}
