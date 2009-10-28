package org.odk.aggregate.form.remoteserver;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.odk.aggregate.form.Form;
import org.odk.aggregate.submission.Submission;
import org.odk.aggregate.table.SubmissionFusionTable;

import com.google.appengine.api.datastore.Key;

@Entity
public class FusionTable implements RemoteServer {


	/**
	 * GAE datastore key that uniquely identifies the form element
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Key key;

	/**
	 * Table Name
	 */
	@Enumerated
	private String tableName;


	public FusionTable(String table) {
		tableName = table;
	}

	public String getTableName() {
		return tableName;
	}


	public void sendSubmissionToRemoteServer(Form xform, String serverName,
			EntityManager em, String appName, Submission submission) {

	    SubmissionFusionTable fusionsSubmission =
	        new SubmissionFusionTable(xform, serverName, em);

	    fusionsSubmission.insertNewDataInSpreadsheet(submission, this);
	}
	
	  /**
	   * @see java.lang.Object#equals(java.lang.Object)
	   */
	  @Override
	  public boolean equals(Object obj) {
	    if (!(obj instanceof FusionTable)) {
	      return false;
	    }
	    FusionTable other = (FusionTable) obj;
	    return (key == null ? (other.key == null) : (key.equals(other.key)))
	        && (tableName == null ? (other.tableName == null) : (tableName.equals(other.tableName)));
	  }

	  /**
	   * @see java.lang.Object#hashCode()
	   */
	  @Override
	  public int hashCode() {
	    int hashCode = 13;
	    if(key != null) hashCode += key.hashCode();
	    if(tableName != null) hashCode += tableName.hashCode();
	    return hashCode;
	  }
}
