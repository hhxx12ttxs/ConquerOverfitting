@Table(name = &quot;seed&quot;, schema = &quot;&quot;, catalog = &quot;SeedDB&quot;)
public class SeedEntity {
private Long idSeed;
private String txSeed;
@Column(name = &quot;idSeed&quot;, nullable = false, insertable = true, updatable = true)
@GeneratedValue(strategy = GenerationType.IDENTITY)
public Long getIdSeed() {
return idSeed;

