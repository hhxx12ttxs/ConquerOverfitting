/**
 * gobandroid 
 * by Marcus -Ligi- Bueschleb 
 * http://ligi.de
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as 
 * published by the Free Software Foundation; 
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details. 
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 **/

package org.ligi.gobandroid_hd.logic;

import org.ligi.gobandroid_hd.logic.markers.GoMarker;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to represent a Go Move
 *
 * @author <a href="http://ligi.de">Marcus -LiGi- Bueschleb </a>
 *         <p/>
 *         This software is licenced with GPLv3
 */

public class GoMove {

    private byte x, y;

    private String comment = "";

    private boolean did_captures = false;

    private GoMove parent = null;

    private List<GoMove> next_move_variations;

    private List<GoMarker> markers;

    private int move_pos = 0;

    private boolean black_to_move = true;

    public GoMove(GoMove parent) {
        this.parent = parent;

        next_move_variations = new ArrayList<>();
        markers = new ArrayList<>();

        if (parent != null) {
            black_to_move = !parent.isBlackToMove();

            parent.addNextMove(this);

            GoMove act_move = this;

            while ((act_move != null) && (!act_move.isFirstMove())) {
                move_pos++;
                act_move = act_move.parent;
            }
        }
    }

    public GoMove(byte x, byte y, GoMove parent) {
        this(parent);

        this.x = x;
        this.y = y;
    }

    public void setDidCaptures(boolean did) {
        did_captures = did;
    }

    public boolean isCapturesMove() {
        return did_captures;
    }

    public int getMovePos() {
        return move_pos;
    }

    public boolean hasNextMove() {
        return (next_move_variations.size() > 0);
    }

    public boolean hasNextMoveVariations() {
        return (next_move_variations.size() > 1);
    }

    public int getNextMoveVariationCount() {
        return (next_move_variations.size() - 1);
    }

    public void addNextMove(GoMove move) {
        next_move_variations.add(move);
    }

    public void setToPassMove() {
        x = -1;
    }

    public boolean isPassMove() {
        return (x == -1);
    }

    public void setIsFirstMove() {
        x = -2;
    }

    public boolean isFirstMove() {
        return (x == -2);
    }

    public void setXY(byte x, byte y) {
        setX(x);
        setY(y);
    }

    public void setX(byte x) {
        this.x = x;
    }

    public void setY(byte y) {
        this.y = y;
    }

    public byte getX() {
        return x;
    }

    public byte getY() {
        return y;
    }

    public boolean isOnXY(byte x, byte y) {
        return ((getX() == x) && (getY() == y));
    }

    public GoMove getParent() {
        return parent;
    }

    public GoMove getnextMove(int pos) {
        return next_move_variations.get(pos);
    }

    public String toString() {
        return "" + x + " " + y;
    }

    public boolean hasComment() {
        return !comment.equals("");
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String newComment) {
        comment = newComment;
    }

    public void addComment(String newComment) {
        comment += newComment;
    }

    public boolean didCaptures() {
        return did_captures;
    }

    public List<GoMove> getNextMoveVariations() {
        return next_move_variations;
    }

    /**
     * @return the markers - e.g. from SGF Problems
     */
    public List<GoMarker> getMarkers() {
        return markers;
    }

    public void addMarker(GoMarker marker) {
        markers.add(marker);
    }

    public GoMarker getGoMarker() {
        for (GoMarker marker : parent.getMarkers()) {
            if ((this.getX() == marker.getX()) && (this.getY() == marker.getY())) {
                return marker;
            }
        }
        return null;
    }

    public boolean isMarked() {
        if (parent == null) {
            return false;
        }
        return (getGoMarker() != null);
    }

    public String getMarkText() {
        if (parent == null) {
            return "";
        }
        return (getGoMarker().getText());
    }

    public void destroy() {
        this.getParent().getNextMoveVariations().remove(this);
    }

    public boolean isBlackToMove() {
        return black_to_move;
    }

    public void setIsBlackToMove(boolean black_to_move) {
        this.black_to_move = black_to_move;
    }

    public boolean isContentEqual(GoMove other) {
        if (!other.isOnXY(x, y)) {
            return false;
        }

        if (!comment.equals(other.getComment())) {
            return false;
        }

        if (getMarkers().size() != other.getMarkers().size()) {
            return false;
        }

        for (GoMarker marker : getMarkers()) {
            if (!other.getMarkers().contains(marker)) {
                return false;
            }
        }

        if (isBlackToMove() != other.isBlackToMove()) {
            return false;
        }

        if (getMovePos() != getMovePos()) {
            return false;
        }

        // TODO check if we are complete

        return true;
    }
}
