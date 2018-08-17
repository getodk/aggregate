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
  public interface SecureRpcRequest<T, U> {
    void apply(T rpc, String sessionCookie, AsyncCallback<U> callback);
  }

  public static <T> void secureRequest(T rpc, SecureRpcRequest<T, Void> request, Runnable onSuccess, Consumer<Throwable> onFailure) {
    secureRequest(rpc, request, __ -> onSuccess.run(), onFailure);
  }

  public static <T, U> void secureRequest(T rpc, SecureRpcRequest<T, U> request, Consumer<U> onSuccess, Consumer<Throwable> onFailure) {
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
        request.apply(rpc, Cookies.getCookie("JSESSIONID"), new AsyncCallback<U>() {
          @Override
          public void onFailure(Throwable caught) {
            onFailure.accept(caught);
          }

          @Override
          public void onSuccess(U result) {
            onSuccess.accept(result);
          }
        });
      }
    });
  }
}
