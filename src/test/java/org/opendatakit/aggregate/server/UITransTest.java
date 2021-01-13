package org.opendatakit.aggregate.server;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.opendatakit.aggregate.constants.common.FilterOperation;
import org.opendatakit.aggregate.constants.common.Visibility;
import org.opendatakit.common.persistence.Query;

import static org.junit.Assert.assertThat;

public class UITransTest {

    @Test
    public void should_invert_operation_for_hide_visibility(){
        assertThat(UITrans.convertFilterOperation(FilterOperation.EQUAL, Visibility.HIDE), Matchers.is(Query.FilterOperation.NOT_EQUAL));
        assertThat(UITrans.convertFilterOperation(FilterOperation.NOT_EQUAL, Visibility.HIDE), Matchers.is(Query.FilterOperation.EQUAL));
        assertThat(UITrans.convertFilterOperation(FilterOperation.GREATER_THAN, Visibility.HIDE), Matchers.is(Query.FilterOperation.LESS_THAN_OR_EQUAL));
        assertThat(UITrans.convertFilterOperation(FilterOperation.GREATER_THAN_OR_EQUAL, Visibility.HIDE), Matchers.is(Query.FilterOperation.LESS_THAN));
        assertThat(UITrans.convertFilterOperation(FilterOperation.LESS_THAN, Visibility.HIDE), Matchers.is(Query.FilterOperation.GREATER_THAN_OR_EQUAL));
        assertThat(UITrans.convertFilterOperation(FilterOperation.LESS_THAN_OR_EQUAL, Visibility.HIDE), Matchers.is(Query.FilterOperation.GREATER_THAN));
    }

    @Test
    public void should_convert_UI_operation_to_query_operation_for_display_visibility(){
        assertThat(UITrans.convertFilterOperation(FilterOperation.EQUAL, Visibility.DISPLAY), Matchers.is(Query.FilterOperation.EQUAL));
        assertThat(UITrans.convertFilterOperation(FilterOperation.NOT_EQUAL, Visibility.DISPLAY), Matchers.is(Query.FilterOperation.NOT_EQUAL));
        assertThat(UITrans.convertFilterOperation(FilterOperation.GREATER_THAN, Visibility.DISPLAY), Matchers.is(Query.FilterOperation.GREATER_THAN));
        assertThat(UITrans.convertFilterOperation(FilterOperation.GREATER_THAN_OR_EQUAL, Visibility.DISPLAY), Matchers.is(Query.FilterOperation.GREATER_THAN_OR_EQUAL));
        assertThat(UITrans.convertFilterOperation(FilterOperation.LESS_THAN, Visibility.DISPLAY), Matchers.is(Query.FilterOperation.LESS_THAN));
        assertThat(UITrans.convertFilterOperation(FilterOperation.LESS_THAN_OR_EQUAL, Visibility.DISPLAY), Matchers.is(Query.FilterOperation.LESS_THAN_OR_EQUAL));
    }
}
