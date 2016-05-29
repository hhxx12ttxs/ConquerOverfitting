private ViewPagerAdapter fragmentPagerAdapter = null;
private int previoustPage = 0;
private TabLayout tabLayout = null;
private static Fragment instance = null;
RecyclerView recyclerView = (RecyclerView) getActivity().findViewById(R.id.recyclerView);
if(recyclerView != null) {
recyclerView.scrollToPosition(0);

