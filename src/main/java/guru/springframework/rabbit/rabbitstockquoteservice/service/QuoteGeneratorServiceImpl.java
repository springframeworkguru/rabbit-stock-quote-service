package guru.springframework.rabbit.rabbitstockquoteservice.service;

import guru.springframework.rabbit.rabbitstockquoteservice.model.Quote;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SynchronousSink;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

@Service
public class QuoteGeneratorServiceImpl implements QuoteGeneratorService {

    private final MathContext mathContext = new MathContext(2);
    private final Random random = new Random();
    private final List<Quote> quotes = new ArrayList<>();

    public QuoteGeneratorServiceImpl() {
        this.quotes.add(new Quote("AAPL", 134.16));
        this.quotes.add(new Quote("TSLA", 739.74));
        this.quotes.add(new Quote("NFLX", 546.25));
        this.quotes.add(new Quote("ARKK", 124.51));
        this.quotes.add(new Quote("SQ", 256.34));
        this.quotes.add(new Quote("DIS", 187.29));
        this.quotes.add(new Quote("MFST", 260.29));
        this.quotes.add(new Quote("PLTR", 23.21));
    }

    @Override
    public Flux<Quote> fetchQuoteStream(Duration period) {

        // We use here Flux.generate to create quotes,
        // iterating on each stock starting at index 0
        return Flux.generate(() -> 0,
                (BiFunction<Integer, SynchronousSink<Quote>, Integer>) (index, sink) -> {
                    Quote updatedQuote = updateQuote(this.quotes.get(index));
                    sink.next(updatedQuote);
                    return ++index % this.quotes.size();
                })
                // We want to emit them with a specific period;
                // to do so, we zip that Flux with a Flux.interval
                .zipWith(Flux.interval(period))
                .map(t -> t.getT1())
                // Because values are generated in batches,
                // we need to set their timestamp after their creation
                .map(quote -> {
                    quote.setInstant(Instant.now());
                    return quote;
                })
                .log("guru.springframework.service.QuoteGeneratorService");
    }

    private Quote updateQuote(Quote quote) {
        BigDecimal priceChange = quote.getPrice()
                .multiply(new BigDecimal(0.05 * this.random.nextDouble()), this.mathContext);
        return new Quote(quote.getTicker(), quote.getPrice().add(priceChange));
    }
}
