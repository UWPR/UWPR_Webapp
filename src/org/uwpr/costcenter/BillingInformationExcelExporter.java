/**
 * BillingInformationExporter.java
 * @author Vagisha Sharma
 * Jun 17, 2011
 */
package org.uwpr.costcenter;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.uwpr.instrumentlog.*;
import org.uwpr.scheduler.UsageBlockBaseWithRate;
import org.yeastrc.data.InvalidIDException;
import org.yeastrc.project.*;
import org.yeastrc.project.payment.PaymentMethod;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;

/**
 * 
 */
public class BillingInformationExcelExporter {


	private java.util.Date startDate;
	private java.util.Date endDate;

	private boolean summarize = false;

	private BillingInformationExporterListener listener = null;

	private static final Logger log = LogManager.getLogger(BillingInformationExcelExporter.class);

	private int rowNum = 0;

	// These are blocks whose end or start date has to be updated because they extend outside the date range being billed.
	// We do this only if we are invoicing blocks.
	//private List<UsageBlockBase> _blocksToUpdate = new ArrayList<>();

	public void setStartDate(java.util.Date startDate) {
		this.startDate = startDate;
	}

	java.util.Date getStartDate() {
		return this.startDate;
	}

	public void setEndDate(java.util.Date endDate) {
		this.endDate = endDate;
	}

	java.util.Date getEndDate() {
		return this.endDate;
	}

	public boolean isSummarize() {
		return summarize;
	}

	public void setSummarize(boolean summarize) {
		this.summarize = summarize;
	}

	public void exportToXls(OutputStream outStream) throws BillingInformationExporterException {

		// create a new workbook
		Workbook wb = new HSSFWorkbook();
		// create a new sheet
		Sheet sheet = wb.createSheet();
		
		if(this.isSummarize())
			writeHeaderSummarized(sheet);
		else
			writeHeaderDetailed(sheet);
		
		// get a list of all projects scheduled in the given time range(includes billed, subsidized and maintenance projects)
		List<Project> projects = getAllProjects();
		
		// write details for each project
		for(Project project: projects) {
			
			if(this.isSummarize())
				exportSummarized(project, sheet, false);
			else
				exportDetailed(project, sheet, false);
			
		}
		informListenerExportDone(); // Blocks will be invoiced in this step, if there is a listener
		
		try {
			wb.write(outStream);
		} catch (IOException e) {
			throw new BillingInformationExporterException("Error writing data.", e);
		}
	}
	

	public void exportToXls(Project project, OutputStream outStream) throws BillingInformationExporterException {
		
		// create a new workbook
		Workbook wb = new HSSFWorkbook();
		// create a new sheet
		Sheet sheet = wb.createSheet();
		
		if(this.isSummarize()) {
			exportSummarized(project, sheet, true);
			informListenerExportDone();
		}
		else {
			exportDetailed(project, sheet, true);
			informListenerExportDone();
		}
		
		try {
			wb.write(outStream);
		} catch (IOException e) {
			throw new BillingInformationExporterException("Error writing data.", e);
		}
	}
	
	private void exportDetailed(Project project, Sheet sheet, boolean writeHeader) throws BillingInformationExporterException {

		if(writeHeader) {
			// write the header
			writeHeaderDetailed(sheet);
		}

		// make sure the dates are alright
		checkDates();

		// Get the usage blocks for this project that have their start OR end dates within the given date range.
		List<UsageBlockBase> usageBlocks = getSortedUsageBlocksForProject_byStartDate(project.getID());

		if(usageBlocks.size() == 0) {
			log.info("No usage found for project ID: "+project.getID()+" between the dates: "+getStartDate()+" - "+getEndDate());
			return;
		}

		// write out the cost for each block
		// there may be multiple payment methods for each block
		try {
			for(UsageBlockBase block: usageBlocks) {
				writeBlockDetails(project, block, sheet);
			}

		} catch (SQLException e) {
			throw new BillingInformationExporterException("Error reading usage details from database", e);
		}
	}
	
