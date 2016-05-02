public class Year extends CalendarUnit {
        
        public static final int MIN = 1801;
        public static final int MAX = 2021;
        
        public Year(int year) {
                if(year < MIN || year > MAX)
                        throw new IllegalArgumentException("invalid year of " + year);
                
                setCurrentPos(year);
        }
        
        public int getYear() {
                return currentPos;
        }
        
        public boolean increment() {
                currentPos++;
                return true;
        }
        
        public boolean isLeap() {
                return (currentPos % 4 == 0 && currentPos % 400 != 0) || currentPos % 400 == 0;
        }
        
        public String toString() {
                return "" + getYear();
        }
}
