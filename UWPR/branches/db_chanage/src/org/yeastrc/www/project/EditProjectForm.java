/*
 * RegisterForm.java
 *
 * Created on October 17, 2003
 *
 * Created by Michael Riffle <mriffle@u.washington.edu>
 *
 */

package org.yeastrc.www.project;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.yeastrc.project.Researcher;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Riffle <mriffle@u.washington.edu>
 * @version 2004-01-21
 */
public class EditProjectForm extends ActionForm {


	/**
	 * Validate the properties that have been sent from the HTTP request,
	 * and return an ActionErrors object that encapsulates any
	 * validation errors that have been found.  If no errors are found, return
	 * an empty ActionErrors object.
	 */
	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		ActionErrors errors = new ActionErrors();

		if (this.getPI() == 0) {
			errors.add("PI", new ActionMessage("error.project.nopi"));
		}
		
		if (this.getTitle() == null || this.getTitle().length() < 1) {
			errors.add("title", new ActionMessage("error.project.notitle"));
		}
		
		if (this.getAbstract() == null || this.getAbstract().length() < 1) {
			errors.add("project", new ActionMessage("error.project.noabstract"));
		}
		
		// Prevent users from submitting abstracts > 500 words
		if(wordCount(this.getAbstract()) > 500) {
		    errors.add("project", new ActionMessage("error.project.longAbstract"));
		}
		
		// If the user is trying to save an entry in the progress report field
		// make sure it is at least 20 words long
		String progress = this.getProgress();
		if(progress != null && progress.trim().length() > 0) {
		    if(WordCounter.count(progress) < 20)
		        errors.add("project", new ActionMessage("error.project.invalid.progress"));
		}
		