	private void exportSummarized(Project project, Sheet sheet, boolean writeHeader)
		throws BillingInformationExporterException {

		if(writeHeader) {
			// write the header
			writeHeaderSummarized(sheet);
		}

		// make sure the dates are alright
		checkDates();

		// Get the usage blocks for this project that have their start OR end dates within the given date range.
		List<UsageBlockBase> usageBlocks = getSortedUsageBlocksForProject_byStartDate(project.getID());

		
		if(usageBlocks.size() == 0) {
			log.debug("No usage found for project ID: " + project.getID() + " between the dates: " + getStartDate() + " - " + getEndDate());

			return;
		}

		ProjectUsageBlockSummarizer summarizer = new ProjectUsageBlockSummarizer(project);
		
		for(UsageBlockBase block: usageBlocks) {
			
			if(block.getEndDate().after(this.getEndDate())) {
				// Should not happen
				throw new BillingInformationExporterException("Block end date outside range. " + block.toString());
			}
			
			// get the instrument rate
			InstrumentRate rate;
			try {
				rate = InstrumentRateDAO.getInstance().getInstrumentRate(block.getInstrumentRateID());
			} catch (SQLException e) {
				throw new BillingInformationExporterException("Error getting instrument rate for block; block ID: "
						+block.getID()+"; instrument rate ID: "+block.getInstrumentRateID()
						+"; ");
			}
			
			try {
				summarizer.add(block, rate);
			} catch (SQLException e) {
				throw new BillingInformationExporterException("Error adding block to summarizer", e);
			}
		}
		
		// write out the cost for each summarized block
		// there may be multiple payment methods for each block
		// 08/08/18 When we are exporting summarized blocks, there is one UsageBlockForBilling per combination of
		// instrumentId, researcherId, payment method Id and payment percent.  So, if there are two payment methods
		// for a block, the summarizer will create two corresponding UsageBlockForBilling objects.
		// Trigger listener method only once for each instrumentUsageId. Otherwise, invoiceInstrumentUsage
		// table will have multiple entries for the same instrumentUsageId.
		Set<Integer> exportedBlockIds = new HashSet<>();
		for(UsageBlockForBilling block: summarizer.getSummarizedBlocks()) {
			
			boolean exported = writeBlockPaymentMethodDetails(sheet, block, true);
			
			if(exported) {

				for(UsageBlockBase ublock: block.getBlocks()) {
					if(!exportedBlockIds.contains(ublock.getID())) {
						informListenerBlockExported(ublock);
					}
					exportedBlockIds.add(ublock.getID());
				}
			}
		}
	}

	private void writeHeaderDetailed(Sheet sheet) {
		
		int cellnum = 0;
		sheet.createRow(rowNum++).createCell(cellnum).setCellValue("Billing information exported on "+new java.util.Date());
		sheet.createRow(rowNum++).createCell(cellnum).setCellValue("Start Date: "+this.getStartDate()+"\n");
		sheet.createRow(rowNum++).createCell(cellnum).setCellValue("End Date: "+this.getEndDate()+"\n");
		sheet.createRow(rowNum++).createCell(cellnum).setCellValue("* Only blocks ending in a billing period are billed in that period.\n");

		
		Row row = sheet.createRow(rowNum++);
		row.createCell(cellnum++).setCellValue("ProjectID");
		row.createCell(cellnum++).setCellValue("Lab_Director");
		row.createCell(cellnum++).setCellValue("Researcher");
		row.createCell(cellnum++).setCellValue("Instrument");
		row.createCell(cellnum++).setCellValue("Labor");
		row.createCell(cellnum++).setCellValue("UsageBlockID");
		row.createCell(cellnum++).setCellValue("Start");
		row.createCell(cellnum++).setCellValue("End");
		row.createCell(cellnum++).setCellValue("TimeBlock_Hours");
		row.createCell(cellnum++).setCellValue("TimeBlock_Rate");
		row.createCell(cellnum++).setCellValue("PO_Number");
		row.createCell(cellnum++).setCellValue("PO_Name");
		row.createCell(cellnum++).setCellValue("Worktag");
		row.createCell(cellnum++).setCellValue("Worktag_Descr");
		row.createCell(cellnum++).setCellValue("Resource_Worktag");
		row.createCell(cellnum++).setCellValue("Resource_Worktag_Descr");
		row.createCell(cellnum++).setCellValue("Assignee_Worktag");
		row.createCell(cellnum++).setCellValue("Assignee_Worktag_Descr");
		row.createCell(cellnum++).setCellValue("Activity_Worktag");
		row.createCell(cellnum++).setCellValue("Activity_Worktag_Descr");
		row.createCell(cellnum++).setCellValue("Federal_Funding");
		row.createCell(cellnum++).setCellValue("%Billed");
		row.createCell(cellnum++).setCellValue("AmountBilled");
		row.createCell(cellnum++).setCellValue("BilledInstrument");
		row.createCell(cellnum++).setCellValue("BilledSetup");
		row.createCell(cellnum++).setCellValue("ContactFirstName");
		row.createCell(cellnum++).setCellValue("ContactLastName");
		row.createCell(cellnum++).setCellValue("ContactEmail");
		row.createCell(cellnum++).setCellValue("ContactPhone");
		
	}
	
