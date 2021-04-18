package guru.springframework.rabbit.rabbitstockquoteservice.service;



import guru.springframework.rabbit.rabbitstockquoteservice.model.Quote;
import reactor.core.publisher.Flux;

import java.time.Duration;

public interface QuoteGeneratorService {

    Flux<Quote> fetchQuoteStream(Duration period);
}
