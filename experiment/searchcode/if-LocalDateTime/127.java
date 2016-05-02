package com.carhistory.entity.parent;

import com.carhistory.entity.converter.TimestampConverter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Created by jarec on 28.5.14.
 */
@Audited
@MappedSuperclass
public abstract class ChEntity<PK extends Serializable> implements Serializable {

    private PK id;
    private Boolean active = Boolean.TRUE;
    private LocalDateTime createdDateTime = LocalDateTime.now();

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public PK getId() {
        return id;
    }

    public void setId(PK id) {
        this.id = id;
    }

    @Column(name = "active", nullable = false)
    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @NotAudited
    @Convert(converter = TimestampConverter.class)
    @Column(name = "created_datetime", nullable = false)
    public LocalDateTime getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(LocalDateTime createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        ChEntity that = (ChEntity) o;
        return new EqualsBuilder().append(getId(), that.getId()).build();
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder().append(getClass()).append(getId()).build();
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).toString();
    }
}