	private void writeHeaderSummarized(Sheet sheet) {
		
		int cellnum = 0;
		sheet.createRow(rowNum++).createCell(cellnum).setCellValue("Billing information exported on "+new java.util.Date());
		sheet.createRow(rowNum++).createCell(cellnum).setCellValue("Start Date: "+this.getStartDate()+"\n");
		sheet.createRow(rowNum++).createCell(cellnum).setCellValue("End Date: "+this.getEndDate()+"\n");
		sheet.createRow(rowNum++).createCell(cellnum).setCellValue("* Only blocks ending in a billing period are billed in that period.\n");


		Row row = sheet.createRow(rowNum++);
		row.createCell(cellnum++).setCellValue("ProjectID");
		row.createCell(cellnum++).setCellValue("Lab_Director");
		row.createCell(cellnum++).setCellValue("Researcher");
		row.createCell(cellnum++).setCellValue("Instrument");
		row.createCell(cellnum++).setCellValue("Labor");
		row.createCell(cellnum++).setCellValue("Start");
		row.createCell(cellnum++).setCellValue("End");
		row.createCell(cellnum++).setCellValue("Hours_Used");
		row.createCell(cellnum++).setCellValue("PO_Number");
		row.createCell(cellnum++).setCellValue("PO_Name");
		row.createCell(cellnum++).setCellValue("Worktag");
		row.createCell(cellnum++).setCellValue("Worktag_Descr");
		row.createCell(cellnum++).setCellValue("Resource_Worktag");
		row.createCell(cellnum++).setCellValue("Resource_Worktag_Descr");
		row.createCell(cellnum++).setCellValue("Assignee_Worktag");
		row.createCell(cellnum++).setCellValue("Assignee_Worktag_Descr");
		row.createCell(cellnum++).setCellValue("Activity_Worktag");
		row.createCell(cellnum++).setCellValue("Activity_Worktag_Descr");
		row.createCell(cellnum++).setCellValue("Federal_Funding");
		row.createCell(cellnum++).setCellValue("%Billed");
		row.createCell(cellnum++).setCellValue("AmountBilled");
		row.createCell(cellnum++).setCellValue("BilledInstrument");
		row.createCell(cellnum++).setCellValue("BilledSetup");
		row.createCell(cellnum++).setCellValue("ContactFirstName");
		row.createCell(cellnum++).setCellValue("ContactLastName");
		row.createCell(cellnum++).setCellValue("ContactEmail");
		row.createCell(cellnum++).setCellValue("ContactPhone");
		
	}
	
