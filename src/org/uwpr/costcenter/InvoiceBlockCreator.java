/**
 * InvoiceBlockCreator.java
 * @author Vagisha Sharma
 * Jul 16, 2011
 */
package org.uwpr.costcenter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.uwpr.instrumentlog.*;
import org.yeastrc.db.DBConnectionManager;
import org.yeastrc.db.DbErrorUtils;

/**
 * 
 */
public class InvoiceBlockCreator implements
		BillingInformationExporterListener {

	private static final Logger log = LogManager.getLogger(InvoiceBlockCreator.class);

	private final Invoice invoice;
	// Admin exporting the invoice.  Recorded as the updater of each split block and in its log rows.
	private final int researcherId;
	private final InvoiceInstrumentUsageDAO invoiceBlockDao;
	private final InstrumentUsageDAO instrumentUsageDao;

	private List<InvoiceInstrumentUsage> invoicedBlocks = new ArrayList<>();
	private List<UsageBlockBase> _updatedBlocks = new ArrayList<>(); // Blocks whose start or end date has been updated;
	private List<UsageBlock> _newBlocks = new ArrayList<>(); // Blocks that have been added.

	public InvoiceBlockCreator (Invoice invoice, int researcherId) {
		this.invoice = invoice;
		this.researcherId = researcherId;
		invoiceBlockDao = InvoiceInstrumentUsageDAO.getInstance();
		instrumentUsageDao = InstrumentUsageDAO.getInstance();
	}
	
	@Override
	public void blockExported(UsageBlockBase block) throws BillingInformationExporterException
	{
		List<InvoiceInstrumentUsage> allRows;
		try {
			allRows = invoiceBlockDao.getAllInvoiceRowsForUsage(block.getID());
		}
		catch(SQLException e) {
			throw new BillingInformationExporterException("Error getting results from invoiceInstrumentUsage table.", e);
		}

		// A block with any invoiceInstrumentUsage row is already invoiced.  A row for this invoice means the
		// block is already on it (a re-export) -- skip it so saveBlocks does not add a duplicate.  A row for
		// any other invoice means the block is already invoiced elsewhere, or the row is an orphan left by a
		// deleted invoice -- refuse rather than invoice over it.
		boolean alreadyOnThisInvoice = false;
		List<String> otherRows = new ArrayList<>();
		for(InvoiceInstrumentUsage row: allRows) {
			if(row.getInvoiceId() == invoice.getId()) {
				alreadyOnThisInvoice = true;
			}
			else {
				otherRows.add("id " + row.getId() + " (invoice " + row.getInvoiceId() + ")");
			}
		}
		if(!otherRows.isEmpty()) {
			throw new BillingInformationExporterException("Usage block " + block.getID()
					+ " already has an invoiceInstrumentUsage row for another invoice, " + otherRows
					+ ".  Resolve it before invoicing this block.");
		}

		// Add to blocks that will be invoiced, unless the block already has a row for this invoice, which
		// would make saveBlocks insert a duplicate.
		if(!alreadyOnThisInvoice) {
			InvoiceInstrumentUsage invoiceBlock = new InvoiceInstrumentUsage();
			invoiceBlock.setInvoiceId(invoice.getId());
			invoiceBlock.setInstrumentUsageId(block.getID());
			invoicedBlocks.add(invoiceBlock);
		}
	}

	public void updateBlock(UsageBlockBase block) throws BillingInformationExporterException
	{
		if(block.getEndDate().after(invoice.getBillEndDate()))
		{
			UsageBlockBase updatedBlock = new UsageBlockBase();
			block.copyTo(updatedBlock);
			updatedBlock.setEndDate(invoice.getBillEndDate());
			updatedBlock.setUpdaterResearcherID(researcherId);
			_updatedBlocks.add(updatedBlock);

			UsageBlock nextCycleBlock = new UsageBlock(); // DO NOT reset the block ID here.  We will use it later to look up payment methods.
			block.copyTo(nextCycleBlock);
			nextCycleBlock.setStartDate(invoice.getBillEndDate());
			nextCycleBlock.setSetupBlock(false);
			nextCycleBlock.setUpdaterResearcherID(researcherId);
			_newBlocks.add(nextCycleBlock);
		}
		else if(block.getStartDate().before(invoice.getBillStartDate()))
		{
			// This SHOULD NOT happen, unless blocks in the previous billing cycle were not invoiced.
			try
			{
				if(InvoiceInstrumentUsageDAO.getInstance().isBlockInvoiced(block.getID()))
				{
					throw new BillingInformationExporterException("Cannot split block.  It has already been invoiced. " + block.toString());
				}
			} catch (SQLException e)
			{
				throw new BillingInformationExporterException("Error getting invoice status for block " + block.toString());
			}

			// If the block was not invoiced, split it
			UsageBlockBase updatedBlock = new UsageBlockBase();
			block.copyTo(updatedBlock);
			updatedBlock.setStartDate(invoice.getBillStartDate());
			updatedBlock.setSetupBlock(false);
			updatedBlock.setUpdaterResearcherID(researcherId);
			_updatedBlocks.add(updatedBlock);

			UsageBlock prevCycleBlock = new UsageBlock(); // DO NOT reset the block ID here.  We will use it later to look up payment methods.
			block.copyTo(prevCycleBlock);
			prevCycleBlock.setEndDate(invoice.getBillStartDate());
			prevCycleBlock.setUpdaterResearcherID(researcherId);
			_newBlocks.add(prevCycleBlock);
		}
	}

	@Override
	public void exportDone() throws BillingInformationExporterException {

		Connection conn = null;
		boolean committed = false;
		try {
			conn = DBConnectionManager.getMainDbConnection();
			conn.setAutoCommit(false);
			invoiceBlockDao.saveBlocks(conn, invoicedBlocks);


			// Update block start/end dates
			if(_updatedBlocks.size() > 0)
			{
				instrumentUsageDao.updateBlocksDates(conn, _updatedBlocks, "Update due to invoicing. Invoice: " + invoice.toString());
			}

			// If there are new blocks to be added
			if(_newBlocks.size() > 0)
			{
				for(UsageBlock block: _newBlocks)
				{
					// block holds the ID of the block it was split from until saveUsageBlocks sets the new block's ID.
					int splitFromId = block.getID();
					List<InstrumentUsagePayment> usagePayments = InstrumentUsagePaymentDAO.getInstance().getPaymentsForUsage(conn, splitFromId);
					// getPaymentsForUsage returns null when the lookup fails, and an empty list for a block with no payments.
					if(usagePayments == null)
					{
						throw new BillingInformationExporterException("Error getting the payments of block " + splitFromId
								+ " for invoice ID: " + invoice.getId() + ".");
					}
					block.setPayments(usagePayments);

					String errorMessage = instrumentUsageDao.saveUsageBlocks(conn, Collections.singletonList(block),
							"Added due to invoicing. Split from block " + splitFromId + ". Invoice: " + invoice.toString(), researcherId);
					if(errorMessage != null)
					{
						String detail = "Error saving the block split from block " + splitFromId
								+ " for invoice ID: " + invoice.getId() + ".";
						if(DbErrorUtils.isRetryMessage(errorMessage))
						{
							// saveUsageBlocks already turned a deadlock into the message the admin should see, so
							// the block and invoice ids go to the log rather than in front of it.
							log.error(detail + " " + errorMessage);
							throw new BillingInformationExporterException(errorMessage);
						}
						throw new BillingInformationExporterException(detail + " " + errorMessage);
					}
				}
			}

			conn.commit();
			committed = true;
		}
		catch(SQLException e)
		{
			throw new BillingInformationExporterException("Error saving invoice block for invoice ID: " + invoice.getId(), e);
		}
		finally
		{
			// The BillingInformationExporterException thrown above for a block that cannot be split, a
			// missing payment list or a failed save also leaves this transaction open.
			DBConnectionManager.endTransactionAndClose(conn, committed);
		}
	}
}