		return errors;
	}
	
	int wordCount(String abstract1) {
	    if(abstract1 == null || abstract1.length() == 0)
	        return 0;
//        return abstract1.split("\\s+").length;
	    return WordCounter.count(abstract1);
    }
	
	/** Set the title */
	public void setTitle(String arg) { this.title = arg; }

	/** Set the abstract */
	public void setAbstract(String arg) { this.projectAbstract = arg; }
	
	/** Set the public abstract */
	public void setPublicAbstract(String arg) { this.publicAbstract = arg; }

	/** Set the funding types */
	public void setFundingTypes(String[] arg) {
		if (arg != null)
			this.fundingTypes = arg;
	}

	/** Set the federal funding types */
	public void setFederalFundingTypes(String[] arg) {
		if (arg != null)
			this.federalFundingTypes = arg;
	}

	/** Set the progress */
	public void setProgress(String arg) { this.progress = arg; }

	/** Set the keywords */
	public void setKeywords(String arg) { this.keywords = arg; }

	/** Set the comments */
	public void setComments(String arg) { this.comments = arg; }

	/** Set the publications */
	public void setPublications(String arg) { this.publications = arg; }

	/** Set the BTA */
	public void setBTA(float arg) { this.bta = arg; }

	/** Set the AXIS I */
	public void setAxisI(String arg) { this.axisi = arg; }

	/** Set the AXIS II */
	public void setAxisII(String arg) { this.axisii = arg; }

	/** Set the PI ID */
	public void setPI(int arg) { this.pi = arg; }

    /** Get the title */
	public String getTitle() { return this.title; }

	/** Get the abstract */
	public String getAbstract() { return this.projectAbstract; }
	
	/** Get the public abstract */
	public String getPublicAbstract() { return this.publicAbstract; }

	/** Get the funding types */
	public String[] getFundingTypes() { return this.fundingTypes; }

	/** Get the federal funding types */
	public String[] getFederalFundingTypes() { return this.federalFundingTypes; }

	/** Get the progress */
	public String getProgress() { return this.progress; }

	/** Get the keywords */
	public String getKeywords() { return this.keywords; }

	/** Get the comments */
	public String getComments() { return this.comments; }

	/** Get the publications */
	public String getPublications() { return this.publications; }

	/** Get the BTA */
	public float getBTA() { return this.bta; }

	/** Get the AXIS I */
	public String getAxisI() { return this.axisi; }

	/** Get the AXIS II */
	public String getAxisII() { return this.axisii; }

	/** Get the PI ID */
	public int getPI() { return this.pi; }
	

	/**
	 * @return Returns the foundationName.
	 */
	public String getFoundationName() {
		return foundationName;
	}
	/**
	 * @param foundationName The foundationName to set.
	 */
	public void setFoundationName(String foundationName) {
		this.foundationName = foundationName;
	}
	/**
	 * @return Returns the grantAmount.
	 */
	public String getGrantAmount() {
		return grantAmount;
	}
	/**
	 * @param grantAmount The grantAmount to set.
	 */
	public void setGrantAmount(String grantAmount) {
		this.grantAmount = grantAmount;
	}
	/**
	 * @return Returns the grantNumber.
	 */
	public String getGrantNumber() {
		return grantNumber;
	}
	/**
	 * @param grantNumber The grantNumber to set.
	 */
	public void setGrantNumber(String grantNumber) {
		this.grantNumber = grantNumber;
	}

    //----------------------------------------------------------------
    // Researchers
    //----------------------------------------------------------------
    public Researcher getResearcher(int index) {
        //System.out.println("Getting researcher id at index: "+index);
        while(index >= researchers.size())
            researchers.add(new Researcher());
        return researchers.get(index);
    }

    public List<Researcher> getResearcherList() {
        //System.out.println("Getting researcher list");
        List<Researcher> rList = new ArrayList<Researcher>();
        for(Researcher r: researchers) {
            if(r != null && r.getID() > 0)
                rList.add(r);
        }
        return rList;
    }

    public void setResearcherList(List<Researcher> researchers) {
        //System.out.println("Setting researcher");
        this.researchers = researchers;
    }

    private int validResearcherCount() {
        int i = 0;
        for (Researcher researcher: researchers) {
            if (researcher != null && researcher.getID() > 0) i++;
        }
        return i;
    }
	
	
	
	
	
	// The form variables we'll be tracking

	
	private String title = null;
	private String projectAbstract = null;
	private String publicAbstract = null;
	private String[] fundingTypes = new String[0];
	private String[] federalFundingTypes = new String[0];
	private String progress = null;
	private String keywords = null;
	private String comments = null;
	private String instrumentTimeExpl = null;
	private boolean isNotPending = false;
	private String publications = null;
	private float bta = (float)0;
	private String axisi = null;
	private String axisii = null;
	private String scientificQuestion = null;
	
	private int pi = 0;
    private List<Researcher> researchers = new ArrayList<Researcher>();

	private String foundationName = null;
	private String grantNumber = null;
	private String grantAmount = null;
	
	private int ltqRunsRequested = 0;
	private int ltq_etdRunsRequested = 0;
	private int ltq_orbitrapRunsRequested = 0;
	private int ltq_ftRunsRequested = 0;
	private int tsq_accessRunsRequested = 0;
	private int tsq_vantageRunsRequested = 0;
	
	private boolean databaseSearchRequested;
	
	private boolean massSpecExpertiseRequested = false;
	
	private String[] fragmentationTypes;

	private String extensionReasons = null;
	
	// these will be uneditable fields in the form.  They have been added to the form so that if an error 
    // happens in form validation these values are still available to display in the form. 
	private int ID = 0;
	private int parentProjectId;
    private String submitDate = null;
    
	/**
	 * @return the databaseSearchRequested
	 */
	public boolean isDatabaseSearchRequested() {
		return databaseSearchRequested;
	}

	/**
	 * @param databaseSearchRequested the databaseSearchRequested to set
	 */
	public void setDatabaseSearchRequested(boolean databaseSearchRequested) {
		this.databaseSearchRequested = databaseSearchRequested;
	}
	
	/**
     * @return the massSpecExpertiseRequested
     */
    public boolean isMassSpecExpertiseRequested() {
        return massSpecExpertiseRequested;
    }

    /**
     * @param massSpecExpertiseRequested the massSpecExpertiseRequested to set
     */
    public void setMassSpecExpertiseRequested(boolean massSpecExpertiseRequested) {
        this.massSpecExpertiseRequested = massSpecExpertiseRequested;
    }


	/**
	 * @return the fragmentationTypes
	 */
	public String[] getFragmentationTypes() {
		return fragmentationTypes;
	}

	/**
	 * @param fragmentationTypes the fragmentationTypes to set
	 */
	public void setFragmentationTypes(String[] fragmentationTypes) {
		this.fragmentationTypes = fragmentationTypes;
	}

	/**
	 * @return the ltq_etdRunsRequested
	 */
	public int getLtq_etdRunsRequested() {
		return ltq_etdRunsRequested;
	}

	/**
	 * @param ltq_etdRunsRequested the ltq_etdRunsRequested to set
	 */
	public void setLtq_etdRunsRequested(int ltq_etdRunsRequested) {
		this.ltq_etdRunsRequested = ltq_etdRunsRequested;
	}

	/**
	 * @return the ltq_ftRunsRequested
	 */
	public int getLtq_ftRunsRequested() {
		return ltq_ftRunsRequested;
	}

	/**
	 * @param ltq_ftRunsRequested the ltq_ftRunsRequested to set
	 */
	public void setLtq_ftRunsRequested(int ltq_ftRunsRequested) {
		this.ltq_ftRunsRequested = ltq_ftRunsRequested;
	}

	/**
	 * @return the ltq_orbitrapRunsRequested
	 */
	public int getLtq_orbitrapRunsRequested() {
		return ltq_orbitrapRunsRequested;
	}

	/**
	 * @param ltq_orbitrapRunsRequested the ltq_orbitrapRunsRequested to set
	 */
	public void setLtq_orbitrapRunsRequested(int ltq_orbitrapRunsRequested) {
		this.ltq_orbitrapRunsRequested = ltq_orbitrapRunsRequested;
	}

	/**
	 * @return the ltqRunsRequested
	 */
	public int getLtqRunsRequested() {
		return ltqRunsRequested;
	}

	/**
	 * @param ltqRunsRequested the ltqRunsRequested to set
	 */
	public void setLtqRunsRequested(int ltqRunsRequested) {
		this.ltqRunsRequested = ltqRunsRequested;
	}

	/**
	 * @return the tsq_accessRunsRequested
	 */
	public int getTsq_accessRunsRequested() {
		return tsq_accessRunsRequested;
	}

	/**
	 * @param tsq_accessRunsRequested the tsq_accessRunsRequested to set
	 */
	public void setTsq_accessRunsRequested(int tsq_accessRunsRequested) {
		this.tsq_accessRunsRequested = tsq_accessRunsRequested;
	}
	
	/**
     * @return the tsq_vantageRunsRequested
     */
    public int getTsq_vantageRunsRequested() {
        return tsq_vantageRunsRequested;
    }

    /**
     * @param tsq_vantageRunsRequested the tsq_vantageRunsRequested to set
     */
    public void setTsq_vantageRunsRequested(int tsq_vantageRunsRequested) {
        this.tsq_vantageRunsRequested = tsq_vantageRunsRequested;
    }

