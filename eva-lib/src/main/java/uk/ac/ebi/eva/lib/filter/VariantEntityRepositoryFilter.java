/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2017 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.eva.lib.filter;

import org.springframework.data.mongodb.core.query.Criteria;

import uk.ac.ebi.eva.lib.repository.VariantEntityRepository;

import java.util.Collection;

public abstract class VariantEntityRepositoryFilter<T> {

    public final static String ANNOTATION_FIELD = "annot";
    public final static String STATISTICS_FIELD = "st";
    public final static String FILES_FIELD = "files";

    public final static String MAF_FIELD = STATISTICS_FIELD + ".maf";
    public final static String STUDY_ID_FIELD = FILES_FIELD + ".sid";
    public final static String CONSEQUENCE_TYPE_FIELD = ANNOTATION_FIELD + ".ct";

    public final static String POLYPHEN_FIELD = CONSEQUENCE_TYPE_FIELD + ".polyphen.sc";
    public final static String SIFT_FIELD = CONSEQUENCE_TYPE_FIELD + ".sift.sc";
    public final static String CONSEQUENCE_TYPE_SO_FIELD = CONSEQUENCE_TYPE_FIELD + ".so";

    private final String field;
    private final T value;
    private final VariantEntityRepository.RelationalOperator operator;

    public VariantEntityRepositoryFilter(String field, T value, VariantEntityRepository.RelationalOperator operator) {
        this.field = field;
        this.value = value;
        this.operator = operator;
    }

    public Criteria getCriteria() {
        Criteria criteria = Criteria.where(field);

        switch (operator) {
            case EQ:
                criteria = criteria.is(value);
                break;
            case GT:
                criteria = criteria.gt(value);
                break;
            case LT:
                criteria = criteria.lt(value);
                break;
            case GTE:
                criteria = criteria.gte(value);
                break;
            case LTE:
                criteria = criteria.lte(value);
                break;
            case IN:
                criteria = criteria.in((Collection) value);
                break;
            default:
                throw new IllegalArgumentException();
        }

        return criteria;
    }

    // equals and hashcode methods generated by intellij
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VariantEntityRepositoryFilter)) {
            return false;
        }

        VariantEntityRepositoryFilter<?> that = (VariantEntityRepositoryFilter<?>) o;

        if (!field.equals(that.field)) {
            return false;
        }
        if (!value.equals(that.value)) {
            return false;
        }
        return operator == that.operator;
    }

    @Override
    public int hashCode() {
        int result = field.hashCode();
        result = 31 * result + value.hashCode();
        result = 31 * result + operator.hashCode();
        return result;
    }

}
