import com.assets.investment.entities.InvestmentAction;
import com.assets.statistic.list.StockList;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
default StockPrice loadInstant(StockList stockList, Instant instant, Instant nextInstant) throws StockNotFoundException {
long diff = nextInstant.getEpochSecond() - instant.getEpochSecond();
Instant previousInstant = instant.minus(diff, ChronoUnit.SECONDS); //TODO esto es un poco manga...

