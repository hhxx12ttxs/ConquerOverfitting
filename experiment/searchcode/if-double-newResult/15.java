literal.setValue( newValue);
double newResult = evaluate( context, literal, rhs);

if ( oldResult != newResult) parent.notifyChange( this, context, newResult, oldResult);
literal.setValue( newValue);
double newResult = evaluate( context, lhs, literal);

if ( oldResult != newResult) parent.notifyChange( this, context, newResult, oldResult);

