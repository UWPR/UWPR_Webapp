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

import org.uwpr.instrumentlog.UsageBlockBase;
import org.yeastrc.db.DBConnectionManager;

/**
 * 
 */
public class InvoiceBlockCreator implements
		BillingInformationExporterListener {

	private final Invoice invoice;
	private final InvoiceInstrumentUsageDAO invoiceBlockDao;

	List<InvoiceInstrumentUsage> invoicedBlocks = new ArrayList<>();

	public InvoiceBlockCreator (Invoice invoice) {
		this.invoice = invoice;
		invoiceBlockDao = InvoiceInstrumentUsageDAO.getInstance();
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

	@Override
	public void exportDone() throws BillingInformationExporterException {

		Connection conn = null;
		try {
			conn = DBConnectionManager.getMainDbConnection();
			conn.setAutoCommit(false);
			invoiceBlockDao.saveBlocks(conn, invoicedBlocks);
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
