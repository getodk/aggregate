package org.odk.aggregate.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The only purpose of this servlet is to insert Set-Cookie META tags into the
 * HEAD section of the Briefcase.html page being vended.  BRIEFCASE_BODY is a
 * string representation of the BODY section of Briefcase.html from the Briefcase project.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class BriefcaseServlet extends ServletUtilBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5121979982542017091L;

	/**
	 * URI from base
	 */
	public static final String ADDR = "Briefcase";

	/**
	 * Title for generated webpage
	 */
	private static final String TITLE_INFO = "ODK Briefcase";
	
	/**
	 * Copy of the body of Briefcase.html (see Briefcase project)
	 */
	private static final String BRIEFCASE_BODY = 
"<h1>Download data from ODK Aggregate</h1>" +
"\n<p>Creates .csv files and binary data files on your local filesystem.</p>" +
"\n<p><a href=\"#Csv\">Import .csv files manually</a> to preserve non-latin text.</p>" +
"\n<p><a href=\"#Cert\">Import the signing certificate</a> to stop the security warnings.</p>" +
"\n<p>You must already be logged into Aggregate from within this browser in order to access the uploaded data.</p>" +
"\n<h3>Retrieving All Data</h3>" +
"\n<p>To extract all data from Aggregate, repeat the following steps for all form identifiers:</p>" +
"\n<ol>" +
"\n<li>Specify a download directory location (this cannot already contain downloaded data).</li>" +
"\n<li>Choose `Download nested repeat groups.`</li>" +
"\n<li>Choose `Download binary data and replace server URL with the local filename in the csv.` (e.g., images, audio, video).</li>" +
"\n<li>For the `odkId`, specify a <em>form identifier</em> (shown on Aggregate's List Forms page). The odkId in an " +
"\n<a href=\"#OdkId\">xpath-like expression</a> that can describe a specific subset of data.</li>" +
"\n<li>Choose `Retrieve`</li>" +
"\n<li>Repeat for all form identifiers.</li>" +
"\n</ol>" +
"\n<p>Should your download fail, read how to <a href=\"#Resume\">resume a failed download attempt.</a></p>" +
"\n<object type=\"application/x-java-applet\" height=\"600\" width=\"700\" >" +
"\n  <param name=\"jnlp_href\" value=\"briefcase/opendatakit-briefcase.jnlp\" />" +
"\n  <param name=\"mayscript\" value=\"true\" />" +
"\n</object>" +
"\n<script language=\"JavaScript\"><!--" +
"\n  document.write('<p>Cookies: '+document.cookie+'</p>')" +
"\n  document.write('<p>Location: '+location.href+'</p>');" +
"\n  //-->" +
"\n</script>" +
"\n<h3><a name=\"OdkId\">The xpath-like OdkId</a></h3>" +
"\n<p>The basic structure of an odkId is:</p>" +
"\n<pre>form-identifier/top-level-tag/repeat-group/nested-repeat-group/...</pre>" +
"\n<p>Where:</p><ul>" +
"\n<li><code>form-identifier</code>: the form identifier shown on the List Forms page.</li>" +
"\n<li><code>top-level-tag</code>: the name of the top-level data tag (the one with the <code>form-identifier</code> as an id attribute value or xmlns name).</li>" +
"\n<li><code>repeat-group</code>: the name of a repeating data tag within the <code>top-level-tag</code> instance.</li>" +
"\n<li><code>nested-repeat-group</code>: the name of a repeating data tag nested within the <code>repeat-group</code>, above.</li>" +
"\n</ul>" +
"\n<p>An xpath-like filter condition can be applied to the last and next-to-last elements of the odkId. " +
"\nThe only filter criteria currently supported filters on the unique key of a data record.  For example:</p>" +
"\n<ul>" +
"\n<li><code>HouseholdSurvey1/HouseholdSurvey/ChildrenOfHousehold</code> returns all <code>ChildrenOfHousehold</code> data for all submitted <code>HouseholdSurvey1</code> forms (all children in all households).</li>" +
"\n<li><code>HouseholdSurvey1/HouseholdSurvey/ChildrenOfHousehold[@key=\"bb\"]</code> returns the single <code>ChildrenOfHousehold</code> with the unique key \"bb\" (a specific child).</li>" +
"\n<li><code>HouseholdSurvey1/HouseholdSurvey[@key=\"aaa\"]/ChildrenOfHousehold</code> returns all <code>ChildrenOfHousehold</code> data that are associated with the <code>HouseholdSurvey</code> with unique key \"aaa\" (all the children of a single household).</li>" +
"\n</ul>" +
"\n<h3><a name=\"Resume\">Resuming a failed download attempt</a></h3>" +
"\n<p>If your download begins fetching files and fails before completing the retrieval, " +
"\nthe <code>Manifest.txt</code> file in your download directory " +
"\nwill contain information like this:</p>" +
"\n<pre>" +
"\nBriefcaseVersion: 1.0" +
"\nRunDate: Aug 10, 2010" +
"\nServerUrl: http://localhost:8888/" +
"\nOdkId-1: HouseholdSurvey1/HouseholdSurvey/ChildrenOfHousehold" +
"\nCsvFileName-1: \\csvData\\ChildrenOfHousehold.HouseholdSurvey.HouseholdSurvey1.csv" +
"\nOdkId-2: HouseholdSurvey1/HouseholdSurvey" +
"\nCsvFileName-2: \\csvData\\HouseholdSurvey.HouseholdSurvey1.csv" +
"\nLastCursor-2: E9oBpgFqoAFqC29wZW5kYXRha2l0cpABCxJjCgAaI0hvdXNlaG9sZFN1cnZleTFDaGlsZHJlbk9mSG91c2Vob2xkIzAFcjYaClBBUkVOVF9LRVkgAComY2oLb3BlbmRhdGFraXRzehBIb3VzZWhvbGRTdXJ2ZXkxgAECdGQkDAsSI0hvdXNlaG9sZFN1cnZleTFDaGlsZHJlbk9mSG91c2Vob2xkGAUMggEA4AEAFA" +
"\nLastKEY-2: http://localhost:8888/csvFragment?odkId=HouseholdSurvey1%2FHouseholdSurvey%5B%40key%3D%22agtvcGVuZGF0YWtpdHIWCxIQSG91c2Vob2xkU3VydmV5MRgCDA%22%5D" +
"\nCompletion-Status: Failure" +
"\n</pre>" +
"\n<p>To resume the fetch, simply locate the `LastCursor-x:` field (there will be only one in the file), " +
"\ncopy its value into the `LastCursor-x:` prompt above, " +
"\nand do the same for the corresponding `LastKEY-x:` and `OdkId-x:` fields.</p>" +
"\n<p>Resumed fetches must download data to a new download directory.  While the applet ensures that " +
"\nthe top-level form entries are never duplicated and that processing picks up where it ended, " +
"\nthere may be duplicates in the repeated groups and binary data nested under that top-level form.</p>" +
"\n<p> Resolving the duplicates is a manual process.  Binary data files have repeatably unique " +
"\nnames and can simply be consolidated into a single directory; any duplicates should be identical " +
"\ncopies of the same file.  The csv files should be manually concatenated then sorted by their " +
"\nrightmost column (the KEY) column), and any duplicate rows removed.</p>" +
"\n<h2>Known Issues</h2>" +
"\n<h3><a name=\"Csv\">Import .csv file contents manually</a></h3>" +
"\n<p>The csv files should be interpreted as containing UTF-8 characters. Unfortunately, browsing to the file " +
"\nand double-clicking to open Excel will cause them to be loaded into a spreadsheet with the default ANSI (latin) " +
"\ncharacter encoding.  Instead, you need to:</p>" +
"\n<ol><li>Open the Excel 2007 application</li>" +
"\n<li>create a new workbook or open an existing workbook</li>" +
"\n<li>choose Data/Import Text</li>" +
"\n<li>Browse to the downloaded .csv file; hit Open</li>" +
"\n<li><b>Text Import Wizard - Step 1 of 3</b> opens.</li>" +
"\n<li>Choose `Delimited`</li>" +
"\n<li>Under `File origin:` select <code>65001 : Unicode (UTF-8)</code></li>" +
"\n<li>Hit Next to advance to <b>Text Import Wizard - Step 2 of 3</b></li>" +
"\n<li>Choose only `Comma` for the delimiters.</li>" +
"\n<li>Uncheck the `Treat consecutive delimiters as one` checkbox</li>" +
"\n<li>Hit Finish or, choose Next to define the formatting for specific columns.</li></ol>" +
"\n<p>The data will now be loaded into Excel 2007 with UTF-8 characters properly interpreted.</p>" +
"\n<h3><a name=\"Cert\">Import the signing certificate</a></h3>" +
"\n<p>This applet is signed with a self-signed certificate created by the OpenDataKit team.  If you are " +
"\naccessing this page frequently, you may want to import the certificate into your browser's certificate " +
"\nstore to suppress the security warnings.  Note that whenever you import a certificate, you are trusting " +
"\nthe owner of that certificate with your system security, as it will allow any software that the owner " +
"\nsigns to be transparently executed on your system. Click <a href=\"/briefcase/OpenDataKit.cer\">here</a> to " +
"\ndownload the certificate.  Then import it to suppress the security warnings; on Windows systems, " +
"\nthe most restricted way to import the certificate would be through <code>Control Panel/Java</code>. Go to " +
"\nthe <code>Security</code> tab, click <code>Certificates...</code> then <code>Import</code> to import " +
"\nthe certificate.</p>";

	/**
	 * Handler for HTTP Get request to create blank page that is navigable
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		// verify user is logged in
		if (!verifyCredentials(req, resp)) {
			return;
		}

		String cookieSet = "";
		Cookie[] cookies = req.getCookies();
		if ( cookies != null ) {
			for ( Cookie c : cookies ) {
				String aDef = "<META HTTP-EQUIV=\"Set-Cookie\" CONTENT=\"" + 
								c.getName() + "=" + c.getValue() + "\" />\n";
				resp.addCookie(c);
				cookieSet += aDef;
				
			}
		}
		String headContent = cookieSet;
		beginBasicHtmlResponse(TITLE_INFO, headContent, resp, req, true); // header
	    PrintWriter out = resp.getWriter();
	    out.write(BRIEFCASE_BODY);
		finishBasicHtmlResponse(resp);
		resp.setStatus(200);
	}
}
