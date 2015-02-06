package org.yeastrc.project;

import java.util.Comparator;

public class CollaborationStatusComparator implements Comparator<ComparableCollaboration> {

    @Override
    public int compare(ComparableCollaboration p1, ComparableCollaboration p2) {
        
        CollaborationStatus s1 = p1.getCollaborationStatus();
        CollaborationStatus s2 = p2.getCollaborationStatus();
        
        if (s1 == null && s2 == null) return 0;
        if (s1 == null) return 1;
        if (s2 == null) return -1;
        
        return s1.getShortName().compareTo(s2.getShortName());
    }

}