	private void writeBlockDetails(Project project, UsageBlockBase block, Sheet sheet) throws BillingInformationExporterException, SQLException {
		
		// get the name of the researcher that scheduled the instrument time
		int researcherId = block.getResearcherID();
		Researcher researcher = new Researcher();
		try {
			researcher.load(researcherId);
		} catch (InvalidIDException e) {
			throw new BillingInformationExporterException("Error getting researcher with ID: "+researcherId+"; Error message was: "+e.getMessage());
		}

		// get the instrument
		MsInstrument instrument = MsInstrumentUtils.instance().getMsInstrument(block.getInstrumentID());

		// get the instrument rate
		InstrumentRate rate = InstrumentRateDAO.getInstance().getInstrumentRate(block.getInstrumentRateID());

		// get the payment method(s) for this block
		List<InstrumentUsagePayment> usagePayments = InstrumentUsagePaymentGetter.get(project, block);

		boolean blockExported = false;
		for(InstrumentUsagePayment usagePayment: usagePayments) {

			UsageBlockForBilling billBlock = new UsageBlockForBilling();
			UsageBlockBaseWithRate blkWithRate = new UsageBlockBaseWithRate();
			block.copyTo(blkWithRate);
			blkWithRate.setRate(rate);;
			billBlock.add(blkWithRate);
			billBlock.setProject(project);
			billBlock.setUser(researcher);
			billBlock.setInstrument(instrument);
			billBlock.setPaymentMethod(usagePayment.getPaymentMethod());
			billBlock.setBillingPercent(usagePayment.getPercent());
			
			blockExported = writeBlockPaymentMethodDetails(sheet, billBlock, false);
		}
		
		if(blockExported)
			informListenerBlockExported(block);
	}
	
