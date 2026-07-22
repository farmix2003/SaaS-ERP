package farmix.com.backend.customer.customer;

import farmix.com.backend.common.dto.PageResponse;
import farmix.com.backend.customer.dto.CreateCustomerRequest;
import farmix.com.backend.customer.dto.CustomerResponse;
import farmix.com.backend.customer.dto.UpdateCustomerRequest;
import farmix.com.backend.customer.entity.CustomerStatus;
import farmix.com.backend.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse create(@Valid @RequestBody CreateCustomerRequest req){
        return customerService.create(req);
    }

    @GetMapping("/{id}")
    public CustomerResponse getById(@PathVariable Long id){
        return customerService.get(id);
    }

    @GetMapping
    public PageResponse<CustomerResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false)CustomerStatus status,
            Pageable pageable
            ){
        return PageResponse.from(customerService.search(q, status, pageable));
    }

    @PutMapping("/{id}")
    public CustomerResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCustomerRequest request
    ) {
        return customerService.update(id, request);
    }

    @PatchMapping("/{id}/archive")
    public CustomerResponse archive(
            @PathVariable Long id
    ) {
        return customerService.archive(id);
    }
}
