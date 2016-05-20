/*
 * This file is part of Pi Commons.
 *
 * Copyright (C) 2011 Pi Solutions <info@pisolutions.eu>
 *
 * Pi Commons is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Pi Commons is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Pi Commons.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.pisolutions.beans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;

import eu.pisolutions.lang.Objects;

/**
 * Constrained JavaBean.
 *
 * @author Laurent Pireyn
 */
public class ConstrainedJavaBean
extends JavaBean
implements VetoableChangeEventSource {
    private final VetoableChangeSupport vetoableChangeSupport = new VetoableChangeSupport(this);

    public ConstrainedJavaBean() {
        super();
    }

    public final void addVetoableChangeListener(VetoableChangeListener listener) {
        this.vetoableChangeSupport.addVetoableChangeListener(listener);
    }

    public final void addVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
        this.vetoableChangeSupport.addVetoableChangeListener(propertyName, listener);
    }

    public final void removeVetoableChangeListener(VetoableChangeListener listener) {
        this.vetoableChangeSupport.removeVetoableChangeListener(listener);
    }

    public final void removeVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
        this.vetoableChangeSupport.removeVetoableChangeListener(propertyName, listener);
    }

    protected final boolean setConstrainedProperty(String propertyName, boolean oldValue, boolean newValue)
    throws PropertyVetoException {
        if (oldValue != newValue) {
            this.fireVetoableChange(propertyName, oldValue, newValue);
        }
        return newValue;
    }

    protected final int setConstrainedProperty(String propertyName, int oldValue, int newValue)
    throws PropertyVetoException {
        if (oldValue != newValue) {
            this.fireVetoableChange(propertyName, oldValue, newValue);
        }
        return newValue;
    }

    protected final <T> T setConstrainedProperty(String propertyName, Object oldValue, T newValue)
    throws PropertyVetoException {
        if (!Objects.equals(oldValue, newValue)) {
            this.fireVetoableChange(propertyName, oldValue, newValue);
        }
        return newValue;
    }

    protected final void fireVetoableChange(PropertyChangeEvent event)
    throws PropertyVetoException {
        this.vetoableChangeSupport.fireVetoableChange(event);
        this.firePropertyChange(event);
    }

    protected final void fireVetoableChange(String propertyName, boolean oldValue, boolean newValue)
    throws PropertyVetoException {
        this.vetoableChangeSupport.fireVetoableChange(propertyName, oldValue, newValue);
        this.firePropertyChange(propertyName, oldValue, newValue);
    }

    protected final void fireVetoableChange(String propertyName, int oldValue, int newValue)
    throws PropertyVetoException {
        this.vetoableChangeSupport.fireVetoableChange(propertyName, oldValue, newValue);
        this.firePropertyChange(propertyName, oldValue, newValue);
    }

    protected final void fireVetoableChange(String propertyName, Object oldValue, Object newValue)
    throws PropertyVetoException {
        this.vetoableChangeSupport.fireVetoableChange(propertyName, oldValue, newValue);
        this.firePropertyChange(propertyName, oldValue, newValue);
    }
}

