package dev.pawan.rupixo.payment.simulator;

import dev.pawan.rupixo.common.enums.PaymentStatus;
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

    @Scheduled(fixedDelayString = "${payment.simulator.poll-interval-ms:5000}")
    public void processCallBacks(){
        LocalDateTime globalWindow = LocalDateTime.now().minusSeconds(1);
        List<Payment> candidates = paymentRepository
                .findAllByStatusAndCreatedAtBefore(PaymentStatus.AUTHORIZED, globalWindow);

        if(candidates.isEmpty()) return;
        for(Payment payment : candidates){
            simulateCallback(payment);
        }
    }

    private void simulateCallback(Payment payment) {
    }


}
