package teamnova.elite_gear.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PaymentRequest {
    private String order_id;
    private String amount;
    private String currency;

}
