// This is a sequential search which tells you whether or not an element exists in an array, however this is written in mini java 
class SequentialSearchMini{

    public static void main(String[] a){
    System.out.println((new SequentialSearch().init()).search());
    }
}


// Declare the array itself and a variable for holding the size
class SequentialSearch {
    int[] data ;
    int size ;
    
    // Calculate the size of the array
    public int getLength()
    {
        return data.length ;    
       
    }
    
    
    // Define the values of the array
    public SequentialSearch init()
    {
    int index; 
    
    // define all elements in the aray, initialize
    data = new int[8];
    data[0] = 6;
    data[1] = 4;
    data[2] = 9;
    data[3] = 14;
    data[4] = 21;
    data [5] = 2;
    data [6] = 0;
    data [7] = 11;
    return this;
    
    }
    
    // Input the number we are searching the array for
    public int search()
    {
        return this.Search((0-7));
        
    }
    
    // Search for the value "num" defined above, also contains the loop for finding the element
    public int Search(int num){
    
    // Declare variables for couting, comparing elements and keeping track if the variable is found or not
    int j ; 
    int isfound ;
    int element1 ;
    int element2 ;
    
    // Gets the size of the arry used in the while loop below
    size = this.getLength();
    
    // J is a counter, found is set as "false" 
    j = 0 ;
    isfound = 0 ;
    
    // Print the number you are searching the array for
    System.out.println(num);
    
    
    // Loop for comparing elements in the array to the number we are searching for
	
    while (j < (size)) {
        // j goes through elements in array 
        element1 = data[j] ;
        // element 2 is set as one greater than the num this is used further down
        element2 = num + 1 ;
        // element 1 doesn't match because its less than "num"
        if (element1 < num) isfound = 0 ;
        // element 2 is 1 greater than num, we can't use > in javamini, becuase of this we say that if element 1 is not greater than element 2 (which is 1 greater than the num) then num is not found
        else if (!(element1 < element2)) isfound = 0 ;
        // if element 1 is not < element 2 (which is one greater than num) is found = 0 
        // becuase == is not allowed and neither is > this is a work around, for example num = 9, element 2 = 10, in our case element 1 is 6 which is < 9 so isfound = 0 and j is incremented
        // 4 is < 9 so j is incremented, 9 is not < 9 so the 2nd statement occurs, 9 < 10 is true so the second statement is skipped and the final else is executed which means we have found our element.
        // If for instance the next element was 10 the first if statement is skipped and 10 !< 10 which is true so isfound = 0. This is a very weird way of using ! and < to the same effect of > and ==.
        else {
        // the value is found and isfound = 1     
        isfound = 1 ;
        j = size ;
        }
        // increment j to go through the next element in list
        j = j + 1 ;
    }
    
    // if isfound = 0 the number wasn't found, if isfound = 1 then the number was found
    return isfound ;
    }
    
}


    


