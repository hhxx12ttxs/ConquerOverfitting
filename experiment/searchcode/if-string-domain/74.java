this.domain = domain;
}

public DomainPart(String domain) {
this(new Domain(domain));
}

@Override
public String smtpText() {
return domain.smtpText();
}

@Override

