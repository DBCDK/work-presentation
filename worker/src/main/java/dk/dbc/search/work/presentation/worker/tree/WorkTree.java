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

import dk.dbc.search.work.presentation.worker.cache.CacheDataBuilder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@SuppressFBWarnings({"EI_EXPOSE_REP2", "EI_EXPOSE_REP"})
public class WorkTree extends HashMap<String, UnitTree> {

    private static final long serialVersionUID = 0x899E4144D4AA6ABBL;

    private final String work;
    private String primary;
    private final Instant modified;

    public WorkTree(String work, Instant modified) {
        this.work = work;
        this.modified = modified;
    }

    public Instant getModified() {
        return modified;
    }

    public String getPrimary() {
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
        return "work-of-" + getPrimary();
    }

    @Override
    public String toString() {
        return "WorkTree{" + "work=" + work + ", primary=" + getPrimary() + ", modified=" + modified + ", " + super.toString() + "}";
    }

    public void prettyPrint(Consumer<String> logger) {
        prettyPrintln(logger, "Work: %s", work);
        prettyPrintln(logger, " |-- primary: %s", getPrimary());
        prettyPrintln(logger, " %s modified: %s", isEmpty() ? "`--" : "|--", modified);
        for (Iterator<Entry<String, UnitTree>> units = entrySet().iterator() ; units.hasNext() ;) {
            Map.Entry<String, UnitTree> nextUnit = units.next();
            UnitTree unit = nextUnit.getValue();
            String unitPrefix = units.hasNext() ? "|--" : "`--";
            prettyPrintln(logger, " %s Unit: %s", unitPrefix, nextUnit.getKey());
            unitPrefix = units.hasNext() ? "|  " : "   ";
            prettyPrintln(logger, " %s |-- primary: %s", unitPrefix, unit.isPrimary());
            prettyPrintln(logger, " %s %s modified: %s", unitPrefix, unit.isEmpty() ? "`--" : "|--", unit.getModified());
            for (Iterator<Entry<String, ObjectTree>> objs = unit.entrySet().iterator() ; objs.hasNext() ;) {
                Map.Entry<String, ObjectTree> nextObj = objs.next();
                ObjectTree obj = nextObj.getValue();
                String objPrefix = objs.hasNext() ? "|--" : "`--";
                prettyPrintln(logger, " %s %s Obj: %s", unitPrefix, objPrefix, nextObj.getKey());
                objPrefix = objs.hasNext() ? "|  " : "   ";
                prettyPrintln(logger, " %s %s |-- primary: %s", unitPrefix, objPrefix, obj.isPrimary());
                prettyPrintln(logger, " %s %s %s modified: %s", unitPrefix, objPrefix, obj.isEmpty() ? "`--" : "|--", obj.getModified());
                for (Iterator<Entry<String, CacheDataBuilder>> streams = obj.entrySet().iterator() ; streams.hasNext() ;) {
                    Map.Entry<String, CacheDataBuilder> nextStream = streams.next();
                    String streamPrefix = streams.hasNext() ? "|--" : "`--";
                    prettyPrintln(logger, " %s %s %s Manifestation: %s: %s", unitPrefix, objPrefix, streamPrefix, nextStream.getKey(), nextStream.getValue());
                }
            }
        }
    }

    private void prettyPrintln(Consumer<String> logger, String format, Object... objs) {
        logger.accept(String.format(Locale.ROOT, format, objs));
    }

}
