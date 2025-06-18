package com.ai.agent.ai_agent.config;

import com.ai.agent.ai_agent.entity.ItemEntity;
import com.ai.agent.ai_agent.repository.ItemRepository;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Configuration
public class DataSeeder {

    private static final int TOTAL_ITEMS = 5000;
    private static final int BATCH_SIZE = 500;

    @Bean
    public CommandLineRunner seedDatabase(ItemRepository itemRepository) {
        return args -> {
            if (itemRepository.count() > 0) {
                log.info("ℹ️ Database already seeded. Skipping.");
                return;
            }

            Faker faker = new Faker();
            Random random = new Random();
            List<ItemEntity> batch = new ArrayList<>();

            for (int i = 1; i <= TOTAL_ITEMS; i++) {
                double msrp = faker.number().randomDouble(2, 30, 500);
                double storePrice = msrp - faker.number().randomDouble(2, 1, 50);
                double ecomPrice = Math.max(1.0, storePrice - faker.number().randomDouble(2, 0, 15));
                double discount = Math.round(((msrp - storePrice) / msrp) * 10000.0) / 100.0;

                boolean onlineOnly = random.nextBoolean();
                boolean storeOnly = !onlineOnly && random.nextBoolean();
                boolean onlineAvailable = onlineOnly || (!storeOnly && random.nextBoolean());
                boolean storeAvailable = storeOnly || (!onlineOnly && random.nextBoolean());

                ZonedDateTime now = ZonedDateTime.now();
                ZonedDateTime promoStart = now.minusDays(faker.number().numberBetween(0, 5));
                ZonedDateTime promoEnd = promoStart.plusDays(faker.number().numberBetween(1, 10));

                batch.add(ItemEntity.builder()
                        .itemId("ITEM" + String.format("%05d", i))
                        .itemName(faker.commerce().productName())
                        .sku("SKU" + faker.number().digits(8))
                        .barcode(faker.code().ean13())
                        .brand(faker.company().name())
                        .category(faker.commerce().department())
                        .msrp(msrp)
                        .storePrice(storePrice)
                        .ecomPrice(ecomPrice)
                        .costPrice(storePrice * (0.6 + 0.2 * Math.random()))
                        .discountPercent(discount)
                        .promotion(faker.company().buzzword())
                        .promoStartDate(promoStart)
                        .promoEndDate(promoEnd)
                        .quantityInStock(faker.number().numberBetween(0, 500))
                        .onlineAvailable(onlineAvailable)
                        .storeAvailable(storeAvailable)
                        .createdAt(now.minusDays(faker.number().numberBetween(5, 30)))
                        .lastUpdated(now.minusHours(faker.number().numberBetween(1, 72)))
                        .lastPurchasedAt(now.minusDays(faker.number().numberBetween(1, 10)))
                        .averageRating(Math.round((faker.number().randomDouble(1, 2, 5)) * 10.0) / 10.0)
                        .numberOfReviews(faker.number().numberBetween(0, 1000))
                        .unitsSold(faker.number().numberBetween(0, 5000))
                        .build());

                if (batch.size() == BATCH_SIZE) {
                    saveBatch(itemRepository, batch);
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                saveBatch(itemRepository, batch);
            }

            log.info("Seeded {} items to the database.", TOTAL_ITEMS);
            log.info("H2 Console available at: http://localhost:8080/h2-console");
            log.info("JDBC URL: jdbc:h2:mem:aiagentdb");
        };
    }

    @Transactional
    public void saveBatch(ItemRepository repository, List<ItemEntity> batch) {
        repository.saveAll(batch);
    }
}
