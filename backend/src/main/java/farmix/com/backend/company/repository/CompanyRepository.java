package farmix.com.backend.company.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import farmix.com.backend.company.entity.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    
}
