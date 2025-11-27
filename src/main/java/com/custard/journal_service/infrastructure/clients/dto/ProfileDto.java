package com.custard.journal_service.infrastructure.clients.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProfileDto {
    private String userId;
    private String profilePicture;
    private String firstName;
    private String lastName;
    private Date dob;
    private List<AddressDto> address;
}
