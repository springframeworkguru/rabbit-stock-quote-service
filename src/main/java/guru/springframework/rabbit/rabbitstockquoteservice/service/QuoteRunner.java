package guru.springframework.rabbit.rabbitstockquoteservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Created by jt on 4/18/21.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuoteRunner implements CommandLineRunner {

    private final QuoteGeneratorService quoteGeneratorService;
    private final QuoteMessageSender quoteMessageSender;

    @Override
    public void run(String... args) throws Exception {
        quoteGeneratorService.fetchQuoteStream(Duration.ofMillis(100))
                .take(25)
                .log("Got Quote")
                .flatMap(quoteMessageSender::sendQuoteMessage)
                .subscribe(result -> {
                    log.debug("Sent Message to Rabbit");
                }, throwable -> {
                    log.error("Got Error", throwable);
                }, () -> {
                    log.debug("All Done");
                });
    }
}