	private boolean writeBlockPaymentMethodDetails(Sheet sheet, UsageBlockForBilling block, boolean summarize)
	throws BillingInformationExporterException {

		PaymentMethod paymentMethod = block.getPaymentMethod();
		BigDecimal percent = block.getBillingPercent();
		
		// If we are not billing anything ignore this block
		if(BigDecimal.ZERO.equals(getBilledCost(block.getTotalCost(), percent, block.getEndDate())))
			return false;
		
		Row row = sheet.createRow(rowNum++);
		
		int cellnum = 0;
		
		row.createCell(cellnum++).setCellValue(block.getProject().getID());

		row.createCell(cellnum++).setCellValue(block.getProject().getPI().getLastName());
		
		row.createCell(cellnum++).setCellValue(block.getUser().getLastName());

		row.createCell(cellnum++).setCellValue(block.getInstrument().getName());
		
		String ffs_project = null;
		if(block.getProject() instanceof BilledProject) {
			ffs_project = block.getProject().isMassSpecExpertiseRequested() ? "YES" : "NO";
		}
		else {
			ffs_project = "NO";
		}
		row.createCell(cellnum++).setCellValue(ffs_project);

		if(!summarize)
			row.createCell(cellnum++).setCellValue(block.getFirstUsageBlockId());
		
		
		row.createCell(cellnum++).setCellValue(block.getStartDateFormated());
		row.createCell(cellnum++).setCellValue(block.getEndDateFormated());
		
		
		row.createCell(cellnum++).setCellValue(block.getTotalHours());
		
		if(!summarize) {
			if(block.getBlocks().size() != 1)
			{
				throw new BillingInformationExporterException("Expected a single block when exporting non-summarized report. Found " + block.getBlocks().size());
			}
			UsageBlockBaseWithRate blk = block.getBlocks().get(0);
			// Hourly rate
			row.createCell(cellnum++).setCellValue(blk.getRate().getRate().toString());
		}

		String poNumber = paymentMethod.getPonumber();
		String uwBudgetNumber = paymentMethod.getUwbudgetNumber();
		String worktag = paymentMethod.getWorktag();

		if (StringUtils.isBlank(poNumber) && StringUtils.isBlank(worktag) && StringUtils.isBlank(uwBudgetNumber)) {
			throw new BillingInformationExporterException("Did not find a Worktag / UW Budget number / PO number for payment method ID: "
					+ paymentMethod.getId());
		}
		if (!StringUtils.isBlank(worktag) && !StringUtils.isBlank(uwBudgetNumber))
		{
			throw new BillingInformationExporterException("Expected only one of Worktag or UW Budget number."
					+ " Found both for payment method Id: " + paymentMethod.getId()
					+ " Worktag: " + worktag + ", Budget number: " + uwBudgetNumber);
		}

		if (!StringUtils.isBlank(poNumber))
		{
			row.createCell(cellnum++).setCellValue(poNumber);
			String poName = paymentMethod.getPaymentMethodName();
			row.createCell(cellnum++).setCellValue(StringUtils.isBlank(poName) ? "" : poName);
		}
		else
		{
			row.createCell(cellnum++).setCellValue(""); // Empty PO Number
			row.createCell(cellnum++).setCellValue(""); // Empty PO Name
		}


		if (!StringUtils.isBlank(worktag))
		{
			row.createCell(cellnum++).setCellValue(worktag);
			String worktagDescr = paymentMethod.getPaymentMethodName();
			row.createCell(cellnum++).setCellValue(StringUtils.isBlank(worktagDescr) ? "" : worktagDescr);
		}
		else if (!StringUtils.isBlank(uwBudgetNumber))
		{
			row.createCell(cellnum++).setCellValue(uwBudgetNumber);
			String budgetName = paymentMethod.getPaymentMethodName();
			row.createCell(cellnum++).setCellValue(StringUtils.isBlank(budgetName) ? "" : budgetName);
		}
		else
		{
			row.createCell(cellnum++).setCellValue(""); // Empty Worktag
			row.createCell(cellnum++).setCellValue(""); // Empty Worktag description
		}


		row.createCell(cellnum++).setCellValue(paymentMethod.getResourceWorktagNotNull());
		row.createCell(cellnum++).setCellValue(paymentMethod.getResourceWorktagDescrNotNull());
		row.createCell(cellnum++).setCellValue(paymentMethod.getAssigneeWorktagNotNull());
		row.createCell(cellnum++).setCellValue(paymentMethod.getAssigneeWorktagDescrNotNull());
		row.createCell(cellnum++).setCellValue(paymentMethod.getActivityWorktagNotNull());
		row.createCell(cellnum++).setCellValue(paymentMethod.getActivityWorktagDescrNotNull());
		
		row.createCell(cellnum++).setCellValue(String.valueOf(paymentMethod.isFederalFunding()));
		
		row.createCell(cellnum++).setCellValue(percent+"%");

		
		// If his block starts before the requested start date flag it.
		// Blocks that end after the requested end date will not be billed.
		String flag = "";
		if(block.getStartDate().before(this.getStartDate())) {
			flag = "*";
		}
		row.createCell(cellnum++).setCellValue(getBilledCost(block.getTotalCost(), percent, block.getEndDate()).toString() + flag);
		row.createCell(cellnum++).setCellValue(getBilledCost(block.getInstrumentCost(), percent, block.getEndDate()).toString());
		row.createCell(cellnum++).setCellValue(getBilledCost(block.getSetupCost(), percent, block.getEndDate()).toString());

		// contact details of the person associated with the payment method
		row.createCell(cellnum++).setCellValue(paymentMethod.getContactFirstName());
		row.createCell(cellnum++).setCellValue(paymentMethod.getContactLastName());
		row.createCell(cellnum++).setCellValue(paymentMethod.getContactEmail());
		row.createCell(cellnum++).setCellValue(paymentMethod.getContactPhone());
		
		return true;
	}

	private List<Project> getAllProjects() throws BillingInformationExporterException
	{
		List<Project> projects = new ArrayList<>();
		try {
			List<Integer> projectIds = ProjectDAO.instance().getScheduledProjects(startDate, endDate);
			for(Integer projectId: projectIds)
			{
				Project project = getProject(projectId);
				projects.add(project);
			}
		} catch (SQLException e) {
			throw new BillingInformationExporterException("Error searching for projects", e);
		}
		return projects;
	}

