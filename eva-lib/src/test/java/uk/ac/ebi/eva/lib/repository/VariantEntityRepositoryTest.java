/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2016 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.eva.lib.repository;

import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencb.biodata.models.feature.Region;
import org.opencb.biodata.models.variant.VariantSourceEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.ac.ebi.eva.commons.models.metadata.VariantEntity;
import uk.ac.ebi.eva.lib.configuration.MongoRepositoryTestConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for VariantEntityRepository
 *
 * Uses in memory Mongo database spoof Fongo, and loading data from json using lordofthejars nosqlunit.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MongoRepositoryTestConfiguration.class})
@UsingDataSet(locations = {"/test-data/variants.json"})
public class VariantEntityRepositoryTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("test-db");

    @Autowired
    private VariantEntityRepository variantEntityRepository;

    @Test
    public void checkFieldPresence() throws IOException {

        List<Region> regions = new ArrayList<>();
        regions.add(new Region("11", 183000, 183300));

        List<String> exclude = new ArrayList<>();

        List<VariantEntity> variantEntityList = variantEntityRepository
                .findByRegionsAndComplexFilters(regions, null, null,
                                                VariantEntityRepository.RelationalOperator.NONE,
                                                null,
                                                VariantEntityRepository.RelationalOperator.NONE,
                                                null,
                                                VariantEntityRepository.RelationalOperator.NONE,
                                                null, exclude,
                                                new PageRequest(0, 100000000));

        for (VariantEntity currVariantEntity : variantEntityList) {
            assertFalse(currVariantEntity.getSourceEntries().isEmpty());
            assertFalse(currVariantEntity.getIds().isEmpty());
            for (Map.Entry<String, VariantSourceEntry> sourceEntryEntry :
                    currVariantEntity.getSourceEntries().entrySet()){
                assertFalse(sourceEntryEntry.getValue().getAttributes().isEmpty());
            }
        }

    }

    @Test
    public void testVariantIdIsFound(){
        String id = "rs776523794";
        List<VariantEntity> variantEntityList =
                variantEntityRepository.findByIdsAndComplexFilters(id, null, null, null, null, null, null, null, null,
                                                                   new ArrayList<>(), null);
        assertNotNull(variantEntityList);
        assertTrue(variantEntityList.size() > 0);
        Set<String> idSet = new HashSet<>();
        idSet.add(id);
        idSet.add("ss664037839");
        assertEquals(idSet, variantEntityList.get(0).getIds());
    }

    @Test
    public void testCountByIdsAndComplexFilters(){
        String id = "rs776523794";
        Long count =
                variantEntityRepository.countByIdsAndComplexFilters(id, null, null, null, null, null, null, null, null);
        assertEquals(new Long(1), count);
    }

    @Test
    public void testNonExistentVariantIdIsNotFound(){
        String id = "notarealid";
        List<VariantEntity> variantEntityList =
                variantEntityRepository.findByIdsAndComplexFilters(id, new ArrayList<>(), new ArrayList<>(), null, null,
                                                                   null, null, null, null, new ArrayList<>(), null);
        assertNotNull(variantEntityList);
        assertTrue(variantEntityList.size() == 0);
    }

    @Test
    public void testVariantRegionIsFound(){
        String chr = "11";
        int start = 180002;
        int end = 180002;
        Region region = new Region(chr, start, end);
        List<Region> regions = new ArrayList<>();
        regions.add(region);
        List<VariantEntity> variantEntityList =
                variantEntityRepository.findByRegionsAndComplexFilters(regions, new ArrayList<>(), new ArrayList<>(), null, null,
                                                                       null, null, null, null, new ArrayList<>(),
                                                                       new PageRequest(0, 1000000));
        assertNotNull(variantEntityList);
        assertTrue(variantEntityList.size() > 0);
        assertEquals(chr, variantEntityList.get(0).getChromosome());
        assertEquals(start, variantEntityList.get(0).getStart());
        assertEquals(end, variantEntityList.get(0).getStart());
    }

    @Test
    public void testVariantRegionIsFoundMultiple(){
        String chr = "11";
        int start = 185000;
        int end = 190000;
        Region region = new Region(chr, start, end);
        List<Region> regions = new ArrayList<>();
        regions.add(region);
        List<VariantEntity> variantEntityList =
                variantEntityRepository.findByRegionsAndComplexFilters(regions, new ArrayList<>(), new ArrayList<>(), null, null,
                                                                       null, null, null, null, new ArrayList<>(),
                                                                       new PageRequest(0, 1000000));
        assertNotNull(variantEntityList);
        assertTrue(variantEntityList.size() > 0);
        assertEquals(309, variantEntityList.size());
        VariantEntity prevVariantEntity = variantEntityList.get(0);
        for (VariantEntity currVariantEntity : variantEntityList) {
            assertTrue(prevVariantEntity.getStart() <= currVariantEntity.getStart());
        }
    }

    @Test
    public void testCountByRegionsAndComplexFilters(){
        String chr = "11";
        int start = 185000;
        int end = 190000;
        Region region = new Region(chr, start, end);
        List<Region> regions = new ArrayList<>();
        regions.add(region);
        Long count = variantEntityRepository.countByRegionsAndComplexFilters(regions, new ArrayList<>(),
                                                                             new ArrayList<>(), null, null, null, null,
                                                                             null, null);
        assertEquals(new Long(309), count);
    }

    @Test
    public void testNonExistentVariantRegionIsNotFound(){
        String chr = "11";
        int start = 61098;
        int end = 60916;
        Region region = new Region(chr, start, end);
        List<Region> regions = new ArrayList<>();
        regions.add(region);
        List<VariantEntity> variantEntityList =
                variantEntityRepository.findByRegionsAndComplexFilters(regions, new ArrayList<>(), new ArrayList<>(), null, null,
                                                                       null, null, null, null, new ArrayList<>(),
                                                                       new PageRequest(0, 1000000));
        assertNotNull(variantEntityList);
        assertTrue(variantEntityList.size() == 0);
    }

    @Test
    public void testRegionIsFoundWithConsequenceType() {
        List<String> cts = new ArrayList<>();
        cts.add("SO:0001627");
        String chr = "11";
        int start = 188000;
        int end = 190000;
        Region region = new Region(chr, start, end);
        List<Region> regions = new ArrayList<>();
        regions.add(region);
        testFiltersHelperRegion(regions, new ArrayList<>(), cts, null, null, null, null, null, null, new ArrayList<>(), 94);
    }

    @Test
    public void testRegionIsFoundWithMafGreaterThan() {
        String chr = "11";
        int start = 185000;
        int end = 190000;
        Region region = new Region(chr, start, end);
        List<Region> regions = new ArrayList<>();
        regions.add(region);
        testFiltersHelperRegion(regions, new ArrayList<>(), new ArrayList<>(),
                                VariantEntityRepository.RelationalOperator.GT, 0.125, null, null, null, null,
                                new ArrayList<>(), 37);
    }

    @Test
    public void testRegionIsFoundWithMafGreaterThanEquals() {
        String chr = "11";
        int start = 189000;
        int end = 190000;
        Region region = new Region(chr, start, end);
        List<Region> regions = new ArrayList<>();
        regions.add(region);
        testFiltersHelperRegion(regions, new ArrayList<>(), new ArrayList<>(),
                                VariantEntityRepository.RelationalOperator.GTE, 0.125, null, null, null, null,
                                new ArrayList<>(), 15);
    }

    @Test
    public void testRegionIsFoundWithMafEquals() {
        String chr = "11";
        int start = 185000;
        int end = 190000;
        Region region = new Region(chr, start, end);
        List<Region> regions = new ArrayList<>();
        regions.add(region);
        testFiltersHelperRegion(regions, new ArrayList<>(), new ArrayList<>(),
                                VariantEntityRepository.RelationalOperator.EQ, 0.5, null, null, null, null,
                                new ArrayList<>(), 8);
    }

    @Test
    public void testRegionIsFoundWithPolyphenGreaterThan() {
        String chr = "11";
        int start = 190000;
        int end = 193719;
        Region region = new Region(chr, start, end);
        List<Region> regions = new ArrayList<>();
        regions.add(region);
        testFiltersHelperRegion(regions, new ArrayList<>(), new ArrayList<>(), null, null,
                                VariantEntityRepository.RelationalOperator.GT, 0.5, null, null, new ArrayList<>(), 4);
    }

    @Test
    public void testRegionIsFoundWithSiftLessThan() {
        String chr = "11";
        int start = 190000;
        int end = 193719;
        Region region = new Region(chr, start, end);
        List<Region> regions = new ArrayList<>();
        regions.add(region);
        testFiltersHelperRegion(regions, new ArrayList<>(), new ArrayList<>(), null, null, null, null,
                                VariantEntityRepository.RelationalOperator.LT, 0.5, new ArrayList<>(), 11);
    }

    @Test
    public void testRegionIsFoundWithStudies() {
        List<String> studies = new ArrayList<>();
        studies.add("PRJEB6930");
        String chr = "11";
        int start = 190000;
        int end = 191000;
        Region region = new Region(chr, start, end);
        List<Region> regions = new ArrayList<>();
        regions.add(region);
        testFiltersHelperRegion(regions, studies, new ArrayList<>(), null, null, null, null, null, null,
                                new ArrayList<>(), 14);
    }

    @Test
    public void testFindByRegionsAndComplexFilters() {

        List<Region> regions = new ArrayList<>();
        regions.add(new Region("11", 183000, 183300));
        regions.add(new Region("11", 180100, 180200));
        regions.add(new Region("11", 190000, 190200));

        testFindByRegionsAndComplexFiltersHelper(regions, null, null, VariantEntityRepository.RelationalOperator.NONE,
                                                 null, VariantEntityRepository.RelationalOperator.NONE, null,
                                                 VariantEntityRepository.RelationalOperator.NONE, null, null, 28);

        regions = new ArrayList<>();
        regions.add(new Region("11", 180001, 180079)); //4

        testFindByRegionsAndComplexFiltersHelper(regions, null, null, VariantEntityRepository.RelationalOperator.NONE,
                                                 null, VariantEntityRepository.RelationalOperator.NONE, null,
                                                 VariantEntityRepository.RelationalOperator.NONE, null, null, 4);

        regions.add(new Region("11", 180150, 180180)); //5

        testFindByRegionsAndComplexFiltersHelper(regions, null, null, VariantEntityRepository.RelationalOperator.NONE,
                                                 null, VariantEntityRepository.RelationalOperator.NONE, null,
                                                 VariantEntityRepository.RelationalOperator.NONE, null, null, 9);

        regions.add(new Region("11", 180205, 180221)); //2

        testFindByRegionsAndComplexFiltersHelper(regions, null, null, VariantEntityRepository.RelationalOperator.NONE,
                                                 null, VariantEntityRepository.RelationalOperator.NONE, null,
                                                 VariantEntityRepository.RelationalOperator.NONE, null, null, 11);
    }

    @Test
    public void testFindByRegionsAndComplexFiltersExcludeSingleRoot() {
        List<Region> regions = new ArrayList<>();
        regions.add(new Region("11", 183000, 183300));

        List<String> exclude = new ArrayList<>();
        exclude.add("files");

        List<VariantEntity> variantEntityList =
                variantEntityRepository.findByRegionsAndComplexFilters(regions, null, null,
                                                                       VariantEntityRepository.RelationalOperator.NONE,
                                                                       null,
                                                                       VariantEntityRepository.RelationalOperator.NONE,
                                                                       null,
                                                                       VariantEntityRepository.RelationalOperator.NONE,
                                                                       null, exclude,
                                                                       new PageRequest(0, 100000000));

        for (VariantEntity currVariantEntity : variantEntityList) {
            assertTrue(currVariantEntity.getSourceEntries().isEmpty());
        }
    }

    @Test
    public void testFindByRegionsAndComplexFiltersExcludeMultiple() {
        List<Region> regions = new ArrayList<>();
        regions.add(new Region("11", 183000, 183300));

        List<String> exclude = new ArrayList<>();
        exclude.add("files");
        exclude.add("ids");

        List<VariantEntity> variantEntityList =
                variantEntityRepository.findByRegionsAndComplexFilters(regions, null, null,
                                                                       VariantEntityRepository.RelationalOperator.NONE,
                                                                       null,
                                                                       VariantEntityRepository.RelationalOperator.NONE,
                                                                       null,
                                                                       VariantEntityRepository.RelationalOperator.NONE,
                                                                       null, exclude,
                                                                       new PageRequest(0, 100000000));

        for (VariantEntity currVariantEntity : variantEntityList) {
            assertTrue(currVariantEntity.getSourceEntries().isEmpty());
            assertTrue(currVariantEntity.getIds().isEmpty());
        }
    }

    @Test
    public void testFindByRegionsAndComplexFiltersExcludeNested() {
        List<Region> regions = new ArrayList<>();
        regions.add(new Region("11", 183000, 183300));

        List<String> exclude = new ArrayList<>();
        exclude.add("files.attrs");

        List<VariantEntity> variantEntityList =
                variantEntityRepository.findByRegionsAndComplexFilters(regions, null, null,
                                                                       VariantEntityRepository.RelationalOperator.NONE,
                                                                       null,
                                                                       VariantEntityRepository.RelationalOperator.NONE,
                                                                       null,
                                                                       VariantEntityRepository.RelationalOperator.NONE,
                                                                       null, exclude,
                                                                       new PageRequest(0, 100000000));

        for (VariantEntity currVariantEntity : variantEntityList) {
            for (Map.Entry<String, VariantSourceEntry> sourceEntry : currVariantEntity.getSourceEntries().entrySet()){
                assertTrue(sourceEntry.getValue().getAttributes().isEmpty());
            }
        }
    }

    private void testFiltersHelperRegion(List<Region> regions, List<String> studies, List<String> consequenceType,
                                         VariantEntityRepository.RelationalOperator mafOperator, Double mafValue,
                                         VariantEntityRepository.RelationalOperator polyphenOperator, Double polyphenValue,
                                         VariantEntityRepository.RelationalOperator siftOperator, Double siftValue,
                                         List<String> exclude, int expectedResultLength) {
        List<VariantEntity> variantEntityList =
                variantEntityRepository.findByRegionsAndComplexFilters(regions, studies, consequenceType, mafOperator,
                                                                       mafValue, polyphenOperator, polyphenValue,
                                                                       siftOperator, siftValue, exclude,
                                                                       new PageRequest(0, 10000));
        assertNotNull(variantEntityList);
        assertEquals(expectedResultLength, variantEntityList.size());
    }

    private void testFindByRegionsAndComplexFiltersHelper(List<Region> regions, List<String> studies,
                                                          List<String> consequenceType,
                                                          VariantEntityRepository.RelationalOperator mafOperator,
                                                          Double mafValue,
                                                          VariantEntityRepository.RelationalOperator polyphenOperator,
                                                          Double polyphenValue,
                                                          VariantEntityRepository.RelationalOperator siftOperator,
                                                          Double siftValue,
                                                          List<String> exclude,
                                                          int expectedResultLength) {
        List<VariantEntity> variantEntityList =
                variantEntityRepository.findByRegionsAndComplexFilters(regions, studies, consequenceType, mafOperator,
                                                                       mafValue, polyphenOperator, polyphenValue,
                                                                       siftOperator, siftValue, exclude,
                                                                       new PageRequest(0, 100000000));
        assertNotNull(variantEntityList);
        assertTrue(variantEntityList.size() > 0);
        VariantEntity prevVariantEntity = variantEntityList.get(0);
        for (VariantEntity currVariantEntity : variantEntityList) {
            if (prevVariantEntity.getChromosome().equals(currVariantEntity.getChromosome())) {
                assertTrue(prevVariantEntity.getStart() <= currVariantEntity.getStart());
            }
        }
        assertEquals(expectedResultLength, variantEntityList.size());
    }
}