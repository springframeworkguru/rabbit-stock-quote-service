package guru.springframework.rabbit.rabbitstockquoteservice.service;

import guru.springframework.rabbit.rabbitstockquoteservice.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.rabbitmq.Receiver;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by jt on 4/18/21.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuoteRunner implements CommandLineRunner {

    private final QuoteGeneratorService quoteGeneratorService;
    private final QuoteMessageSender quoteMessageSender;
    private final Receiver receiver;

    @Override
    public void run(String... args) throws Exception {
        CountDownLatch latch = new CountDownLatch(25);

        quoteGeneratorService.fetchQuoteStream(Duration.ofMillis(100))
                .take(25)
                .log("Got Quote")
                .flatMap(quoteMessageSender::sendQuoteMessage)
                .subscribe(result -> {
                    log.debug("Sent Message to Rabbit");
                    latch.countDown();
                }, throwable -> {
                    log.error("Got Error", throwable);
                }, () -> {
                    log.debug("All Done");
                });

        latch.await(1, TimeUnit.SECONDS);

        AtomicInteger receivedCount = new AtomicInteger();

        receiver.consumeAutoAck(RabbitConfig.QUEUE)
                .log("Msg Receiver")
            .subscribe(msg -> {
                log.debug("Received Message # {} - {}", receivedCount.incrementAndGet(), new String(msg.getBody()));
            }, throwable -> {
                log.debug("Error Receiving", throwable);
            }, () -> {
                log.debug("Complete");
            });
    }
}




