	private Project getProject(int projectId) throws BillingInformationExporterException {
		Project project = null;
		try {
			project = ProjectFactory.getProject(projectId);
		} catch (InvalidProjectTypeException e) {
			throw new BillingInformationExporterException("Invalid project type requested", e);
		} catch (SQLException e) {
			throw new BillingInformationExporterException("Error loading the project from database. ProjectId: "+projectId, e);
		} catch (InvalidIDException e) {
			throw new BillingInformationExporterException("No project found for ID "+projectId, e);
		}
		return project;
	}

	private List<UsageBlockBase> getSortedUsageBlocksForProject_byStartDate(int projectId)
			throws BillingInformationExporterException {

		// Get the usage blocks for this project where the start OR end of the block falls between the
		// start and end dates.
		List<UsageBlockBase> usageBlocks;
		try {
			usageBlocks = UsageBlockBaseDAO.getUsageBlocksForBilling(projectId, startDate, endDate);
		} catch (SQLException e) {
			throw new BillingInformationExporterException("Error loading usage blocks for project ID: "+projectId, e);
		}

		// sort the blocks by start dates
		Collections.sort(usageBlocks, new Comparator<UsageBlockBase>() {
			@Override
			public int compare(UsageBlockBase o1, UsageBlockBase o2) {
				return o1.getStartDate().compareTo(o2.getStartDate());
			}
		});

		List<UsageBlockBase> blocksToBill = new ArrayList<>();
		// If there are blocks that extend outside the given date range, split them, and save new blocks later if we are exporting an invoice.
		for(UsageBlockBase block: usageBlocks)
		{
			if(block.getStartDate().before(startDate) && block.getEndDate().after(endDate))
			{
				// This should not happen due to limit on how many hours a user can sign up for
				throw new BillingInformationExporterException("Unexpected block. Spans start AND end dates. " + block.toString());
			}

			if(block.getEndDate().after(endDate))
			{
				informListenerBlockUpdated(block); // To be updated later

				UsageBlockBase billBlock = new UsageBlockBase();
				block.copyTo(billBlock);
				billBlock.setEndDate(endDate);
				blocksToBill.add(billBlock);
			}
			else if(block.getStartDate().before(startDate))
			{
				// This SHOULD NOT happen, unless the previous billing cycle was not invoiced.
				informListenerBlockUpdated(block); // To be updated later.

				UsageBlockBase billBlock = new UsageBlockBase();
				block.copyTo(billBlock);
				billBlock.setStartDate(startDate);
				billBlock.setSetupBlock(false); // The previous split block will be the setup block
				blocksToBill.add(billBlock);
			}
			else
			{
				blocksToBill.add(block);
			}
		}
		return blocksToBill;
	}

	private void checkDates() throws BillingInformationExporterException {
		if(startDate == null) {
			throw new BillingInformationExporterException("No start date was specified");
		}
		if(endDate == null) {
			throw new BillingInformationExporterException("No end date was specified");
		}
		if(startDate.equals(endDate) || startDate.after(endDate)) {
			throw new BillingInformationExporterException("Start date is after end date");
		}
	}

	BigDecimal getBilledCost(BigDecimal blockCost, BigDecimal percent, Date blockEndTime) {

		// If this block extends beyond the requested end date it will be billed in the next cycle
		if(blockEndTime.after(this.endDate)) {
			return BigDecimal.ZERO;
		}

		return getPercentCost(blockCost, percent);
	}

	BigDecimal getPercentCost(BigDecimal blockCost, BigDecimal percent) {

		return CostUtils.calcCost(blockCost, percent);
	}

	private void informListenerBlockExported(UsageBlockBase block) throws BillingInformationExporterException {
		if(this.listener != null)
			listener.blockExported(block);
	}

	private void informListenerBlockUpdated(UsageBlockBase block) throws BillingInformationExporterException {
		// _blocksToUpdate.add(block); // TODO: remove this
		if(this.listener != null)
		{
			listener.updateBlock(block);
		}
	}

	private void informListenerExportDone() throws BillingInformationExporterException {
		if(this.listener != null)
			listener.exportDone();
	}

	public void setBillinInformationExporterListener(InvoiceBlockCreator invoiceBlockCreator)
	{
		this.listener = invoiceBlockCreator;
	}
}
