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
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import eu.pisolutions.lang.Objects;

/**
 * JavaBean.
 *
 * @author Laurent Pireyn
 */
public class JavaBean
extends Object
implements PropertyChangeEventSource {
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public JavaBean() {
        super();
    }

    public final void addPropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public final void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        this.propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public final void removePropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public final void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        this.propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }

    protected final boolean setProperty(String propertyName, boolean oldValue, boolean newValue) {
        if (oldValue != newValue) {
            this.firePropertyChange(propertyName, oldValue, newValue);
        }
        return newValue;
    }

    protected final int setProperty(String propertyName, int oldValue, int newValue) {
        if (oldValue != newValue) {
            this.firePropertyChange(propertyName, oldValue, newValue);
        }
        return newValue;
    }

    protected final <T> T setProperty(String propertyName, Object oldValue, T newValue) {
        if (!Objects.equals(oldValue, newValue)) {
            this.firePropertyChange(propertyName, oldValue, newValue);
        }
        return newValue;
    }

    protected final void firePropertyChange(PropertyChangeEvent event) {
        this.propertyChangeSupport.firePropertyChange(event);
    }

    protected final void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        this.propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    protected final void firePropertyChange(String propertyName, int oldValue, int newValue) {
        this.propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    protected final void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        this.propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
}

