private static ILoginStrategy _loginStrategy;

private LoginFactory()
{

}

public static ILoginStrategy getLoginInstance()
{
if(_loginStrategy==null)
_loginStrategy = new RegaDBLoginStrategy();

return _loginStrategy;
}
}

