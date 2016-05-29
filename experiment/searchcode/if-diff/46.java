mDiffList = getDiffEntities();
}

@Override
public DiffEntity get(int position) {
if(mDiffList == null) {
mDiffList = getDiffEntities();
}

return mDiffList.get(position);
}

@Override
public int size() {

if(mDiffList == null) {

