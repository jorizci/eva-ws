package uk.ac.ebi.eva.lib.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.ebi.eva.lib.models.Assembly;
import uk.ac.ebi.eva.lib.entity.Taxonomy;

import java.util.List;

/**
 * Created by jorizci on 03/10/16.
 */
public interface TaxonomyRepository extends JpaRepository<Taxonomy, Long> {

    List<Assembly> getSpecies();

}
