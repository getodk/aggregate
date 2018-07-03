package org.opendatakit.aggregate.client.security;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.HasRpcToken;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.client.rpc.XsrfTokenService;
import com.google.gwt.user.client.rpc.XsrfTokenServiceAsync;
import java.util.function.Consumer;
import org.opendatakit.aggregate.client.AggregateUI;

public class SecurityUtils {
  @FunctionalInterface
  public interface SecureRpcRequest<T> {
    void apply(T rpc, String sessionCookie, AsyncCallback<Void> callback);
  }

  public static <T> void secureRequest(T rpc, SecureRpcRequest<T> request, Runnable onSuccess, Consumer<Throwable> onFailure) {
    XsrfTokenServiceAsync xsrf = GWT.create(XsrfTokenService.class);
    ((ServiceDefTarget) xsrf).setServiceEntryPoint(GWT.getModuleBaseURL() + "xsrf");
    xsrf.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
      @Override
      public void onFailure(Throwable caught) {
        AggregateUI.getUI().reportError("Incomplete security update: ", caught);
      }

      @Override
      public void onSuccess(XsrfToken result) {
        ((HasRpcToken) rpc).setRpcToken(result);
        request.apply(rpc, Cookies.getCookie("JSESSIONID"), new AsyncCallback<Void>() {
          @Override
          public void onFailure(Throwable caught) {
            onFailure.accept(caught);
          }

          @Override
          public void onSuccess(Void result) {
            onSuccess.run();
          }
        });
      }
    });
  }
}
