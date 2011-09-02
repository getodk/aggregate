package org.opendatakit.common.persistence;

import java.util.List;

public class QueryResult {

	private final QueryResumePoint startCursor;
	private final QueryResumePoint resumeCursor;
	
	private final List<? extends CommonFieldsBase> resultList;
	
	public QueryResult(QueryResumePoint startCursor, List<? extends CommonFieldsBase> resultList, QueryResumePoint resumeCursor) {
		this.startCursor = startCursor;
		this.resultList = resultList;
		this.resumeCursor = resumeCursor;
	}

	public QueryResumePoint getResumeCursor() {
		return resumeCursor;
	}

	public List<? extends CommonFieldsBase> getResultList() {
		return resultList;
	}

	public QueryResumePoint getStartCursor() {
		return startCursor;
	}
	
}
