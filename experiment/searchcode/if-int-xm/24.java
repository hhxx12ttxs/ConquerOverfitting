public class BaseFragment extends Fragment {
XmPluginBaseActivity mXmPluginBaseActivity;

@Override
int requestCode) {
if (mXmPluginBaseActivity != null) {
mXmPluginBaseActivity.startActivityForResult(intent, className, requestCode);
}
}
}

