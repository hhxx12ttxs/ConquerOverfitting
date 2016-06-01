public class Secured extends Security.Authenticator {
@Override
public String getUsername(Http.Context ctx) {
// see if user is logged in
if (ctx.session().get(PortalConstants.SESSION_USERNAME) == null)
return null;

// see if the session is expired

