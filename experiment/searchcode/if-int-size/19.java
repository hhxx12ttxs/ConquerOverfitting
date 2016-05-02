package org.crank.crud.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.crank.crud.controller.Paginator;
import org.crank.crud.controller.datasource.PagingDataSource;
import org.crank.crud.controller.datasource.SimplePagingDataSource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import junit.framework.TestCase;

@SuppressWarnings("unchecked")
public class PaginatorTest extends TestCase {
    Paginator paginator ;

    @BeforeMethod
    protected void setUp() throws Exception {
        setupPaginator(107);
    }

    private void setupPaginator(int size) {
        List<String> list = new ArrayList<String>();
        for (int index = 0; index < size; index++) {
            String string = "" + index;
            list.add( string );
        }
        PagingDataSource dataSource = new SimplePagingDataSource(list);
        Paginator paginatorImpl = new Paginator(dataSource);
        //paginatorImpl.setDataSource( dataSource );
        paginator = paginatorImpl;
        paginator.reset();
    }

    @Test
    public void testGetList() {
        List<String> list = paginator.getPage();
        assertEquals( 10, list.size() );
        showList(list);
        assertEquals( "0", list.get(0));
        assertEquals( "9", list.get(9));
    }

    @Test
    public void testNext() {
        paginator.moveToNextPage();
        List<String> list = paginator.getPage();
        assertEquals( 10, list.size() );
        showList(list);
        assertEquals( "10", list.get(0));
        assertEquals( "19", list.get(9));
    }

    @Test
    public void testFastForwardNext() {
        paginator.fastForwardPages();
        List<String> list = paginator.getPage();
        assertEquals( 10, list.size() );
        showList(list);
        assertEquals( "50", list.get(0));
        assertEquals( "59", list.get(9));
    }

    @Test
    public void testPrevious() {
        paginator.fastForwardPages();
        paginator.moveToPreviousPage();
        List<String> list = paginator.getPage();
        assertEquals( 10, list.size() );
        showList(list);
        assertEquals( "40", list.get(0));
        assertEquals( "49", list.get(9));
    }

    @Test
    public void testRewind() {
        paginator.fastForwardPages();
        paginator.moveToNextPage();
        paginator.rewindPages();
        List<String> list = paginator.getPage();
        assertEquals( 10, list.size() );
        showList(list);
        assertEquals( "10", list.get(0));
        assertEquals( "19", list.get(9));
    }
    
    @Test
    public void testEnabled() {
        assertTrue(paginator.isMoveToNextPageEnabled());
        assertTrue(paginator.isFastForwardPagesEnabled());
        assertFalse(paginator.isMoveToPreviousPageEnabled());
        assertFalse(paginator.isRewindPagesEnabled());
        paginator.moveToNextPage(); //1
        List<String> list = paginator.getPage();
        assertTrue(paginator.isMoveToNextPageEnabled());
        assertTrue(paginator.isFastForwardPagesEnabled());
        assertTrue(paginator.isMoveToPreviousPageEnabled());
        assertFalse(paginator.isRewindPagesEnabled());
        paginator.moveToNextPage(); //2
        paginator.moveToNextPage(); //3
        paginator.moveToNextPage(); //4
        list = paginator.getPage();
        assertTrue(paginator.isMoveToNextPageEnabled());
        assertTrue(paginator.isFastForwardPagesEnabled());
        assertTrue(paginator.isMoveToPreviousPageEnabled());
        assertFalse(paginator.isRewindPagesEnabled());
        paginator.moveToNextPage(); //5
        list = paginator.getPage();
        assertTrue(paginator.isMoveToNextPageEnabled());
        assertTrue(paginator.isFastForwardPagesEnabled());
        assertTrue(paginator.isMoveToPreviousPageEnabled());
        assertTrue(paginator.isRewindPagesEnabled()); //now true
        paginator.moveToNextPage(); //6
        list = paginator.getPage();
        assertTrue(paginator.isMoveToNextPageEnabled());
        assertFalse(paginator.isFastForwardPagesEnabled()); // now false
        assertTrue(paginator.isMoveToPreviousPageEnabled());
        assertTrue(paginator.isRewindPagesEnabled()); //still true
        paginator.moveToNextPage(); //7
        paginator.moveToNextPage(); //8
        list = paginator.getPage();
        assertTrue(paginator.isMoveToNextPageEnabled());
        assertFalse(paginator.isFastForwardPagesEnabled()); // still false
        assertTrue(paginator.isMoveToPreviousPageEnabled());
        assertTrue(paginator.isRewindPagesEnabled()); //still true
        paginator.moveToNextPage(); //9
        list = paginator.getPage();
        assertTrue(paginator.isMoveToNextPageEnabled());
        assertFalse(paginator.isFastForwardPagesEnabled()); // still false
        assertTrue(paginator.isMoveToPreviousPageEnabled());
        assertTrue(paginator.isRewindPagesEnabled()); //still true
        paginator.moveToNextPage(); //10
        list = paginator.getPage();
        assertFalse(paginator.isMoveToNextPageEnabled()); //NOW FALSE
        assertFalse(paginator.isFastForwardPagesEnabled()); 
        assertTrue(paginator.isMoveToPreviousPageEnabled());
        assertTrue(paginator.isRewindPagesEnabled());
        paginator.moveToPreviousPage(); //9
        list = paginator.getPage();
        assertTrue(paginator.isMoveToNextPageEnabled()); //NOW TRUE AGAIN
        assertFalse(paginator.isFastForwardPagesEnabled()); 
        assertTrue(paginator.isMoveToPreviousPageEnabled());
        assertTrue(paginator.isRewindPagesEnabled());
        assertEquals( 11, paginator.getNumberOfPages() );
        list = paginator.getPage();
        list.size();
    }
    