//    private int getTotalRunsRequested() {
//    	int count = getLtq_etdRunsRequested() +
//    	 			getLtq_ftRunsRequested() +
//    	 			getLtq_orbitrapRunsRequested() +
//    	 			getLtqRunsRequested() +
//    	 			getTsq_accessRunsRequested() +
//    	 			getTsq_vantageRunsRequested();
//    	return count;
//    }
	/**
	 * @return the scientificQuestion
	 */
	public String getScientificQuestion() {
		return scientificQuestion;
	}

	/**
	 * @param scientificQuestion the scientificQuestion to set
	 */
	public void setScientificQuestion(String scientificQuestion) {
		this.scientificQuestion = scientificQuestion;
	}
	
	public int getID() {
        return ID;
    }
    
    public void setID(int ID) {
        this.ID = ID;
    }
    
    public int getParentProjectID() {
        return parentProjectId;
    }
    
    public void setParentProjectID(int parentProjectId) {
        this.parentProjectId = parentProjectId;
    }
    
    public String getSubmitDate() {
        return submitDate;
    }
    
    public void setSubmitDate(String date) {
        this.submitDate = date;
    }

    public String getExtensionReasons() {
        return extensionReasons;
    }

    public void setExtensionReasons(String extensionReasons) {
        this.extensionReasons = extensionReasons;
    }

	public String getInstrumentTimeExpl() {
		return instrumentTimeExpl;
	}

	public void setInstrumentTimeExpl(String instrumentTimeExpl) {
		this.instrumentTimeExpl = instrumentTimeExpl;
	}

	public boolean isNotPending() {
		return isNotPending;
	}

	public void setNotPending(boolean isNotPending) {
		this.isNotPending = isNotPending;
	}
}