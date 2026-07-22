package farmix.com.backend.customer.service;

import farmix.com.backend.common.exception.NotFoundException;
import farmix.com.backend.company.entity.Company;
import farmix.com.backend.company.repository.CompanyRepository;
import farmix.com.backend.customer.dto.CreateCustomerRequest;
import farmix.com.backend.customer.dto.CustomerResponse;
import farmix.com.backend.customer.dto.UpdateCustomerRequest;
import farmix.com.backend.customer.entity.Customer;
import farmix.com.backend.customer.entity.CustomerStatus;
import farmix.com.backend.customer.mapper.CustomerMapper;
import farmix.com.backend.customer.repository.CustomerRepository;
import farmix.com.backend.security.CurrentUser;
import farmix.com.backend.user.entity.User;
import farmix.com.backend.user.repository.UserRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final CustomerMapper customerMapper;
    private final CurrentUser currentUser;

    @Transactional
    @PreAuthorize("hasRole('COMPANY_ADMIN') or hasRole('MANAGER')")
    public CustomerResponse create(CreateCustomerRequest request){
        Long companyId = currentUser.getCompanyId();
        Long userId = currentUser.getUserId();

        Company company = companyRepository.getReferenceById(companyId);
        User creator = userRepository.getReferenceById(userId);

        Customer customer = Customer.builder()
                .company(company)
                .createdBy(creator)
                .fullName(request.fullName())
                .phone(request.phone())
                .email(request.email())
                .address(request.address())
                .notes(request.notes())
                .status(CustomerStatus.ACTIVE)
                .build();

        Customer createdCustomer = customerRepository.save(customer);

        return customerMapper.toResponse(createdCustomer);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('COMPANY_ADMIN','MANAGER', 'EMPLOYEE')")
    public CustomerResponse get(Long id){
    Long companyId = currentUser.getCompanyId();

    Customer customer = customerRepository.findByIdAndCompany_Id(id, companyId)
            .orElseThrow(() -> new NotFoundException("Customer not found"));

    return customerMapper.toResponse(customer);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('COMPANY_ADMIN','MANAGER', 'EMPLOYEE')")
    public Page<CustomerResponse> search(String q, CustomerStatus status, Pageable pageable){
        Long companyId = currentUser.getCompanyId();

        return customerRepository.search(companyId, normalizeSearchQuery(q), status, sanitizePageable(pageable))
                .map(customerMapper::toResponse);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('COMPANY_ADMIN','MANAGER')")
    public CustomerResponse update(Long id, UpdateCustomerRequest request){
        Long companyId = currentUser.getCompanyId();
        Long userId = currentUser.getUserId();

        Customer customer = customerRepository.findByIdAndCompany_Id(id, companyId).orElseThrow(() -> new NotFoundException("Customer not found"));

        User updater = userRepository.getReferenceById(userId);

        customer.setUpdatedBy(updater);
        customer.setFullName(request.fullName().trim());
        customer.setPhone(normalizeNullableText(request.phone()));
        customer.setEmail(normalizeEmail(request.email()));
        customer.setAddress(normalizeNullableText(request.address()));
        customer.setNotes(normalizeNullableText(request.notes()));

        return customerMapper.toResponse(customer);
    }

    private String normalizeEmail(@Email(message = "Email must be valid") @Size(max = 150, message = "Email must not exceed 150 characters") String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        return email.trim().toLowerCase();
    }

    @Transactional
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER')")
    public CustomerResponse archive(Long id) {
        Long companyId = currentUser.getCompanyId();
        Long userId = currentUser.getUserId();

        Customer customer = customerRepository.findByIdAndCompany_Id(id, companyId)
                .orElseThrow(() -> new NotFoundException("Customer not found"));

        User updater = userRepository.getReferenceById(userId);

        customer.setUpdatedBy(updater);
        customer.setStatus(CustomerStatus.ARCHIVED);

        return customerMapper.toResponse(customer);
    }

    private String normalizeNullableText(@Size(max = 30, message = "Phone must not exceed 30 characters") String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private Pageable sanitizePageable(Pageable pageable) {
       int page = Math.max(1, pageable.getPageNumber());
       int size = Math.min(Math.max(pageable.getPageSize(), pageable.getPageSize()), 100);
       return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private String normalizeSearchQuery(String q) {
        if (q == null || q.isBlank()) {
            return null;
        }
        return q.trim();
    }

}

