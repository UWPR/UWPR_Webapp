-- Exact row count for every table in mainDb and pr. Read-only.
--
-- Run before section 3 and again after. information_schema.table_rows is exact while a table
-- is MyISAM and sampled once it is InnoDB, so after the conversion there is no way back to an
-- exact before. COUNT(*) is the only figure that can be compared across the change.
--
-- Every count must be identical either side. A converted table that lost rows is the one
-- failure of section 3 that its own output does not show.

SELECT 'maindb.config_msdapl_webapp' AS tbl, COUNT(*) AS rows_now FROM maindb.`config_msdapl_webapp`
UNION ALL SELECT 'maindb.grants' AS tbl, COUNT(*) AS rows_now FROM maindb.`grants`
UNION ALL SELECT 'maindb.instrumentlog' AS tbl, COUNT(*) AS rows_now FROM maindb.`instrumentlog`
UNION ALL SELECT 'maindb.instrumentrate' AS tbl, COUNT(*) AS rows_now FROM maindb.`instrumentrate`
UNION ALL SELECT 'maindb.instrumentusage' AS tbl, COUNT(*) AS rows_now FROM maindb.`instrumentusage`
UNION ALL SELECT 'maindb.instrumentusagepayment' AS tbl, COUNT(*) AS rows_now FROM maindb.`instrumentusagepayment`
UNION ALL SELECT 'maindb.invoice' AS tbl, COUNT(*) AS rows_now FROM maindb.`invoice`
UNION ALL SELECT 'maindb.invoiceinstrumentusage' AS tbl, COUNT(*) AS rows_now FROM maindb.`invoiceinstrumentusage`
UNION ALL SELECT 'maindb.ncbi_taxonomy' AS tbl, COUNT(*) AS rows_now FROM maindb.`ncbi_taxonomy`
UNION ALL SELECT 'maindb.notice' AS tbl, COUNT(*) AS rows_now FROM maindb.`notice`
UNION ALL SELECT 'maindb.paymentmethod' AS tbl, COUNT(*) AS rows_now FROM maindb.`paymentmethod`
UNION ALL SELECT 'maindb.projectgrant' AS tbl, COUNT(*) AS rows_now FROM maindb.`projectgrant`
UNION ALL SELECT 'maindb.projectgroup' AS tbl, COUNT(*) AS rows_now FROM maindb.`projectgroup`
UNION ALL SELECT 'maindb.projectpaymentmethod' AS tbl, COUNT(*) AS rows_now FROM maindb.`projectpaymentmethod`
UNION ALL SELECT 'maindb.projectresearcher' AS tbl, COUNT(*) AS rows_now FROM maindb.`projectresearcher`
UNION ALL SELECT 'maindb.ratetype' AS tbl, COUNT(*) AS rows_now FROM maindb.`ratetype`
UNION ALL SELECT 'maindb.tblcollaboration' AS tbl, COUNT(*) AS rows_now FROM maindb.`tblcollaboration`
UNION ALL SELECT 'maindb.tblexperiments' AS tbl, COUNT(*) AS rows_now FROM maindb.`tblexperiments`
UNION ALL SELECT 'maindb.tblprojectexperiment' AS tbl, COUNT(*) AS rows_now FROM maindb.`tblprojectexperiment`
UNION ALL SELECT 'maindb.tblprojectproteininference' AS tbl, COUNT(*) AS rows_now FROM maindb.`tblprojectproteininference`
UNION ALL SELECT 'maindb.tblprojects' AS tbl, COUNT(*) AS rows_now FROM maindb.`tblprojects`
UNION ALL SELECT 'maindb.tblresearchers' AS tbl, COUNT(*) AS rows_now FROM maindb.`tblresearchers`
UNION ALL SELECT 'maindb.tblusers' AS tbl, COUNT(*) AS rows_now FROM maindb.`tblusers`
UNION ALL SELECT 'maindb.tblyatescyclems2data' AS tbl, COUNT(*) AS rows_now FROM maindb.`tblyatescyclems2data`
UNION ALL SELECT 'maindb.tblyatescycles' AS tbl, COUNT(*) AS rows_now FROM maindb.`tblyatescycles`
UNION ALL SELECT 'maindb.tblyatescyclesqtdata' AS tbl, COUNT(*) AS rows_now FROM maindb.`tblyatescyclesqtdata`
UNION ALL SELECT 'maindb.tblyatesresultpeptide' AS tbl, COUNT(*) AS rows_now FROM maindb.`tblyatesresultpeptide`
UNION ALL SELECT 'maindb.tblyatesrun' AS tbl, COUNT(*) AS rows_now FROM maindb.`tblyatesrun`
UNION ALL SELECT 'maindb.tblyatesrunresult' AS tbl, COUNT(*) AS rows_now FROM maindb.`tblyatesrunresult`
UNION ALL SELECT 'maindb.tblyrcgroupmembers' AS tbl, COUNT(*) AS rows_now FROM maindb.`tblyrcgroupmembers`
UNION ALL SELECT 'maindb.tblyrcgroups' AS tbl, COUNT(*) AS rows_now FROM maindb.`tblyrcgroups`
UNION ALL SELECT 'maindb.timeblock' AS tbl, COUNT(*) AS rows_now FROM maindb.`timeblock`
UNION ALL SELECT 'pr.collaborationrejected' AS tbl, COUNT(*) AS rows_now FROM pr.`collaborationrejected`
UNION ALL SELECT 'pr.collaborationrejectioncause' AS tbl, COUNT(*) AS rows_now FROM pr.`collaborationrejectioncause`
UNION ALL SELECT 'pr.externaldatalocations' AS tbl, COUNT(*) AS rows_now FROM pr.`externaldatalocations`
UNION ALL SELECT 'pr.files' AS tbl, COUNT(*) AS rows_now FROM pr.`files`
UNION ALL SELECT 'pr.projectfiles' AS tbl, COUNT(*) AS rows_now FROM pr.`projectfiles`
UNION ALL SELECT 'pr.projectrawdatasummary' AS tbl, COUNT(*) AS rows_now FROM pr.`projectrawdatasummary`
UNION ALL SELECT 'pr.projectreportreminder' AS tbl, COUNT(*) AS rows_now FROM pr.`projectreportreminder`
UNION ALL SELECT 'pr.projectreviewer' AS tbl, COUNT(*) AS rows_now FROM pr.`projectreviewer`
UNION ALL SELECT 'pr.tblbilledproject' AS tbl, COUNT(*) AS rows_now FROM pr.`tblbilledproject`;
