/**
 * ExemptResearchers.java
 * @author Vagisha Sharma
 * Jan 12, 2009
 * @version 1.0
 */
package org.yeastrc.project;

/**
 * 
 */
public class ExemptResearchers {

    // 1756 Priska
    // 1752 Jimmy
    private static int[] researcherIds = {1756, 1752};
    
    public static boolean contains(int researcherId) {
        for(int rid: researcherIds)
            if(rid == researcherId)
                return true;
        return false;
    }
    
    public static boolean isProjectExempt(Project project) {
        return contains(project.getPI().getID());
    }
}
