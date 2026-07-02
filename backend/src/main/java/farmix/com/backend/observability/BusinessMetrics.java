package farmix.com.backend.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class BusinessMetrics {

    private final Counter productCreatedCounter;
    private final Counter stockInCounter;
    private final Counter stockOutCounter;
    private final Counter stockAdjustmentCounter;
    private final Counter insufficientStockCounter;

    public BusinessMetrics(MeterRegistry meterRegistry) {
        this.productCreatedCounter = Counter.builder("erp.products.created")
                .description("Number of products created")
                .register(meterRegistry);

        this.stockInCounter = Counter.builder("erp.inventory.stock_in")
                .description("Number of stock-in operations")
                .register(meterRegistry);

        this.stockOutCounter = Counter.builder("erp.inventory.stock_out")
                .description("Number of stock-out operations")
                .register(meterRegistry);

        this.stockAdjustmentCounter = Counter.builder("erp.inventory.adjustments")
                .description("Number of stock adjustment operations")
                .register(meterRegistry);

        this.insufficientStockCounter = Counter.builder("erp.inventory.insufficient_stock")
                .description("Number of rejected stock-out operations due to insufficient stock")
                .register(meterRegistry);
    }

    public void productCreated() {
        productCreatedCounter.increment();
    }

    public void stockIn() {
        stockInCounter.increment();
    }

    public void stockOut() {
        stockOutCounter.increment();
    }

    public void stockAdjusted() {
        stockAdjustmentCounter.increment();
    }

    public void insufficientStock() {
        insufficientStockCounter.increment();
    }

}
