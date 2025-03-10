package io.jefrajames.booking;

import java.time.temporal.ChronoUnit;

import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.spi.RegisterAIService;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@RegisterAIService(tools = BookingService.class, chatMemoryName = "chat-ai-service-memory", chatLanguageModelName = "chat-model")
public interface ChatAiService {

    @SystemMessage("""
            You are a customer support agent of a car rental company named 'Miles of Smiles'.
            Before providing information about booking or canceling a booking, you MUST always check:
            booking number, customer name and surname.
            You should not answer to any request not related to car booking or Miles of Smiles company general information.
            When a customer wants to cancel a booking, you must check his name and the Miles of Smiles cancellation policy first.
            Any cancelation request must comply with cancellation policy both for the delay and the duration.
            Today is {{current_date}}.
            """)
    @Timeout(unit = ChronoUnit.MINUTES, value = 5)
    @Retry(abortOn = { BookingCannotBeCanceledException.class,
            BookingAlreadyCanceledException.class,
            BookingNotFoundException.class }, maxRetries = 2)
    @Fallback(fallbackMethod = "chatFallback", skipOn = {
            BookingCannotBeCanceledException.class,
            BookingAlreadyCanceledException.class,
            BookingNotFoundException.class })
    // String chat(@V("question") @UserMessage String question);
    String chat(String question);

    default String chatFallback(String question) {
        return String.format(
                "Sorry, I am not able to answer your request %s at the moment. Please try again later.",
                question);
    }

}
