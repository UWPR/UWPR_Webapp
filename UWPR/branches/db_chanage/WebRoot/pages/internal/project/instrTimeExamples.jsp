<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>

<html>
<head>
 <yrcwww:title />

 <link REL="stylesheet" TYPE="text/css" HREF="/pr/css/global.css">

</head>
<body>
<yrcwww:contentbox title="Examples">
Examples of instrument time justification:
<ul>
<li>
We have 10 samples and want to run 3 replicates each on a high mass accuracy instrument like the LTQ-Orbitrap 
as it is required for labelfree quantification algorithm.
</li>
<li>
We intend to use a test sample to optimize the method (approx. 5-10 LC-MS/MS runs). 
Once the method is established we'll analyze 6 sample in triplicate. We require the use of the LTQ-FT as we plan to use ECD and IRMPD.
</li>
<li>
We will analyze our samples by direct infusion, we estimate we will be acquiring ~50 RAW files in one day of instrument time, 
we require to use a high resolution instrument to get appropriate mass accuracy.
</li>
<li>
The numbers of runs and instrument type are explained in the abstract.
</li>
</ul>
</yrcwww:contentbox>
</body>
</html>