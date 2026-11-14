package com.company.los.scoring;

import com.company.los.entity.Customer;
import com.company.los.entity.LoanApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Дүрэмд суурилсан кредит оноо ба шийдвэрийн engine.
 *
 * <p>Оноо нь 300..850 хооронд. Жин, босго, хязгаарууд нь тохиргооноос
 * (`app.scoring.*`, `app.loan.interest-rates.*`) уншигдана — код өөрчлөхгүйгээр
 * бодлого өөрчилж болно.</p>
 */
@Service
public class RuleBasedScoringService implements ScoringService {

    static final int MIN_SCORE = 300;
    static final int MAX_SCORE = 850;

    private static final BigDecimal DTI_LOW = new BigDecimal("0.30");
    private static final BigDecimal DTI_MEDIUM = new BigDecimal("0.45");
    private static final BigDecimal DTI_HIGH = new BigDecimal("0.60");
    private static final BigDecimal INCOME_HIGH = new BigDecimal("2000000");
    private static final BigDecimal INCOME_MEDIUM = new BigDecimal("1000000");
    private static final BigDecimal AMOUNT_RATIO_LOW = new BigDecimal("0.5");

    @Value("${app.scoring.min-score:650}")
    private int minScore;

    @Value("${app.scoring.max-dti:0.45}")
    private BigDecimal maxDti;

    @Value("${app.scoring.max-amount:10000000}")
    private BigDecimal maxAmount;

    @Value("${app.scoring.max-term-months:60}")
    private int maxTermMonths;

    @Value("${app.scoring.weights.dti:250}")
    private int weightDti;

    @Value("${app.scoring.weights.experience:80}")
    private int weightExperience;

    @Value("${app.scoring.weights.income:90}")
    private int weightIncome;

    @Value("${app.scoring.weights.bureau:100}")
    private int weightBureau;

    @Value("${app.scoring.weights.amount:50}")
    private int weightAmount;

    @Value("${app.scoring.weights.term:30}")
    private int weightTerm;

    @Value("${app.loan.interest-rates.personal-loan:18.5}")
    private BigDecimal defaultAnnualRate;

