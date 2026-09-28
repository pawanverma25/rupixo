package dev.pawan.rupixo.merchant.service.impl;

import dev.pawan.rupixo.common.exception.ResourceNotFoundException;
import dev.pawan.rupixo.merchant.entity.Customer;
import dev.pawan.rupixo.merchant.entity.Merchant;
import dev.pawan.rupixo.merchant.repository.CustomerRepository;
import dev.pawan.rupixo.merchant.repository.MerchantRepository;
import dev.pawan.rupixo.merchant.service.CustomerService;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final MerchantRepository merchantRepository;


    @Override
    @Transactional
    public @Nullable UUID findOrCreateCustomer(UUID merchantId, String name, String email, String phone) {
        if(email == null) return null;
        return customerRepository.findByMerchant_IdAndEmail(merchantId, email)
                .map(Customer::getId)
                .orElseGet(() -> createNewCustomer(merchantId, name, email, phone));
    }

    private UUID createNewCustomer(UUID merchantId, String name, String email, String phone) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found with id", merchantId));

        Customer customer = Customer.builder()
                .merchant(merchant)
                .name(name)
                .email(email)
                .phone(phone)
                .build();
        customer = customerRepository.save(customer);
        log.info("Created new customer with id: {} and email {}, for merchant: {}", customer.getId(), customer.getEmail(), merchantId);

        return customer.getId();
    }
}
