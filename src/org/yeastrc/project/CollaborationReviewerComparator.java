package org.yeastrc.project;

import java.util.Comparator;

public class CollaborationReviewerComparator implements Comparator<CollaborationAndReview> {

    @Override
    public int compare(CollaborationAndReview cr1, CollaborationAndReview cr2) {
        
        if(cr1.getReviewerNames().length() == 0 && cr2.getReviewerNames().length() == 0)
            return 0;
        
        if(cr1.getReviewerNames().length() == 0)    return 1;
        if(cr2.getReviewerNames().length() == 0)    return -1;
        return cr1.getReviewerNames().compareTo(cr2.getReviewerNames());
    }
}
