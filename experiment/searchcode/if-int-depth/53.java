boolean finished = false ;

backtrack(int a[], int depth) {
if(is_solution(a,depth)) {
process_solution(a,depth) ;
finished = true ;
}
else {
List children = construct_children(a,depth) ;

