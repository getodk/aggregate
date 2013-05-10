package org.opendatakit.common.testing.gae;

import org.opendatakit.common.testing.ICommonTestSetup;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.apphosting.api.ApiProxy;
import com.google.apphosting.api.ApiProxy.Environment;

class GaeInitialize implements ICommonTestSetup {
  
    private final LocalServiceTestHelper helper =
        new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
          .setDefaultHighRepJobPolicyUnappliedJobPercentage(20).setStoreDelayMs(800));

    public Environment gaeEnvironment = null;

	public GaeInitialize() {
		helper.setUp();
		gaeEnvironment = ApiProxy.getCurrentEnvironment();
	}
	
	public void setup() {
		ApiProxy.setEnvironmentForCurrentThread(gaeEnvironment);
	}
}