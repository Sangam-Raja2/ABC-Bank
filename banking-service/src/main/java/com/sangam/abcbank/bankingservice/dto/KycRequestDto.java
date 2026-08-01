package com.sangam.abcbank.bankingservice.dto;

import com.sangam.abcbank.bankingservice.model.DocumentType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class KycRequestDto {

    @NotBlank(message = "customerId is required")
    private String customerId;

    @NotBlank(message = "fullName is required")
    @Size(max = 150)
    private String fullName;

    @NotNull(message = "dateOfBirth is required")
    @Past(message = "dateOfBirth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "documentType is required")
    private DocumentType documentType;

    @NotBlank(message = "documentNumber is required")
    @Size(max = 50)
    private String documentNumber;

    @NotBlank(message = "address is required")
    private String address;

    @NotBlank(message = "phoneNumber is required")
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "phoneNumber must be a valid number")
    private String phoneNumber;

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    private String email;
}
