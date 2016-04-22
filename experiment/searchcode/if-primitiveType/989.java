package com.mnubo.java.sdk.client.models.result;

import static com.mnubo.java.sdk.client.utils.Convert.highLevelToPrimitiveType;

import java.util.List;

import com.google.common.base.Preconditions;

/**
 * Hold search query results.
 */
public interface ResultSet extends Iterable<Row> {

    class ColumnDefinition {

        private final String label;
        private final String highLevelType;
        private final String primitiveType;

        public ColumnDefinition(String label, String highLevelType) {
            Preconditions.checkNotNull(label, "label must not be null");
            Preconditions.checkNotNull(highLevelType, "label must not be null");
            this.label = label;
            this.highLevelType = highLevelType;
            this.primitiveType = highLevelToPrimitiveType(highLevelType);
        }

        public String getLabel() {
            return label;
        }

        public String getHighLevelType() {
            return highLevelType;
        }

        public String getPrimitiveType() {
            return primitiveType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            ColumnDefinition that = (ColumnDefinition) o;

            if (!label.equals(that.label))
                return false;
            if (!highLevelType.equals(that.highLevelType))
                return false;
            return primitiveType.equals(that.primitiveType);
        }

        @Override
        public int hashCode() {
            int result = label.hashCode();
            result = 31 * result + highLevelType.hashCode();
            result = 31 * result + primitiveType.hashCode();
            return result;
        }
    }

    List<ColumnDefinition> getColumnDefinitions();

    boolean isExhausted();

    Row one();

    List<Row> all();
}
