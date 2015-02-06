package org.yeastrc.project;

import java.util.ArrayList;
import java.util.List;

public enum CollaborationStatus {
    PENDING("Pending", "Pending", 'P'),
    ACCEPTED("Accepted", "Accept", 'A'),
    REJECTED("Rejected", "Reject", 'R'),
    REVISE("Revise", "Revise", 'V'),
    REVISE_PENDING("Revise-Pending", "Revise-Pending", 'S'),
    COMPLETE("Complete", "Complete", 'C'),
    EXPIRED("Expired", "Expired", 'E');
    
    private String longName;
    private char shortName;
    private String altName;
    
    private CollaborationStatus(String longName, String altName, char shortName) {
        this.longName = longName;
        this.shortName = shortName;
        this.altName = altName;
    }
    public String getLongName() {
        return longName;
    }
    public String getShortName() {
        return String.valueOf(shortName);
    }
    public String getAltName() {
        return altName;
    }
    public String toString() {
        return longName;
    }
    
    public static CollaborationStatus statusForChar(char statusChar) {
        switch (statusChar) { 
        case 'P':   return PENDING;
        case 'A':   return ACCEPTED;
        case 'R':   return REJECTED;
        case 'V':   return REVISE;
        case 'S':	return REVISE_PENDING;
        case 'C':   return COMPLETE;
        case 'E':   return EXPIRED;
        }
        // did not find a match
        return null;
    }
    
    public static CollaborationStatus statusForString(String statusStr) {
        if (statusStr == null || statusStr.length() != 1)
            return null;
        return statusForChar(statusStr.charAt(0));
    }
    
    public static List<CollaborationStatus> getDisplayStatusList() {
    	
    	List<CollaborationStatus> list = new ArrayList<CollaborationStatus>();
    	list.add(CollaborationStatus.PENDING);
    	list.add(CollaborationStatus.REJECTED);
    	list.add(CollaborationStatus.REVISE);
    	list.add(CollaborationStatus.ACCEPTED);
    	list.add(CollaborationStatus.COMPLETE);
    	list.add(CollaborationStatus.EXPIRED);
    	return list;
    }
}