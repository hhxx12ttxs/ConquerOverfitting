/**
 * Copyright (C) 2010 Cloudfarming <info@cloudfarming.nl>
 *
 * Licensed under the Eclipse Public License - v 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.cloudfarming.client.editors;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.ImageIcon;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 *
 * @author Timon Veenstra
 */
public class ImageIconEditor {
  
    private ImageIcon value;

    public void setValue(Object o) {
        this.value = (ImageIcon) o;
    }

    public Object getValue() {
        return value;
    }

    public boolean isPaintable() {
        return true;
    }

    public void paintValue(Graphics grphcs, Rectangle rctngl) {
        if (value != null) {
            int width = 0;
            int height = 0;
            
            double imageRatio = (double)value.getIconWidth()/(double)value.getIconHeight();
            double rctnglRatio = (double)rctngl.width/(double)rctngl.height;
            if (imageRatio<rctnglRatio){
                double factor = (double)rctngl.width/(double)value.getIconWidth();
                width=(int) ((double)value.getIconWidth()*factor);
                height=(int) ((double)value.getIconHeight()*factor);
            }else{
                double factor = (double)rctngl.height/(double)value.getIconHeight();
                width=(int) ((double)value.getIconWidth()*factor);
                height=(int) ((double)value.getIconHeight()*factor);                
            }
            
            grphcs.drawImage(value.getImage(), rctngl.x, rctngl.y, width, height, null);
        }
    }

    public String getJavaInitializationString() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getAsText() {
        return value == null ? "empty" : value.toString();
    }

    public void setAsText(String string) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String[] getTags() {
        return null;
    }

    public Component getCustomEditor() {
        return null;
    }

    public boolean supportsCustomEditor() {
        return false;
    }
    private final PropertyChangeSupport supp = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener pl) {
        supp.addPropertyChangeListener(pl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pl) {
        supp.removePropertyChangeListener(pl);
    }
    private PropertyEnv env;

    public void attachEnv(PropertyEnv pe) {
        env = pe;
    }  
}

