if (intent != null)
{
final String action = intent.getAction();

if (ACTION_CREATE_BRACKET.equals(action))
final BracketInput input = (BracketInput) intent.getSerializableExtra(EXTRA_BRACKET_INPUT);
handleCreateBracket(input);
}
else if (ACTION_DELETE_BRACKET.equals(action))

