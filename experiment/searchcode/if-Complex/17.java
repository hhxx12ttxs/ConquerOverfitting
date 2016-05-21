    private final ImageSimplifier imageSimplifier;
    private final DescriptionWatermarker descriptionWatermarker;
    private final DescribedImageExtractor imageExtractor = new DescribedImageExtractor();
    private final PublisherSimplifier publisherSimplifier = new PublisherSimplifier();
    private final RatingModelSimplifier ratingModelSimplifier = new RatingModelSimplifier();
    private final AudienceStatisticsModelSimplifier audienceStatsModelSimplifier = new AudienceStatisticsModelSimplifier();
    protected DescribedModelSimplifier(ImageSimplifier imageSimplifier) {
        this.imageSimplifier = imageSimplifier;
                if (complex.getLocale() != null) {
                    simple.setLanguage(complex.getLocale().toLanguageTag());
public abstract class DescribedModelSimplifier<F extends Described, T extends Description> extends IdentifiedModelSimplifier<F,T> {