    @Test
    public void testWindow() {
        setupPaginator(1000);
        advanceByPages( 10 );
        List<Integer> list = paginator.getPageNumberList();
        paginator.getPage();
        assertEquals((int)6, (int)list.get( 0 ));
        assertEquals((int)15, (int)list.get( 9 ));

        advanceByPages( 50 );
        list = paginator.getPageNumberList();
        assertUniqueList(list);
        paginator.getPage();
        assertEquals((int)56, (int)list.get( 0 ));
        assertEquals((int)65, (int)list.get( 9 ));
        
        advanceByPages( 20 );
        list = paginator.getPageNumberList();
        assertUniqueList(list);
        paginator.getPage();
        assertEquals((int)76, (int)list.get( 0 ));
        assertEquals((int)85, (int)list.get( 9 ));
        
        advanceByPages( 20 );
        list = paginator.getPageNumberList();
        assertUniqueList(list);        
        paginator.getPage();
        assertEquals((int)96, (int)list.get( 0 ));
        assertEquals((int)100, (int)list.get( 4 ));
        
        advanceByPages( 20 );
        list = paginator.getPageNumberList();
        assertUniqueList(list);        
        paginator.getPage();
        assertEquals((int)96, (int)list.get( 0 ));
        assertEquals((int)100, (int)list.get( 4 ));

        
        rewindPageBy( 25 );
        list = paginator.getPageNumberList();
        assertUniqueList(list);        
        paginator.getPage();
        assertEquals((int)71, (int)list.get( 0 ));
        assertEquals((int)80, (int)list.get( 9 ));

        
        rewindPageBy( 25 );
        list = paginator.getPageNumberList();
        assertUniqueList(list);        
        paginator.getPage();
        assertEquals((int)46, (int)list.get( 0 ));
        assertEquals((int)55, (int)list.get( 9 ));

        rewindPageBy( 25 );
        list = paginator.getPageNumberList();
        assertUniqueList(list);        
        paginator.getPage();
        assertEquals((int)21, (int)list.get( 0 ));
        assertEquals((int)30, (int)list.get( 9 ));

        rewindPageBy( 25 );
        list = paginator.getPageNumberList();
        assertUniqueList(list);        
        paginator.getPage();        
        assertEquals((int)1, (int)list.get( 0 ));
        assertEquals((int)10, (int)list.get( 9 ));

        rewindPageBy( 25 );
        list = paginator.getPageNumberList();
        assertUniqueList(list);       
        paginator.getPage();        
        assertEquals((int)1, (int)list.get( 0 ));
        assertEquals((int)10, (int)list.get( 9 ));

        rewindPageBy( 25 );
        list = paginator.getPageNumberList();
        assertUniqueList(list);        
        paginator.getPage();        
        assertEquals((int)1, (int)list.get( 0 ));
        assertEquals((int)10, (int)list.get( 9 ));
        
        paginator.moveToPage( 50 );
        list = paginator.getPageNumberList();
        assertUniqueList(list);        
        paginator.getPage();        
        assertEquals((int)46, (int)list.get( 0 ));
        assertEquals((int)55, (int)list.get( 9 ));
        
        paginator.moveToPage( 69 );
        list = paginator.getPageNumberList();
        assertUniqueList(list);        
        paginator.getPage();        
        assertEquals((int)65, (int)list.get( 0 ));
        assertEquals((int)74, (int)list.get( 9 ));
        
        paginator.moveToEndPage();
        paginator.moveToStartPage();
        paginator.moveToEndPage();
        paginator.moveToPreviousPage();
        assertUniqueList(paginator.getPageNumberList());        
        paginator.moveToPreviousPage();
        assertUniqueList(paginator.getPageNumberList());        
        paginator.moveToPreviousPage();
        assertUniqueList(paginator.getPageNumberList());        
        paginator.moveToPreviousPage();
        assertUniqueList(paginator.getPageNumberList());        
        paginator.moveToPreviousPage();
        assertUniqueList(paginator.getPageNumberList());        
        paginator.moveToPreviousPage();
        assertUniqueList(paginator.getPageNumberList());        
        paginator.moveToPreviousPage();
        assertUniqueList(paginator.getPageNumberList());        

    }
    
    private void assertUniqueList( List<Integer> list ) {
        Set<Integer> set = new HashSet<Integer>(list);
        if (list.size() != set.size()) {
            throw new RuntimeException("Items not unique");
        }
    }

    private void advanceByPages(int pageNum) {
        for (int index = 0; index < pageNum; index++) {
            paginator.moveToNextPage();
        }
    }
    private void rewindPageBy(int pageNum) {
        for (int index = 0; index < pageNum; index++) {
            paginator.moveToPreviousPage();
        }
    }

    private void showList( List<String> list ) {
//        for (String string : list) {
//            //System.out.println(string);
//        }
//        
    }
}