    @Override
    public ScoringResult score(LoanApplication application) {
        Customer customer = application.getCustomer();

        BigDecimal income = nz(customer == null ? null : customer.getMonthlyIncome());
        BigDecimal payment = monthlyPayment(application);
        BigDecimal dti = income.signum() <= 0
                ? BigDecimal.ONE
                : payment.divide(income, 4, RoundingMode.HALF_UP);

        int score = MIN_SCORE;
        Map<String, Integer> breakdown = new LinkedHashMap<>();
        List<String> reasons = new ArrayList<>();

        // 1) Өрийн ачаалал (DTI)
        int dtiPoints;
        if (dti.compareTo(DTI_LOW) <= 0) {
            dtiPoints = weightDti;
        } else if (dti.compareTo(DTI_MEDIUM) <= 0) {
            dtiPoints = (int) Math.round(weightDti * 0.72);
        } else if (dti.compareTo(DTI_HIGH) <= 0) {
            dtiPoints = (int) Math.round(weightDti * 0.36);
        } else {
            dtiPoints = 0;
        }
        score += dtiPoints;
        breakdown.put("dti", dtiPoints);
        if (dti.compareTo(maxDti) > 0) {
            reasons.add("DTI_HIGH");
        }

        // 2) Ажлын туршлага
        int years = customer != null && customer.getWorkExperienceYears() != null
                ? customer.getWorkExperienceYears() : 0;
        int experiencePoints = years >= 5 ? weightExperience : years >= 2 ? weightExperience / 2 : 0;
        score += experiencePoints;
        breakdown.put("experience", experiencePoints);
        if (years < 2) {
            reasons.add("SHORT_EMPLOYMENT");
        }

        // 3) Орлогын түвшин
        int incomePoints = income.compareTo(INCOME_HIGH) >= 0 ? weightIncome
                : income.compareTo(INCOME_MEDIUM) >= 0 ? (int) Math.round(weightIncome * 0.55) : 0;
        score += incomePoints;
        breakdown.put("income", incomePoints);
        if (income.signum() <= 0) {
            reasons.add("NO_INCOME");
        }

        // 4) Зээлийн түүх (одоогоор customer.creditScore; дараа нь ЗМС-ийн хариу)
        Integer bureauScore = customer == null ? null : customer.getCreditScore();
        int bureauPoints = bureauScore == null ? 0
                : bureauScore >= 750 ? weightBureau
                : bureauScore >= 650 ? weightBureau / 2
                : -(weightBureau / 2);
        score += bureauPoints;
        breakdown.put("bureau", bureauPoints);
        if (bureauScore == null) {
            reasons.add("NO_BUREAU_DATA");
        } else if (bureauScore < 650) {
            reasons.add("LOW_BUREAU_SCORE");
        }

        // 5) Хүсэлтийн дүн / жилийн орлого
        BigDecimal requested = nz(application.getRequestedAmount());
        BigDecimal annualIncome = income.multiply(BigDecimal.valueOf(12));
        BigDecimal amountRatio = annualIncome.signum() <= 0
                ? BigDecimal.TEN
                : requested.divide(annualIncome, 4, RoundingMode.HALF_UP);
        int amountPoints = amountRatio.compareTo(AMOUNT_RATIO_LOW) <= 0 ? weightAmount
                : amountRatio.compareTo(BigDecimal.ONE) <= 0 ? 0 : -weightAmount;
        score += amountPoints;
        breakdown.put("amount", amountPoints);
        if (amountRatio.compareTo(BigDecimal.ONE) > 0) {
            reasons.add("AMOUNT_TOO_HIGH_FOR_INCOME");
        }

        // 6) Хугацаа
        int term = application.getRequestedTermMonths() == null ? 0 : application.getRequestedTermMonths();
        int termPoints = term <= 24 ? weightTerm : term <= 48 ? weightTerm / 3 : 0;
        score += termPoints;
        breakdown.put("term", termPoints);

        score = clamp(score);

        String decision;
        if (income.signum() <= 0) {
            decision = ScoringResult.REJECTED;
        } else if (score >= minScore
                && dti.compareTo(maxDti) <= 0
                && requested.compareTo(maxAmount) <= 0
                && term > 0 && term <= maxTermMonths) {
            decision = ScoringResult.APPROVED;
        } else {
            decision = ScoringResult.UNDER_REVIEW;
            if (score < minScore) {
                reasons.add("SCORE_BELOW_MIN");
            }
            if (requested.compareTo(maxAmount) > 0) {
                reasons.add("AMOUNT_OVER_LIMIT");
            }
            if (term > maxTermMonths) {
                reasons.add("TERM_OVER_LIMIT");
            }
        }

        return new ScoringResult(score, dti, decision, reasons, breakdown);
    }

    @Override
    public int customerScore(Customer customer) {
        if (customer == null) {
            return MIN_SCORE;
        }

        BigDecimal income = nz(customer.getMonthlyIncome());
        int incomePoints = income.compareTo(INCOME_HIGH) >= 0 ? weightIncome
                : income.compareTo(INCOME_MEDIUM) >= 0 ? (int) Math.round(weightIncome * 0.55) : 0;

        Integer years = customer.getWorkExperienceYears();
        int experiencePoints = years == null ? 0 : years >= 5 ? weightExperience : years >= 2 ? weightExperience / 2 : 0;

        Integer bureauScore = customer.getCreditScore();
        int bureauPoints = bureauScore == null ? 0
                : bureauScore >= 750 ? weightBureau
                : bureauScore >= 650 ? weightBureau / 2
                : -(weightBureau / 2);

        return clamp(MIN_SCORE + incomePoints + experiencePoints + bureauPoints);
    }

    /**
     * Аннуитетийн сарын төлбөр: {@code P * r / (1 - (1 + r)^-n)}.
     */
    BigDecimal monthlyPayment(LoanApplication application) {
        BigDecimal principal = nz(application.getRequestedAmount());
        int months = application.getRequestedTermMonths() == null ? 0 : application.getRequestedTermMonths();
        if (months <= 0 || principal.signum() <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal rate = defaultAnnualRate == null ? BigDecimal.valueOf(18.5) : defaultAnnualRate;
        double monthlyRate = rate.doubleValue() / 100 / 12;
        if (monthlyRate <= 0) {
            return principal.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        }

        double factor = monthlyRate / (1 - Math.pow(1 + monthlyRate, -months));
        return principal.multiply(BigDecimal.valueOf(factor)).setScale(2, RoundingMode.HALF_UP);
    }

    private int clamp(int score) {
        return Math.max(MIN_SCORE, Math.min(MAX_SCORE, score));
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
