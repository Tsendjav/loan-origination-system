package com.company.los.scoring;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Scoring engine-ийн үр дүн.
 *
 * @param score     300..850 хоорондын кредит оноо
 * @param dti       төлбөр / сарын орлого (0.45-аас дээш бол эрсдэлтэй)
 * @param decision  APPROVED | UNDER_REVIEW | REJECTED
 * @param reasons   шийдвэрийн шалтгаанууд (машинд уншигдах код)
 * @param breakdown компонент тус бүрийн оноо (аудит/тайлбарт)
 */
public record ScoringResult(
        int score,
        BigDecimal dti,
        String decision,
        List<String> reasons,
        Map<String, Integer> breakdown
) {

    public static final String APPROVED = "APPROVED";
    public static final String UNDER_REVIEW = "UNDER_REVIEW";
    public static final String REJECTED = "REJECTED";

    public boolean isApproved() {
        return APPROVED.equals(decision);
    }
}
