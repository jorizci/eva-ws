/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2014-2016 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ebi.eva.server.ws;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.base.Splitter;
import org.opencb.biodata.models.feature.Genotype;
import org.opencb.biodata.models.variant.VariantSource;
import org.opencb.biodata.models.variant.VariantSourceEntry;
import org.opencb.biodata.models.variant.stats.VariantStats;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResponse;
import org.opencb.datastore.core.QueryResult;
import org.opencb.opencga.storage.core.variant.io.json.GenotypeJsonMixin;
import org.opencb.opencga.storage.core.variant.io.json.VariantSourceEntryJsonMixin;
import org.opencb.opencga.storage.core.variant.io.json.VariantSourceJsonMixin;
import org.opencb.opencga.storage.core.variant.io.json.VariantStatsJsonMixin;
import org.opencb.opencga.storage.core.variant.io.json.VariantStatsJsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import uk.ac.ebi.eva.lib.utils.DBAdaptorConnector;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EvaWSServer {

    protected final String version = "v1";

    @Autowired
    protected HttpServletRequest httpServletRequest;

    protected QueryOptions queryOptions;
    protected long startTime;
    protected long endTime;

    protected static Logger logger = LoggerFactory.getLogger(EvaWSServer.class);

    @Autowired
    protected DBAdaptorConnector dbAdaptorConnector;

    @Bean
    public Jackson2ObjectMapperBuilder jacksonBuilder() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder()
                       .mixIn(VariantSourceEntry.class, VariantSourceEntryJsonMixin.class)
                       .mixIn(Genotype.class, GenotypeJsonMixin.class)
                       .mixIn(VariantStats.class, VariantStatsJsonMixin.class)
                       .mixIn(VariantSource.class, VariantSourceJsonMixin.class)
                       .serializationInclusion(Include.NON_NULL);
        
        SimpleModule module = new SimpleModule();
        module.addSerializer(VariantStats.class, new VariantStatsJsonSerializer());
        builder.modules(module);
        
        return builder;
    }
    
    public EvaWSServer() { }

    protected void initializeQuery() {
        startTime = System.currentTimeMillis();
        initializeQueryOptions();
    }

    private void initializeQueryOptions() {
        this.queryOptions = new QueryOptions();
        Map<String, String[]> multivaluedMap = httpServletRequest.getParameterMap();

        boolean metadata = (multivaluedMap.get("metadata") != null) ? multivaluedMap.get("metadata")[0].equals("true") : true ;
        int limit = (multivaluedMap.get("limit") != null) ? Integer.parseInt(multivaluedMap.get("limit")[0]) : -1;
        int skip = (multivaluedMap.get("skip") != null) ? Integer.parseInt(multivaluedMap.get("skip")[0]) : -1;
        boolean count = (multivaluedMap.get("count") != null) ? multivaluedMap.get("count")[0].equals("true") : false ;

        String[] exclude = multivaluedMap.get("exclude");
        String[] include = multivaluedMap.get("include");

        queryOptions.put("metadata", metadata);
        queryOptions.put("exclude", (exclude != null && exclude.length > 0) ? Splitter.on(",").splitToList(exclude[0]) : null);
        queryOptions.put("include", (include != null && include.length > 0) ? Splitter.on(",").splitToList(include[0]) : null);
        queryOptions.put("limit", (limit > 0) ? limit : -1);
        queryOptions.put("skip", (skip > 0) ? skip : -1);
        queryOptions.put("count", count);
        logger.debug(queryOptions.toJson());
    }

    protected <T> QueryResponse<T> setQueryResponse(T obj) {
        QueryResponse<T> queryResponse = buildQueryResponse();

        List<T> coll = new ArrayList<>();
        coll.add(obj);
        queryResponse.setResponse(coll);

        return queryResponse;
    }

    protected <T> QueryResponse<T> setErrorQueryResponse(String message) {
        QueryResponse<T> queryResponse = buildQueryResponse();

        queryResponse.setResponse(Collections.EMPTY_LIST);
        queryResponse.setError(message);
        return queryResponse;
    }

    private <T> QueryResponse<T> buildQueryResponse() {
        QueryResponse<T> queryResponse = new QueryResponse<>();
        endTime = System.currentTimeMillis();
        queryResponse.setApiVersion(version);
        queryResponse.setQueryOptions(queryOptions);

        // TODO why the QueryResponse.time is null when the tests get the QueryResponse from the WS? because it's a native int?
        queryResponse.setTime(new Long(endTime - startTime).intValue());
        return queryResponse;
    }

    protected <T> QueryResult<T> buildQueryResult(List<T> results) {
        return buildQueryResult(results, results.size());
    }

    protected <T> QueryResult<T> buildQueryResult(List<T> results, long numTotalResults) {
        QueryResult<T> queryResult = new QueryResult<>();
        queryResult.setResult(results);
        queryResult.setNumResults(results.size());
        queryResult.setNumTotalResults(numTotalResults);
        queryResult.setDbTime(new Long(System.currentTimeMillis() - startTime).intValue());
        return queryResult;
    }
}
