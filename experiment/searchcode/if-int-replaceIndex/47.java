private Championship championship;
private EditText nameEdit;

private int replaceIndex;

@Override
protected void onCreate(Bundle savedInstanceState) {
championship = (Championship) intent
.getSerializableExtra(MainActivity.CHAMPIONSHIP_EXTRA);
replaceIndex = intent.getIntExtra(MainActivity.REPLACE_POSITION_EXTRA,

