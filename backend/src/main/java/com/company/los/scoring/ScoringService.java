package com.company.los.scoring;

import com.company.los.entity.Customer;
import com.company.los.entity.LoanApplication;

/**
 * Зээлийн шийдвэрийн оноо/дүгнэлт гаргах порт.
 *
 * <p>Одоогийн хэрэгжүүлэлт нь дүрэмд суурилсан ({@link RuleBasedScoringService}).
 * Дараа нь Монголбанкны зээлийн мэдээллийн сан, ХУР, ДАН-ы өгөгдөл нэмэгдэхэд
 * энэ интерфэйсийн цаана өөр хэрэгжүүлэлт (эсвэл ML модель) тавьж болно.</p>
 */
public interface ScoringService {

    /**
     * Хүсэлтийг бүрэн үнэлж, оноо ба шийдвэрийг буцаана.
     */
    ScoringResult score(LoanApplication application);

    /**
     * Зөвхөн харилцагчийн профайлаас (орлого, туршлага, зээлийн түүх) оноо.
     * Хүсэлт үүсгэхээс өмнө урьдчилсан үнэлгээ хийхэд ашиглана.
     */
    int customerScore(Customer customer);
}
