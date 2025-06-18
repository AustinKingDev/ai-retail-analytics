package com.ai.agent.ai_agent.service.impl;

import com.ai.agent.ai_agent.dto.QueryParameters;
import com.ai.agent.ai_agent.entity.ItemEntity;
import com.ai.agent.ai_agent.service.CustomQueryBuilderService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomQueryBuilderServiceImpl implements CustomQueryBuilderService {

    private static final int DEFAULT_LIMIT = 10;
    private static final Logger logger = LoggerFactory.getLogger(CustomQueryBuilderServiceImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<ItemEntity> runCustomQuery(QueryParameters params) {
        logger.info("Running custom query with params: {}", params);
        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<ItemEntity> cq = cb.createQuery(ItemEntity.class);
            Root<ItemEntity> root = cq.from(ItemEntity.class);

            List<Predicate> predicates = new ArrayList<>();

            addPredicateIfNotNull(predicates, params.getMinUnitsSold(), v -> cb.greaterThanOrEqualTo(root.get("unitsSold"), v));
            addPredicateIfNotNull(predicates, params.getMaxUnitsSold(), v -> cb.lessThanOrEqualTo(root.get("unitsSold"), v));
            addPredicateIfNotNull(predicates, params.getMinAverageRating(), v -> cb.greaterThanOrEqualTo(root.get("averageRating"), v));
            addPredicateIfNotNull(predicates, params.getMaxAverageRating(), v -> cb.lessThanOrEqualTo(root.get("averageRating"), v));
            addPredicateIfNotNull(predicates, params.getMaxStock(), v -> cb.lessThanOrEqualTo(root.get("stock"), v));

            cq.where(predicates.toArray(new Predicate[0]));

            int limit = params.getLimit() != null ? params.getLimit() : DEFAULT_LIMIT;
            return entityManager.createQuery(cq)
                    .setMaxResults(limit)
                    .getResultList();
        } catch (Exception ex) {
            logger.error("Error running custom query with params: {}", params, ex);
            throw new RuntimeException("Failed to run custom query", ex);
        }
    }

    private <T> void addPredicateIfNotNull(List<Predicate> predicates, T value, java.util.function.Function<T, Predicate> predicateFunction) {
        if (value != null) {
            predicates.add(predicateFunction.apply(value));
        }
    }
}
