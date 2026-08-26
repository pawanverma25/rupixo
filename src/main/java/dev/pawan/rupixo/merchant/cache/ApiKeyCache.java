package dev.pawan.rupixo.merchant.cache;

import java.util.Optional;

public interface ApiKeyCache {

    public void put(String keyId, ApiKeyCacheEntry entry);

    public Optional<ApiKeyCacheEntry> get(String keyId);

    public void evict(String keyId);
}
