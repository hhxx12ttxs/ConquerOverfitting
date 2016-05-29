boolean exists = elasticSearchClient.admin().indices().prepareExists(&quot;countries&quot;).execute().actionGet().isExists();
if (exists) {
elasticSearchClient.admin().indices().preparePutMapping(&quot;countries&quot;).setType(&quot;H&quot;).setSource(xb).execute().actionGet();
if (!putMappingResponse.isAcknowledged()) {

