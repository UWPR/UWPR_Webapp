package org.uwpr.instrumentlog.rawfile;

import java.util.List;

import org.uwpr.instrumentlog.ProjectInstrumentUsage;
import org.yeastrc.project.Project;

public class ProjectUsageSummary {

    private Project project;
    private List<ProjectInstrumentUsage> instrumentUsageList;
    private ProjectRawFileUsage rawFileUsage;
    
    public Project getProject() {
        return project;
    }
    public void setProject(Project project) {
        this.project = project;
    }
    
    public List<ProjectInstrumentUsage> getInstrumentUsage() {
        return instrumentUsageList;
    }
    public void setInstrumentUsage(List<ProjectInstrumentUsage> instrumentUsage) {
        this.instrumentUsageList = instrumentUsage;
    }
    
    public ProjectRawFileUsage getRawFileUsage() {
        return rawFileUsage;
    }
    public void setRawFileUsage(ProjectRawFileUsage rawFileUsage) {
        this.rawFileUsage = rawFileUsage;
    }
    
    public ProjectInstrumentUsage getUsageForInstrument(int instrumentId) {
        for(ProjectInstrumentUsage usage: instrumentUsageList) {
            if(usage.getInstrumentID() == instrumentId)
                return usage;
        }
        return null;
    }
    
}
