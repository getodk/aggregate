package org.odk.aggregate.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

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
			boolean first = true;
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
	    out.write(
"		<h1>Download data from ODK Aggregate</h1>" +
"		<p>Creates .csv files and binary data files on your local filesystem.</p>" +
"		<p><a href=\"#Cert\">Import the signing certificate</a> to stop the security warnings.</p>" +
"		<p>You must already be logged into Aggregate from within this browser in order to access the uploaded data.</p>" +
"		<h3>Retrieving All Data</h3>" +
"		<p>To extract all data from Aggregate, repeat the following steps for all form identifiers:</p>" +
"		<ol>" +
"		<li>Specify a download directory location (this cannot already contain downloaded data).</li>" +
"		<li>Choose `Download nested repeat groups.`</li>" +
"		<li>Choose `Download binary data and replace server URL with the local filename in the csv.` (e.g., images, audio, video).</li>" +
"		<li>For the odkId, specify a form identifier (shown on Aggregate's List Forms page).</li>" +
"		<li>Choose `Retrieve`</li>" +
"		<li>Repeat for all form identifiers.</li>" +
"		</ol>" +
"		<object type=\"application/x-java-applet\" height=\"600\" width=\"700\" > " +
"		  <param name=\"jnlp_href\" value=\"/briefcase/opendatakit-briefcase.jnlp\" /> " +
"		  <param name=\"mayscript\" value=\"true\" /> " +
"		</object>" +
"       <script language=\"JavaScript\"><!--\n" +
"       document.write('<p>Cookies: '+document.cookie+'</p>')\n" +
"       document.write('<p>Location: '+location.href+'</p>')\n" +
"       //--></script>" +
"		<h3>About the OdkId</h3>" +
"		<p>The basic structure of an odkId is:</p>" +
"		<pre>form-identifier/top-level-tag/repeat-group/nested-repeat-group/...</pre>" +
"		<p>Where:</p><ul>" +
"		<li><code>form-identifier</code>: the form identifier shown on the List Forms page.</li>" +
"		<li><code>top-level-tag</code>: the name of the top-level data tag (the one with the <code>form-identifier</code> as an id attribute value or xmlns name).</li>" +
"		<li><code>repeat-group</code>: the name of a repeating data tag within the <code>top-level-tag</code> instance.</li>" +
"		<li><code>nested-repeat-group</code>: the name of a repeating data tag nested within the <code>repeat-group</code>, above.</li>" +
"		</ul>" +
"		<p>An xpath-like filter condition can be applied to the last and next-to-last elements of the odkId. The only filter criteria" +
"		 currently supported filters on the unique key of a data record.  For example:</p>" +
"		<ul>" +
"		<li><code>HouseholdSurvey1/HouseholdSurvey/ChildrenOfHousehold</code> returns all <code>ChildrenOfHousehold</code> data for all submitted <code>HouseholdSurvey1</code> forms (all children in all households).</li>" +
"		<li><code>HouseholdSurvey1/HouseholdSurvey/ChildrenOfHousehold[@key=\"bb\"]</code> returns the single <code>ChildrenOfHousehold</code> with the unique key \"bb\" (a specific child).</li>" +
"		<li><code>HouseholdSurvey1/HouseholdSurvey[@key=\"aaa\"]/ChildrenOfHousehold</code> returns all <code>ChildrenOfHousehold</code> data that are associated with the <code>HouseholdSurvey</code> with unique key \"aaa\" (all the children of a single household).</li>" +
"		</ul>" +
"		<h3>Resuming a failed download attempt</h3>" +
"		<p>If your download begins fetching files and fails before completing the retrieval, the <code>Manifest.txt</code> file in your download directory " +
"		will contain information like this:</p>" +
"		<pre>\n" +
"BriefcaseVersion: 1.0\n" +
"RunDate: Aug 10, 2010\n" +
"ServerUrl: http://localhost:8888/\n" +
"OdkId-1: HouseholdSurvey1/HouseholdSurvey/ChildrenOfHousehold\n" +
"CsvFileName-1: \\csvData\\ChildrenOfHousehold.HouseholdSurvey.HouseholdSurvey1.csv\n" +
"OdkId-2: HouseholdSurvey1/HouseholdSurvey\n" +
"CsvFileName-2: \\csvData\\HouseholdSurvey.HouseholdSurvey1.csv\n" +
"LastCursor-2: E9oBpgFqoAFqC29wZW5kYXRha2l0cpABCxJjCgAaI0hvdXNlaG9sZFN1cnZleTFDaGlsZHJlbk9mSG91c2Vob2xkIzAFcjYaClBBUkVOVF9LRVkgAComY2oLb3BlbmRhdGFraXRzehBIb3VzZWhvbGRTdXJ2ZXkxgAECdGQkDAsSI0hvdXNlaG9sZFN1cnZleTFDaGlsZHJlbk9mSG91c2Vob2xkGAUMggEA4AEAFA\n" +
"LastKEY-2: http://localhost:8888/csvFragment?odkId=HouseholdSurvey1%2FHouseholdSurvey%5B%40key%3D%22agtvcGVuZGF0YWtpdHIWCxIQSG91c2Vob2xkU3VydmV5MRgCDA%22%5D\n" +
"Completion-Status: Failure\n" +
"</pre>" +
"		<p>To resume the fetch, simply locate the `LastCursor-x:` field (there will be only one in the file), copy its value into the `LastCursor-x:` prompt above," +
"		 and do the same for the corresponding `LastKEY-x:` and `OdkId-x:` fields.</p>" +
"		<p>Resumed fetches must download data to a new download directory.  While the applet ensures that" +
"		 the top-level form entries are never duplicated and that processing picks up where it ended," +
"		  there may be duplicates in the repeated groups and binary data nested under that top-level form.</p>" +
"		<p> Resolving the duplicates is a manual process.  Binary data files have repeatably unique " +
"		names and can simply be consolidated into a single directory; any duplicates should be identical " +
"		copies of the same file.  The csv files should be manually concatenated then sorted by their " +
"		rightmost column (the KEY) column), and any duplicate rows removed.</p>" +
"		<h2>Known Issues</h2>" +
"		<h3>Csv file contents</h3>" +
"		<p>The csv files should be interpreted as containing UTF-8 characters. Unfortunately, browsing to the file" +
"		and double-clicking to open Excel will cause them to be loaded into a spreadsheet with the default ANSI" +
"		character encoding.  Instead, you need to:</p>" +
"		<ol><li>Open the Excel 2007 application</li>" +
"		<li>create a new workbook or open an existing workbook</li>" +
"		<li>choose Data/Import Text</li>" +
"		<li>Browse to the downloaded .csv file; hit Open</li>" +
"		<li><b>Text Import Wizard - Step 1 of 3</b> opens.</li>" +
"		<li>Choose `Delimited`</li>" +
"		<li>Under `File origin:` select <code>65001 : Unicode (UTF-8)</code></li>" +
"		<li>Hit Next to advance to <b>Text Import Wizard - Step 2 of 3</b></li>" +
"		<li>Choose only `Comma` for the delimiters.</li>" +
"		<li>Uncheck the `Treat consecutive delimiters as one` checkbox</li>" +
"		<li>Hit Finish or, choose Next to define the formatting for specific columns.</li></ol>" +
"		<p>The data will now be loaded into Excel 2007 with UTF-8 characters properly interpreted.</p>" +
"		<h3><a name=\"Cert\">Import the signing certificate</a></h3>" +
"		<p>This applet is signed with a self-signed certificate created by the OpenDataKit team.  If you are" +
"		accessing this page frequently, you may want to import the certificate into your browser's certificate" +
"		store to suppress the security warnings.  Note that whenever you import a certificate, you are trusting" +
"		the owner of that certificate with your system security, as it will allow any software that the owner " +
"		signs to be transparently executed on your system. Click <a href=\"/briefcase/OpenDataKit.cer\">here</a> to " +
"		download the certificate.  Then import it to suppress the security warnings; on Windows systems, " +
"		the most restricted way to import the certificate would be through <code>Control Panel/Java</code>. Go to" +
"		the <code>Security</code> tab, click <code>Certificates...</code> then <code>Import</code> to import " +
"		the certificate.</p>" );
		finishBasicHtmlResponse(resp);
		resp.setStatus(200);
	}
}
