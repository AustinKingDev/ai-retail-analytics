package com.ai.agent.ai_agent.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Table(name = "items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemEntity {

    //Identity & Metadata
    @Id
    private String itemId;

    private String itemName;

    private String sku;
    private String barcode;
    private String brand;
    private String category;

    //Pricing
    @Column(nullable = false, columnDefinition = "REAL DEFAULT 0.0")
    private double msrp;

    @Column(nullable = false, columnDefinition = "REAL DEFAULT 0.0")
    private double storePrice;

    @Column(nullable = false, columnDefinition = "REAL DEFAULT 0.0")
    private double ecomPrice;

    @Column(nullable = false, columnDefinition = "REAL DEFAULT 0.0")
    private double costPrice;

    @Column(nullable = false, columnDefinition = "REAL DEFAULT 0.0")
    private double discountPercent;

    private String promotion;

    private ZonedDateTime promoStartDate;
    private ZonedDateTime promoEndDate;

    //Inventory & Availability
    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private int quantityInStock;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT 0")
    private boolean onlineAvailable;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT 0")
    private boolean storeAvailable;

    //Timestamps
    private ZonedDateTime createdAt;
    private ZonedDateTime lastUpdated;
    private ZonedDateTime lastPurchasedAt;

    //Metrics
    @Column(nullable = false, columnDefinition = "REAL DEFAULT 0.0")
    private double averageRating;

    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private int numberOfReviews;

    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private int unitsSold;

    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private int recentSalesCount;
}