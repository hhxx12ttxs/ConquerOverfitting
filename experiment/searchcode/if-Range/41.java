import com.douban.book.reader.content.page.Position;
import com.douban.book.reader.content.page.Range;
import com.douban.book.reader.content.page.Range.Topology;
this.mRange = null;
}

public Range getRange() {
if (this.mRange == null) {
this.mRange = calculateRange();

