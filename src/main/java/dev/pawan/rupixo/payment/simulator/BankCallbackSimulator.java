package dev.pawan.rupixo.payment.simulator;

import dev.pawan.rupixo.common.enums.ChaosMode;
import dev.pawan.rupixo.common.enums.PaymentStatus;
import dev.pawan.rupixo.common.util.RandomizerUtil;
import dev.pawan.rupixo.payment.config.SimulatorConfig;
import dev.pawan.rupixo.payment.entity.Payment;
import dev.pawan.rupixo.payment.repository.PaymentRepository;
import dev.pawan.rupixo.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class BankCallbackSimulator {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final SimulatorConfig simulatorConfig;

//    @Scheduled(fixedDelayString = "${payment.simulator.poll-interval-ms:5000}")
    public void processCallBacks(){
        LocalDateTime globalWindow = LocalDateTime.now().minusSeconds(1);
        List<Payment> candidates = paymentRepository
                .findAllByStatusAndCreatedAtBefore(PaymentStatus.AUTHORIZING, globalWindow);

        log.info("Found {} payments eligible for callback simulation", candidates.size());

        if(candidates.isEmpty()) return;
        for(Payment payment : candidates){
            simulateCallback(payment);
        }
    }

    private void simulateCallback(Payment payment) {
        SimulatorConfig.MethodSimulatorConfig methodSimulatorConfig = simulatorConfig.configFor(payment.getMethod());

        LocalDateTime dueAt = dueAt(payment, methodSimulatorConfig);
        if(LocalDateTime.now().isBefore(dueAt)){
            log.debug("Payment {} is not due for callback yet. Due at: {}", payment.getId(), dueAt);
            return;
        }

        switch (simulatorConfig.getChaosMode()) {
            case SUCCESS-> resolve(payment, true);
            case FAILURE -> resolve(payment, false);
            case TIMEOUT -> log.warn("Simulating timeout for payment {}. No callback will be sent.", payment.getId());
            case NORMAL, SLOW -> resolve(payment, shouldApprove(payment, methodSimulatorConfig));
        }
    }

    private boolean shouldApprove(Payment payment, SimulatorConfig.MethodSimulatorConfig methodSimulatorConfig) {
        int randomValue = Math.abs(payment.getId().hashCode()) % 100;
        return randomValue < methodSimulatorConfig.getSuccessRate();
    }

    private void resolve(Payment payment, boolean isSuccessful) {
        if (isSuccessful) {
            String bankRef = "SIM_BANK_REF_" + RandomizerUtil.randomBase64(8);
            paymentService.resolveAuthorization(payment.getId(), true, bankRef, null, null);
        } else {
            paymentService.resolveAuthorization(payment.getId(), false, null, "SIMULATED_BANK_ERROR_CODE", "Simulated bank error message");
        }
    }

    private LocalDateTime dueAt(Payment payment, SimulatorConfig.MethodSimulatorConfig methodConfig) {
        int range = methodConfig.getMaxDelaySeconds() - methodConfig.getMinDelaySeconds();
        int delaySeconds = methodConfig.getMinDelaySeconds() + Math.abs(payment.getId().hashCode()) % (range + 1);


        if(simulatorConfig.getChaosMode() == ChaosMode.SLOW){
            delaySeconds *= 2; // Double the delay for slow chaos mode
        }

        return payment.getCreatedAt().plusSeconds(delaySeconds);
    }

}
