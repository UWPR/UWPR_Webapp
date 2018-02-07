/**
 * InvoiceBlockCreator.java
 * @author Vagisha Sharma
 * Jul 16, 2011
 */
package org.uwpr.costcenter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.uwpr.instrumentlog.*;
import org.yeastrc.db.DBConnectionManager;

/**
 * 
 */
public class InvoiceBlockCreator implements
		BillingInformationExporterListener {

	private final Invoice invoice;
	private final InvoiceInstrumentUsageDAO invoiceBlockDao;
	private final InstrumentUsageDAO instrumentUsageDao;

	private List<InvoiceInstrumentUsage> invoicedBlocks = new ArrayList<>();
	private List<UsageBlockBase> _updatedBlocks = new ArrayList<>(); // Blocks whose start or end date has been updated;
	private List<UsageBlock> _newBlocks = new ArrayList<>(); // Blocks that have been added.

	public InvoiceBlockCreator (Invoice invoice) {
		this.invoice = invoice;
		invoiceBlockDao = InvoiceInstrumentUsageDAO.getInstance();
		instrumentUsageDao = InstrumentUsageDAO.getInstance();
	}
	
	@Override
	public void blockExported(UsageBlockBase block) throws BillingInformationExporterException
	{
		InvoiceInstrumentUsage oldSavedBlock = null;
		try {
			oldSavedBlock = invoiceBlockDao.getInvoiceBlock(block.getID());
		}
		catch(SQLException e) {
			throw new BillingInformationExporterException("Error getting results from invoiceInstrumentUsage table.", e);
		}

		// If there is already an entry in the table for this block it means this block
		// has already been included in an invoice.  If the invoice ID we have been given
		// is different from the one associated with this block it means that this block
		// is being included in multiple invoices.  This should never happen
		if(oldSavedBlock != null) {
			if(oldSavedBlock.getInvoiceId() != invoice.getId()) {
				throw new BillingInformationExporterException("Usage block with ID "+block.getID()+" is already part of another invoice");
			}
		}

		// Add to blocks that will be invoiced
		InvoiceInstrumentUsage invoiceBlock = new InvoiceInstrumentUsage();
		invoiceBlock.setInvoiceId(invoice.getId());
		invoiceBlock.setInstrumentUsageId(block.getID());
		invoicedBlocks.add(invoiceBlock);
	}

	public void updateBlock(UsageBlockBase block) throws BillingInformationExporterException
	{
		if(block.getEndDate().after(invoice.getBillEndDate()))
		{
			UsageBlockBase updatedBlock = new UsageBlockBase();
			block.copyTo(updatedBlock);
			updatedBlock.setEndDate(invoice.getBillEndDate());
			_updatedBlocks.add(updatedBlock);

			UsageBlock nextCycleBlock = new UsageBlock(); // DO NOT reset the block ID here.  We will use it later to look up payment methods.
			block.copyTo(nextCycleBlock);
			nextCycleBlock.setStartDate(invoice.getBillEndDate());
			nextCycleBlock.setSetupBlock(false);
			_newBlocks.add(nextCycleBlock);
		}
		else if(block.getStartDate().before(invoice.getBillStartDate()))
		{
			// This SHOULD NOT happen, unless blocks in the previous billing cycle were not invoiced.
			try
			{
				InvoiceInstrumentUsage invoicedBlock = InvoiceInstrumentUsageDAO.getInstance().getInvoiceBlock(block.getID());
				if(invoicedBlock != null)
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
			_updatedBlocks.add(updatedBlock);

			UsageBlock prevCycleBlock = new UsageBlock(); // DO NOT reset the block ID here.  We will use it later to look up payment methods.
			block.copyTo(prevCycleBlock);
			prevCycleBlock.setEndDate(invoice.getBillStartDate());
			_newBlocks.add(prevCycleBlock);
		}
	}

	@Override
	public void exportDone() throws BillingInformationExporterException {

		Connection conn = null;
		try {
			conn = DBConnectionManager.getMainDbConnection();
			conn.setAutoCommit(false);
			invoiceBlockDao.saveBlocks(conn, invoicedBlocks);


			// Update block start/end dates
			if(_updatedBlocks.size() > 0)
			{
				InstrumentUsageDAO.getInstance().updateBlocksDates(conn, _updatedBlocks, "Update due to invoicing. Invoice: " + invoice.toString());
			}

			// If there are new blocks to be added
			if(_newBlocks.size() > 0)
			{
				for(UsageBlock block: _newBlocks)
				{
					// We are using the ID of the block this was split from to get the payment methods.
					List<InstrumentUsagePayment> usagePayments = InstrumentUsagePaymentDAO.getInstance().getPaymentsForUsage(conn, block.getID());
					block.setPayments(usagePayments);
				}
				InstrumentUsageDAO.getInstance().saveUsageBlocks(conn, _newBlocks, "Added due to invoicing. Invoice: " + invoice.toString());
			}

			conn.commit();
		}
		catch(SQLException e)
		{
			throw new BillingInformationExporterException("Error saving invoice block for invoice ID: " + invoice.getId(), e);
		}
		finally
		{
			if(conn != null) try {conn.close();} catch(SQLException e){}
		}
	}
}
