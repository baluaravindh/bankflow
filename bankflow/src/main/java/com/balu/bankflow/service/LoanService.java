package com.balu.bankflow.service;

import com.balu.bankflow.dto.LoanApplicationRequestDTO;
import com.balu.bankflow.dto.LoanResponseDTO;
import com.balu.bankflow.dto.LoanStatusUpdateRequestDTO;
import com.balu.bankflow.entity.Loan;
import com.balu.bankflow.entity.User;
import com.balu.bankflow.exception.ResourceNotFoundException;
import com.balu.bankflow.repository.LoanRepository;
import com.balu.bankflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanService {

    // Inject: LoanRepository, UserRepository
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;

    // METHOD: applyForLoan(String email, LoanApplicationRequestDTO dto)
    // WHO: CUSTOMER only
    // WHAT to return: LoanResponseDTO
    public LoanResponseDTO applyForLoan(String email, LoanApplicationRequestDTO dto) {

        // WHAT validate:
        //   - Find user by email → throw ResourceNotFoundException
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        //   - Validate loanAmount > 0, tenure > 0 (or rely on @Valid in controller)
        if (dto.getLoanAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Loan amount must be greater than 0.01");
        }

        if (dto.getTenure() <= 0) {
            throw new RuntimeException("Tenure must be greater than 0.01");
        }

        // WHAT to do:
        //   Step 1: Calculate EMI using calculateEmi(loanAmount, interestRate, tenure)
        BigDecimal emiAmount = calculateEmi(dto.getLoanAmount(), dto.getInterestRate(), dto.getTenure());

        //   Step 2: Build Loan entity — status = PENDING, emiAmount = calculated value, user = found user
        Loan buildLoan = Loan.builder()
                .loanType(dto.getLoanType())
                .loanAmount(dto.getLoanAmount())
                .tenure(dto.getTenure())
                .interestRate(dto.getInterestRate())
                .emiAmount(emiAmount)
                .status(Loan.LoanStatus.PENDING)
                .user(user)
                .build();

        //   Step 3: Save loan
        Loan savedLoan = loanRepository.save(buildLoan);

        //   Step 4: log.info loan application submitted
        log.info("Loan application submitted with id {}", savedLoan.getId());

        //   Step 5: Return LoanResponseDTO
        return mapToDto(savedLoan);
    }


    // METHOD: getMyLoans(String email)
    // WHO: CUSTOMER only
    // WHAT to return: List<LoanResponseDTO>
    public List<LoanResponseDTO> getMyLoans(String email) {
        // WHAT to do:
        //   Step 1: Find user by email → throw ResourceNotFoundException
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        //   Step 2: Find all loans by user (LoanRepository.findByUser)
        //   Step 3: Map to list of LoanResponseDTO
        return loanRepository.findByUser(user)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // METHOD: getLoansByStatus(Loan.LoanStatus status)
    // WHO: ADMIN only
    // WHAT to return: List<LoanResponseDTO>
    public List<LoanResponseDTO> getLoansByStatus(Loan.LoanStatus status) {
        // WHAT to do:
        //   Step 1: Find all loans by status (LoanRepository.findByStatus)
        //   Step 2: Map to list of LoanResponseDTO
        return loanRepository.findByStatus(status)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // METHOD: updateLoanStatus(Long loanId, LoanStatusUpdateRequestDTO dto)
    // WHO: ADMIN only
    // WHAT to return: LoanResponseDTO
    public LoanResponseDTO updateLoanStatus(Long loanId, LoanStatusUpdateRequestDTO dto) {
        // WHAT validate:
        //   - Find loan by id → throw ResourceNotFoundException
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found."));

        //   - Check current status is PENDING → else throw RuntimeException("Loan already processed")
        if (loan.getStatus() != Loan.LoanStatus.PENDING) {
            throw new RuntimeException("Loan already processed");
        }

        //   - dto.status must be APPROVED or REJECTED (not PENDING/CLOSED) → else throw RuntimeException
        if (dto.getStatus().equals(Loan.LoanStatus.PENDING)) {
            throw new RuntimeException("Loan already processed");
        }
        if (dto.getStatus().equals(Loan.LoanStatus.CLOSED)) {
            throw new RuntimeException("Loan already closed");
        }

        // WHAT to do:
        //   Step 1: Set loan.status = dto.status
        //   Step 2: Set loan.remarks = dto.remarks
        //   Step 3: Save loan
        loan.setStatus(dto.getStatus());
        loan.setRemarks(dto.getRemarks());
        Loan updatedLoan = loanRepository.save(loan);

        //   Step 4: log.info loan {id} status changed to {status}
        //   Step 5: Return LoanResponseDTO
        log.info("Loan {} status changed to {}", updatedLoan.getId(), updatedLoan.getStatus());
        return mapToDto(updatedLoan);
    }

    // PRIVATE METHOD: calculateEmi(BigDecimal principal, BigDecimal annualInterestRate, Integer tenureMonths)
    // WHAT to do:
    //   Step 1: Convert annual rate to monthly rate (annualInterestRate / 12 / 100)
    //   Step 2: Apply standard EMI formula: [P x R x (1+R)^N] / [(1+R)^N - 1]
    //   Step 3: Round to 2 decimal places (RoundingMode.HALF_UP)
    // WHAT to return: BigDecimal
    private BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualInterestRate, Integer tenureMonths) {
        BigDecimal monthlyRate = annualInterestRate
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal onePlusRPowN = onePlusR.pow(tenureMonths);

        BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRPowN);
        BigDecimal denominator = onePlusRPowN.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    // PRIVATE METHOD: mapToDto(Loan loan)
// WHAT to return: LoanResponseDTO
    private LoanResponseDTO mapToDto(Loan loan) {
        return LoanResponseDTO.builder()
                .id(loan.getId())
                .loanType(loan.getLoanType())
                .loanAmount(loan.getLoanAmount())
                .tenure(loan.getTenure())
                .interestRate(loan.getInterestRate())
                .emiAmount(loan.getEmiAmount())
                .status(loan.getStatus())
                .remarks(loan.getRemarks())
                .createdAt(loan.getCreatedAt())
                .build();
    }
}
