package org.icgc.dcc.imports.pathway.util;

import static com.google.common.base.Strings.repeat;
import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.newTreeSet;
import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;
import static org.icgc.dcc.common.core.util.Formats.formatCount;
import static org.icgc.dcc.common.core.util.Formats.formatPercent;
import static org.icgc.dcc.common.core.util.stream.Streams.stream;
import static org.icgc.dcc.imports.core.util.Importers.getLocalMongoClientUri;
import static org.icgc.dcc.imports.pathway.PathwayImporter.LOCAL_REACTOME_PATHWAY_HIER_URL;
import static org.icgc.dcc.imports.pathway.PathwayImporter.LOCAL_REACTOME_PATHWAY_SUMMATION_URL;
import static org.icgc.dcc.imports.pathway.PathwayImporter.LOCAL_REACTOME_UNIPROT_URL;
import static org.icgc.dcc.imports.pathway.PathwayImporter.REMOTE_REACTOME_PATHWAY_HIER_URL;
import static org.icgc.dcc.imports.pathway.PathwayImporter.REMOTE_REACTOME_PATHWAY_SUMMATION_URL;
import static org.icgc.dcc.imports.pathway.PathwayImporter.REMOTE_REACTOME_UNIPROT_URL;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.icgc.dcc.imports.core.GeneUniprotIdsReader;
import org.icgc.dcc.imports.geneset.model.pathway.PathwaySegment;
import org.icgc.dcc.imports.pathway.model.PathwaySummation;
import org.icgc.dcc.imports.pathway.model.PathwayUniprot;
import org.icgc.dcc.imports.pathway.reader.PathwayHierarchyReader;
import org.icgc.dcc.imports.pathway.reader.PathwaySummationReader;
import org.icgc.dcc.imports.pathway.reader.PathwayUniprotReader;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PathwayFileReport {

  /**
   * Constants.
   */
  private static final boolean REMOTE = true;

  private static final URL REACTOME_UNIPROT_URL =
      REMOTE ? REMOTE_REACTOME_UNIPROT_URL : LOCAL_REACTOME_UNIPROT_URL;
  private static final URL REACTOME_PATHWAY_SUMMATION_URL =
      REMOTE ? REMOTE_REACTOME_PATHWAY_SUMMATION_URL : LOCAL_REACTOME_PATHWAY_SUMMATION_URL;
  private static final URL REACTOME_PATHWAY_HIER_URL =
      REMOTE ? REMOTE_REACTOME_PATHWAY_HIER_URL : LOCAL_REACTOME_PATHWAY_HIER_URL;

  /**
   * Configuration.
   */
  private final boolean reportSummations = true;
  private final boolean reportIds = true;
  private final boolean reportUniprots = true;
  private final boolean reportNames = true;
  private final boolean reportStats = true;

  public void report() throws IOException {
    val watch = Stopwatch.createStarted();

    // Input
    val genes = readGenes();
    val summations = readSummations();
    val uniprots = readUniprots();
    val hierarchies = readHierarchies();

    // Summations
    if (reportSummations) {
      log.info("");
      banner("Summation Duplicates");
      reportDuplicates(summations);
    }

    // Ids
    if (reportIds) {
      val idsHierarchies =
          hierarchies.values().stream().flatMap(v -> v.stream()).map(s -> s.getReactomeId()).collect(toSet());
      val idsUniprots = stream(uniprots).map(s -> s.getReactomeId()).collect(toSet());
      val idsSummations = stream(summations).map(s -> s.getReactomeId()).collect(toSet());

      log.info("");
      banner("Id Differences");
      reportDifferences("id", "hierarchies", idsHierarchies, "summations", idsSummations);
      log.info("");
      reportDifferences("id", "hierarchies", idsHierarchies, "uniprots2reactome", idsUniprots);
      log.info("");
      reportDifferences("id", "summations", idsSummations, "uniprots2reactome", idsUniprots);
    }

    // Names
    if (reportNames) {
      val namesHierarchies =
          hierarchies.values().stream().flatMap(l -> l.stream()).map(s -> s.getReactomeName()).collect(toSet());
      val namesUniprots = stream(uniprots).map(s -> s.getName()).collect(toSet());
      val namesSummations = stream(summations).map(s -> s.getReactomeName()).collect(toSet());

      log.info("");
      banner("Name Differences");
      reportDifferences("name", "hierarchies", namesHierarchies, "summations", namesSummations);
      log.info("");
      reportDifferences("name", "hierarchies", namesHierarchies, "uniprots2reactome", namesUniprots);
      log.info("");
      reportDifferences("name", "summations", namesSummations, "uniprots2reactome", namesUniprots);
    }

    // Uniprots
    if (reportUniprots) {
      val uniprots1 = genes.values().stream().collect(toSet());
      val uniprots2 = stream(uniprots).map(s -> s.getUniprot()).collect(toSet());

      log.info("");
      banner("Uniprot Differences");
      reportDifferences("uniprotId", "db.Genes", uniprots1, "uniprots2reactome", uniprots2);
    }

    // Statistics
    if (reportStats) {
      val avgHierLength = hierarchies.values().stream().mapToInt(List::size).average().getAsDouble();

      log.info("");
      banner("Statistics");
      log.info("Average hierarchy path length: {}", format("%.2f", avgHierLength));
    }

    log.info("");
    log.info("Finished report in {}", watch);
  }

  private void reportDifferences(String field, String name1, Set<String> values1, String name2, Set<String> values2) {
    {
      val diff12 = newTreeSet(difference(values1, values2));
      log.info("The following {} ({} %) {} values are in {} but are not in {}:",
          formatCount(diff12), formatPercent(100.0f * diff12.size() / values1.size()), field, name1, name2);
      log.info("   {}", diff12);
    }
    log.info("");
    {
      val diff21 = newTreeSet(difference(values2, values1));
      log.info("The following {} ({} %) {} values are in {} but are not in {}:",
          formatCount(diff21), formatPercent(100.0f * diff21.size() / values2.size()), field, name2, name1);
      log.info("   {}", diff21);
    }
  }

  private void reportDuplicates(Iterable<PathwaySummation> summations) {
    val ids = Multimaps.index(summations, s -> s.getReactomeId());
    for (val id : ids.keySet()) {
      val values = ids.get(id);
      if (values.size() > 1) {
        log.info("The following {} summation entries are duplicated in id {}:", values.size(), id);
        values.forEach(p -> log.info("   {}", p));
      }
    }
  }

  private Iterable<PathwaySummation> readSummations() {
    return new PathwaySummationReader().read(REACTOME_PATHWAY_SUMMATION_URL);
  }

  private Multimap<String, List<PathwaySegment>> readHierarchies() throws IOException {
    return new PathwayHierarchyReader().read(REACTOME_PATHWAY_HIER_URL);
  }

  private Iterable<PathwayUniprot> readUniprots() {
    return new PathwayUniprotReader().read(REACTOME_UNIPROT_URL);
  }

  @SneakyThrows
  private Multimap<String, String> readGenes() {
    @Cleanup
    val reader = new GeneUniprotIdsReader(getLocalMongoClientUri("dcc-import"));
    return reader.read();
  }

  private static void banner(String message, Object... args) {
    log.info("{}", repeat("-", 100));
    log.info(message, args);
    log.info("{}", repeat("-", 100));
  }

}
