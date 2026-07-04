package dev.pawan.rupixo.operations.entity;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public class SettlementPaymentId {

    private UUID settlementId;

    private UUID paymentId;
}
