package dev.pawan.rupixo.merchant.service;

import java.util.UUID;

public interface CustomerService {
    public UUID findOrCreateCustomer(UUID merchantId, String name, String email, String phone);
}
