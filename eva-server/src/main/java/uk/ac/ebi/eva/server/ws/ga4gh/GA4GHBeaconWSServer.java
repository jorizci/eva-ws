/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2014-2017 EMBL - European Bioinformatics Institute
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

package uk.ac.ebi.eva.server.ws.ga4gh;

import io.swagger.annotations.Api;
import org.opencb.biodata.models.feature.Region;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.ebi.eva.lib.filter.Helpers;
import uk.ac.ebi.eva.lib.filter.VariantEntityRepositoryFilter;
import uk.ac.ebi.eva.lib.repository.VariantEntityRepository;
import uk.ac.ebi.eva.lib.utils.DBAdaptorConnector;
import uk.ac.ebi.eva.lib.utils.MultiMongoDbFactory;
import uk.ac.ebi.eva.server.ws.EvaWSServer;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/v1/ga4gh", produces = "application/json")
@Api(tags = { "ga4gh" })
public class GA4GHBeaconWSServer extends EvaWSServer {

    @Autowired
    private VariantEntityRepository variantEntityRepository;

    protected static Logger logger = LoggerFactory.getLogger(GA4GHBeaconWSServer.class);

    public GA4GHBeaconWSServer() { }
    
    @RequestMapping(value = "/beacon", method = RequestMethod.GET)
    public GA4GHBeaconResponse beacon(@RequestParam("referenceName") String chromosome,
                                      @RequestParam("start") int start,
                                      @RequestParam("allele") String allele,
                                      @RequestParam("datasetIds") List<String> studies,
                                      HttpServletResponse response) 
            throws UnknownHostException, IllegalOpenCGACredentialsException, IOException {
        initializeQuery();
        
        if (start < 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return new GA4GHBeaconResponse(chromosome, start, allele, String.join(",", studies),
                    "Please provide a positive number as start position");
        }

        MultiMongoDbFactory.setDatabaseNameForCurrentThread(DBAdaptorConnector.getDBName("hsapiens_grch37"));

        long totalCount;
        if (allele.equalsIgnoreCase("INDEL")) {
            totalCount = variantEntityRepository.countByChromosomeAndStartAndTypeAndStudyIn(chromosome, start,
                                                                                            Variant.VariantType.INDEL,
                                                                                            studies);
        } else {
            totalCount = variantEntityRepository.countByChromosomeAndStartAndEndAndAltAndStudyIn(chromosome, start,
                                                                                                 allele, studies);
        }

        return new GA4GHBeaconResponse(chromosome, start, allele, String.join(",", studies), totalCount > 0);
    }
    
    class GA4GHBeaconResponse {
        
        private String chromosome;
        
        private Integer start;
        
        private String allele;
        
        private String datasetIds;
        
        private boolean exists;
        
        private String errorMessage;

        public GA4GHBeaconResponse(String chromosome, Integer start, String allele, String datasetIds, boolean exists) {
            this.chromosome = chromosome;
            this.start = start;
            this.allele = allele;
            this.datasetIds = datasetIds;
            this.exists = exists;
        }

        public GA4GHBeaconResponse(String chromosome, Integer start, String allele, String datasetIds, String errorMessage) {
            this.chromosome = chromosome;
            this.start = start;
            this.allele = allele;
            this.datasetIds = datasetIds;
            this.errorMessage = errorMessage;
        }

        public String getChromosome() {
            return chromosome;
        }

        public Integer getStart() {
            return start;
        }

        public String getAllele() {
            return allele;
        }

        public String getDatasetIds() {
            return datasetIds;
        }

        public boolean isExists() {
            return exists;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
    
}
