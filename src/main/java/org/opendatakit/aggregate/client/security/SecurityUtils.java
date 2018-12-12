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
  /**
   * Executes a secure request (with CSRF protection) to make a RPC.
   * <p>
   * This overload is used when the RPC call doesn't return anything (Void type)
   *
   * @see #secureRequest(Object, SecureRpcRequest, Consumer, Consumer)
   */
  public static <T> void secureRequest(T rpc, SecureRpcRequest<T, Void> request, Runnable onSuccess, Consumer<Throwable> onFailure) {
    secureRequest(rpc, request, __ -> onSuccess.run(), onFailure);
  }

  /**
   * Executes a secure request (with CSRF protection) to make a RPC.
   *
   * @param rpc       RPC class that will be invoked
   * @param request   Request that makes the actual RPC call
   * @param onSuccess Callback to be called on success. It receives the request's output
   * @param onFailure Callback to be called on failure. It receives the {@link Throwable} cause of the failure
   * @param <T>       Type of the RPC class
   * @param <U>       Type of the output of the RPC call
   */
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

  @FunctionalInterface
  public interface SecureRpcRequest<T, U> {
    void apply(T rpc, String sessionCookie, AsyncCallback<U> callback);
  }
}
