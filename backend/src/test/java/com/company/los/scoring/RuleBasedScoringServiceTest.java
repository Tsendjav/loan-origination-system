package com.company.los.scoring;

import com.company.los.entity.Customer;
import com.company.los.entity.LoanApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Дүрэмд суурилсан scoring engine-ийн тест.
 */
class RuleBasedScoringServiceTest {

    private RuleBasedScoringService scoring;

    @BeforeEach
    void setUp() {
        scoring = new RuleBasedScoringService();
        ReflectionTestUtils.setField(scoring, "minScore", 650);
        ReflectionTestUtils.setField(scoring, "maxDti", new BigDecimal("0.45"));
        ReflectionTestUtils.setField(scoring, "maxAmount", new BigDecimal("10000000"));
        ReflectionTestUtils.setField(scoring, "maxTermMonths", 60);
        ReflectionTestUtils.setField(scoring, "weightDti", 250);
        ReflectionTestUtils.setField(scoring, "weightExperience", 80);
        ReflectionTestUtils.setField(scoring, "weightIncome", 90);
        ReflectionTestUtils.setField(scoring, "weightBureau", 100);
        ReflectionTestUtils.setField(scoring, "weightAmount", 50);
        ReflectionTestUtils.setField(scoring, "weightTerm", 30);
        ReflectionTestUtils.setField(scoring, "defaultAnnualRate", new BigDecimal("18.5"));
    }

    @Test
    @DisplayName("Сайн профайл, дүн ба хугацаа хязгаарт — APPROVED")
    void goodProfileIsApproved() {
        ScoringResult result = scoring.score(application(2_000_000, 5_000_000, 24, 5, null));

        assertThat(result.decision()).isEqualTo(ScoringResult.APPROVED);
        assertThat(result.score()).isGreaterThanOrEqualTo(650);
        assertThat(result.dti()).isLessThan(new BigDecimal("0.30"));
        assertThat(result.breakdown()).containsKeys("dti", "experience", "income", "bureau", "amount", "term");
    }

    @Test
    @DisplayName("Хязгаараас их дүн — UNDER_REVIEW, AMOUNT_OVER_LIMIT")
    void amountOverLimitGoesToReview() {
        ScoringResult result = scoring.score(application(2_000_000, 20_000_000, 24, 5, null));

        assertThat(result.decision()).isEqualTo(ScoringResult.UNDER_REVIEW);
        assertThat(result.reasons()).contains("AMOUNT_OVER_LIMIT");
    }

    @Test
    @DisplayName("Орлогогүй — REJECTED, NO_INCOME")
    void noIncomeIsRejected() {
        ScoringResult result = scoring.score(application(0, 3_000_000, 12, 3, 700));

        assertThat(result.decision()).isEqualTo(ScoringResult.REJECTED);
        assertThat(result.reasons()).contains("NO_INCOME");
    }

    @Test
    @DisplayName("Бага орлого, богино туршлага, муу түүх — UNDER_REVIEW")
    void weakProfileGoesToReview() {
        ScoringResult result = scoring.score(application(1_200_000, 5_000_000, 60, 1, 600));

        assertThat(result.decision()).isEqualTo(ScoringResult.UNDER_REVIEW);
        assertThat(result.score()).isLessThan(650);
        assertThat(result.reasons()).contains("SCORE_BELOW_MIN", "LOW_BUREAU_SCORE", "SHORT_EMPLOYMENT");
    }

    @Test
    @DisplayName("Маш сайн профайл — оноо 850-аас хэтрэхгүй")
    void excellentProfileIsCapped() {
        ScoringResult result = scoring.score(application(3_000_000, 10_000_000, 48, 10, 780));

        assertThat(result.decision()).isEqualTo(ScoringResult.APPROVED);
        assertThat(result.score()).isLessThanOrEqualTo(850);
    }

    @Test
    @DisplayName("customerScore орлого, туршлага, түүхийг харгалзана")
    void customerScoreUsesProfile() {
        Customer weak = customer(500_000, 1, null);
        Customer strong = customer(3_000_000, 10, 800);

        assertThat(scoring.customerScore(strong)).isGreaterThan(scoring.customerScore(weak));
    }

    private LoanApplication application(long income, long amount, int termMonths,
                                        int experienceYears, Integer bureauScore) {
        LoanApplication application = new LoanApplication();
        application.setCustomer(customer(income, experienceYears, bureauScore));
        application.setRequestedAmount(BigDecimal.valueOf(amount));
        application.setRequestedTermMonths(termMonths);
        application.setLoanType(LoanApplication.LoanType.PERSONAL);
        application.setStatus(LoanApplication.ApplicationStatus.SUBMITTED);
        return application;
    }

    private Customer customer(long income, int experienceYears, Integer bureauScore) {
        Customer customer = new Customer();
        customer.setMonthlyIncome(BigDecimal.valueOf(income));
        customer.setWorkExperienceYears(experienceYears);
        customer.setCreditScore(bureauScore);
        return customer;
    }
}
