/*
 * Copyright (C) 2020 DBC A/S (http://dbc.dk/)
 *
 * This is part of work-presentation-worker
 *
 * work-presentation-worker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * work-presentation-worker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.search.work.presentation.worker.tree;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Collections.EMPTY_SET;

/**
 * Pojo to represent the root of a work tree, when extracting from corepo
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@SuppressFBWarnings({"EI_EXPOSE_REP2", "EI_EXPOSE_REP"})
public class WorkTree extends HashMap<String, UnitTree> {

    private static final long serialVersionUID = 0x899E4144D4AA6ABBL;

    private final String corepoWorkId;
    private String primary;
    private final Instant modified;
    private final HashMap<TypedRelation, RelationTree> relations;

    public WorkTree(String corepoWorkId, Instant modified) {
        this.corepoWorkId = corepoWorkId;
        this.modified = modified;
        this.relations = new HashMap<>();
    }

    public List<CacheContentBuilder> extractActiveCacheContentBuilders() {
        return Stream.concat(this.values().stream(),
                             relations.values().stream())
                .flatMap(u -> u.values().stream())
                .flatMap(o -> o.values().stream())
                .filter(builder -> !builder.isDeleted())
                .collect(Collectors.toList());
    }

    public Set<String> extractManifestationIds() {
        return extractActiveCacheContentBuilders().stream()
                .map(CacheContentBuilder::getManifestationId)
                .collect(Collectors.toSet());
    }

    public Instant getModified() {
        return modified;
    }

    public HashMap<TypedRelation, RelationTree> getRelations() {
        return relations;
    }

    public void addRelation(TypedRelation relationUnitId, RelationTree relation) {
        this.relations.put(relationUnitId, relation);
    }

    /**
     * Find the "owner" of the work
     *
     * @return a manifestation id or null if none exists (deleted work)
     */
    public String getPrimaryManifestationId() {
        if (isEmpty())
            return null;
        if (primary == null) {
            forEach((unitK, unitV) -> {
                if (unitV.isPrimary()) {
                    unitV.forEach((objK, objV) -> {
                        if (objV.isPrimary()) {
                            primary = objK;
                        }
                    });
                }
            });
        }
        return primary;
    }

    public String getPersistentWorkId() {
        if (isEmpty())
            return null;
        return "work-of:" + getPrimaryManifestationId();
    }

    public String getCorepoWorkId() {
        return corepoWorkId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), corepoWorkId, primary, modified, relations);
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj) || getClass() != obj.getClass())
            return false;
        final WorkTree other = (WorkTree) obj;
        return Objects.equals(this.corepoWorkId, other.corepoWorkId) &&
               Objects.equals(this.primary, other.primary) &&
               Objects.equals(this.modified, other.modified) &&
               Objects.equals(this.relations, other.relations);
    }

    @Override
    public String toString() {
        return "WorkTree{" + "corepoWorkId=" + corepoWorkId + ", primary=" + getPrimaryManifestationId() + ", modified=" + modified + ", relations=" + relations + ", " + super.toString() + "}";
    }

    public void prettyPrint(Consumer<String> logger) {
        println(logger, "Work: %s", corepoWorkId);
        println(logger, " |-- primary: %s", getPrimaryManifestationId());
        println(logger, " %s modified: %s", isEmpty() ? "`--" : "|--", modified);
        boolean hasWorkRelations = !relations.isEmpty();
        for (Iterator<Entry<String, UnitTree>> units = entrySet().iterator() ; units.hasNext() ;) {
            Map.Entry<String, UnitTree> nextUnit = units.next();
            UnitTree unit = nextUnit.getValue();
            boolean hasRelations = !unit.getRelations().isEmpty();
            String unitPrefix = units.hasNext() ? "|--" : "`--";
            println(logger, " %s Unit: %s", unitPrefix, nextUnit.getKey());
            unitPrefix = units.hasNext() || hasWorkRelations ? "|  " : "   ";
            println(logger, " %s  |-- primary: %s", unitPrefix, unit.isPrimary());
            println(logger, " %s  %s modified: %s", unitPrefix, unit.isEmpty() && !hasRelations ? "`--" : "|--", unit.getModified());

            for (Iterator<Entry<String, ObjectTree>> objs = unit.entrySet().iterator() ; objs.hasNext() ;) {
                Map.Entry<String, ObjectTree> nextObj = objs.next();
                ObjectTree obj = nextObj.getValue();
                boolean moreObjEntries = objs.hasNext() || hasRelations;
                String objPrefix = moreObjEntries ? "|--" : "`--";
                println(logger, " %s  %s Obj: %s", unitPrefix, objPrefix, nextObj.getKey());
                objPrefix = moreObjEntries ? "|  " : "   ";
                println(logger, " %s  %s  |-- primary: %s", unitPrefix, objPrefix, obj.isPrimary());
                println(logger, " %s  %s  %s modified: %s", unitPrefix, objPrefix, obj.isEmpty() ? "`--" : "|--", obj.getModified());
                for (Iterator<Entry<String, CacheContentBuilder>> streams = obj.entrySet().iterator() ; streams.hasNext() ;) {
                    Map.Entry<String, CacheContentBuilder> nextStream = streams.next();
                    String streamPrefix = streams.hasNext() ? "|--" : "`--";
                    println(logger, " %s  %s  %s Manifestation: %s: %s", unitPrefix, objPrefix, streamPrefix, nextStream.getKey(), nextStream.getValue());
                }
            }
            if (hasRelations) {
                println(logger, " %s  `-- Relations:", unitPrefix);
                for (Iterator<TypedRelation> rels = unit.getRelations().iterator() ; rels.hasNext() ;) {
                    TypedRelation rel = rels.next();
                    String relPrefix = rels.hasNext() ? "|--" : "`--";
                    println(logger, " %s      %s %s in %s", unitPrefix, relPrefix, rel.getType(), rel.getUnit());
                }
            }
        }
        if (hasWorkRelations) {
            println(logger, " `-- Relations:");
            for (Iterator<Entry<TypedRelation, RelationTree>> rels = relations.entrySet().iterator() ; rels.hasNext() ;) {
                Map.Entry<TypedRelation, RelationTree> rel = rels.next();
                String relPrefix = rels.hasNext() ? "|--" : "`--";
                println(logger, "      %s Relation:", relPrefix);
                relPrefix = rels.hasNext() ? "|  " : "   ";
                println(logger, "      %s  |-- Type: %s", relPrefix, rel.getKey().getType().getName());
                println(logger, "      %s  |-- Unit: %s ", relPrefix, rel.getKey().getUnit());
                for (Iterator<Entry<String, ObjectTree>> objs = rel.getValue().entrySet().iterator() ; objs.hasNext() ;) {
                    Map.Entry<String, ObjectTree> nextObj = objs.next();
                    ObjectTree obj = nextObj.getValue();
                    String objPrefix = objs.hasNext() ? "|--" : "`--";
                    println(logger, "      %s  %s Obj: %s", relPrefix, objPrefix, nextObj.getKey());
                    objPrefix = objs.hasNext() ? "|  " : "   ";
                    println(logger, "      %s  %s  |-- primary: %s", relPrefix, objPrefix, obj.isPrimary());
                    println(logger, "      %s  %s  %s modified: %s", relPrefix, objPrefix, obj.isEmpty() ? "`--" : "|--", obj.getModified());
                    for (Iterator<Entry<String, CacheContentBuilder>> streams = obj.entrySet().iterator() ; streams.hasNext() ;) {
                        Map.Entry<String, CacheContentBuilder> nextStream = streams.next();
                        String streamPrefix = streams.hasNext() ? "|--" : "`--";
                        println(logger, "      %s  %s  %s Manifestation: %s: %s", relPrefix, objPrefix, streamPrefix, nextStream.getKey(), nextStream.getValue());
                    }
                }
            }
        }
    }

    private void println(Consumer<String> logger, String format, Object... objs) {
        logger.accept(String.format(Locale.ROOT, format, objs));
    }

}
