public Response getDrainages( @PathParam(&quot;emdat&quot;) String emdat,  @PathParam(&quot;fromYear&quot;) int fromYear, @PathParam(&quot;toYear&quot;) int toYear, @PathParam(&quot;iso3&quot;) String iso3){
int countryId = CountryService.get().getId(iso3);

