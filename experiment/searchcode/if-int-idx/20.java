super( a );
}

public void sort() {
int idx_of_min_val;
for(int idx=0; idx < array.length; idx++) {
idx_of_min_val = idx;
for(int run_idx = idx; run_idx < array.length; run_idx++) {
num_comparisons++;

