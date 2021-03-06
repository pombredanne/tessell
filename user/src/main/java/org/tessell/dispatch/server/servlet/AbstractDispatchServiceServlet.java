package org.tessell.dispatch.server.servlet;

import javax.servlet.GenericServlet;

import org.tessell.dispatch.server.ActionDispatch;
import org.tessell.dispatch.server.ExecutionContext;
import org.tessell.dispatch.server.SessionIdValidator;
import org.tessell.dispatch.shared.Action;
import org.tessell.dispatch.shared.ActionException;
import org.tessell.dispatch.shared.DispatchService;
import org.tessell.dispatch.shared.Result;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Provides a basic {@link DispatchService} implementation that defers to subclasses
 * for the {@link SessionIdValidator} and {@link ActionDispatch} instances.
 */
public abstract class AbstractDispatchServiceServlet extends RemoteServiceServlet implements DispatchService {

  private static final long serialVersionUID = 1L;

  @Override
  public Result execute(final String sessionId, final Action<?> action) throws ActionException {
    ActionDispatch d = getActionDispatch();
    if (d == null) {
      throw new IllegalStateException("Null ActionDispatch, ensure the server started correctly");
    }
    try {
      final ExecutionContext context = new ExecutionContext(getThreadLocalRequest(), getThreadLocalResponse());
      if (getSessionValidator() != null && !d.skipCSRFCheck(action)) {
        String secureSessionId = getSessionValidator().get(context);
        if (secureSessionId == null || !secureSessionId.equals(sessionId)) {
          throw invalidSession(context);
        }
      }
      return d.execute(action, context);
    } catch (final ActionException ae) {
      // assume the user has already logged the ActionException appropriately
      throw ae;
    } catch (final Exception e) {
      logActionFailure(e);
      throw wrapInActionException(e);
    }
  }

  /** Allows subclasses to override exception logging. By default uses {@link GenericServlet#log}. */
  protected void logActionFailure(Exception e) {
    log(e.getMessage(), e);
  }

  /** Allows subclasses to create their own "runtime exception" subclass of {@link ActionException}. */
  protected ActionException wrapInActionException(Exception e) {
    return new ActionException("A server error occured."); // don't leak the raw exception message
  }

  /** Allows subclasses to create their own invalid session subclasses of {@link ActionException}. */
  protected Exception invalidSession(ExecutionContext context) {
    return new IllegalStateException("Invalid session");
  }

  /** Method for subclasses to provide an optional {@link SessionIdValidator} for CSRF protection (or {@code null} to skip CSRF checking). */
  protected abstract SessionIdValidator getSessionValidator();

  /** Method for subclasses to return their {@link ActionDispatch} class. */
  protected abstract ActionDispatch getActionDispatch();

}
