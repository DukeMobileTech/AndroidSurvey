package org.adaptlab.chpir.android.survey.utils;

import org.adaptlab.chpir.android.survey.relations.OptionSetOptionRelation;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortUtils {
    public static List<OptionSetOptionRelation> sortedOptionSetOptionRelations(List<OptionSetOptionRelation> relations) {
        Collections.sort(relations, new Comparator<OptionSetOptionRelation>() {
            @Override
            public int compare(OptionSetOptionRelation o1, OptionSetOptionRelation o2) {
                return o1.optionSetOption.getPosition() - o2.optionSetOption.getPosition();
            }
        });
        return relations;
    }
}
