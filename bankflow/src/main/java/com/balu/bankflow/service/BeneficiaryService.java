package com.balu.bankflow.service;

import com.balu.bankflow.dto.AddBeneficiaryRequestDTO;
import com.balu.bankflow.dto.BeneficiaryResponseDTO;
import com.balu.bankflow.entity.Beneficiary;
import com.balu.bankflow.entity.User;
import com.balu.bankflow.exception.ResourceNotFoundException;
import com.balu.bankflow.repository.BeneficiaryRepository;
import com.balu.bankflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BeneficiaryService {

    // Inject: BeneficiaryRepository, UserRepository
    private final BeneficiaryRepository beneficiaryRepository;
    private final UserRepository userRepository;

    // METHOD: addBeneficiary(String email, AddBeneficiaryRequestDTO dto)
    // WHO: CUSTOMER only
    // WHAT to return: BeneficiaryResponseDTO
    public BeneficiaryResponseDTO addBeneficiary(String email, AddBeneficiaryRequestDTO dto) {

        // WHAT validate:
        //   - Find user by email → throw ResourceNotFoundException
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        //   - Check if same accountNumber already added by this user
        //     → throw RuntimeException("Beneficiary already exists")
        if (beneficiaryRepository.existsByAccountNumberAndUser(dto.getAccountNumber(), user)) {
            throw new RuntimeException("Beneficiary already exists");
        }

        // WHAT to do:
        //   Step 1: Build and save Beneficiary entity
        Beneficiary beneficiary = Beneficiary.builder()
                .beneficiaryName(dto.getBeneficiaryName())
                .accountNumber(dto.getAccountNumber())
                .bankName(dto.getBankName())
                .ifscCode(dto.getIfscCode())
                .user(user)
                .build();

        Beneficiary savedBeneficiary = beneficiaryRepository.save(beneficiary);

        //   Step 2: log.info beneficiary added
        //   Step 3: Return BeneficiaryResponseDTO
        log.info("Beneficiary has been saved successfully with id: {}", savedBeneficiary.getId());
        return mapToDto(savedBeneficiary);
    }

    // METHOD: getMyBeneficiaries(String email)
    // WHO: CUSTOMER only
    // WHAT to return: List<BeneficiaryResponseDTO>
    public List<BeneficiaryResponseDTO> getMyBeneficiaries(String email) {

        // WHAT to do:
        //   Step 1: Find user by email → throw ResourceNotFoundException
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        //   Step 2: Find all beneficiaries by user id
        //   Step 3: Map to list of BeneficiaryResponseDTO
        return beneficiaryRepository.findByUser_Id(user.getId())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // METHOD: removeBeneficiary(Long beneficiaryId, String email)
    // WHO: CUSTOMER only
    // WHAT to return: String ("Beneficiary removed successfully")
    public String removeBeneficiary(Long beneficiaryId, String email) {

        // WHAT to do:
        //   Step 1: Find user by email → throw ResourceNotFoundException
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        //   Step 2: Find beneficiary by id and user → throw if not found
        Beneficiary beneficiary = beneficiaryRepository.findByIdAndUser(beneficiaryId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found"));

        //   Step 3: Delete beneficiary
        beneficiaryRepository.delete(beneficiary);

        //   Step 4: log.info beneficiary removed
        log.info("Beneficiary has been removed successfully with id: {}", beneficiaryId);
        return "Beneficiary has been removed successfully";
    }

    // PRIVATE METHOD: mapToDto(Beneficiary beneficiary)
    // WHAT to return: BeneficiaryResponseDTO
    private BeneficiaryResponseDTO mapToDto(Beneficiary beneficiary) {
        return BeneficiaryResponseDTO.builder()
                .id(beneficiary.getId())
                .beneficiaryName(beneficiary.getBeneficiaryName())
                .accountNumber(beneficiary.getAccountNumber())
                .bankName(beneficiary.getBankName())
                .ifscCode(beneficiary.getIfscCode())
                .createdAt(beneficiary.getCreatedAt())
                .build();
    }
}
