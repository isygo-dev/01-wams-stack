package eu.isygoit.com.rest.interceptor;

import org.springframework.lang.Nullable;
import org.springframework.web.servlet.ModelAndView;


/**
 * The interface Rest handler interceptor.
 */
public interface RestHandlerInterceptor {

    /**
     * Pre rest handle boolean.
     *
     * @param handler the handler
     * @return the boolean
     * @throws Exception the exception
     */
    boolean preRestHandle(Object handler) throws Exception;

    /**
     * Post rest handle.
     *
     * @param handler      the handler
     * @param modelAndView the model and view
     * @throws Exception the exception
     */
    void postRestHandle(Object handler,
                        @Nullable ModelAndView modelAndView) throws Exception;

    /**
     * After rest completion.
     *
     * @param handler the handler
     * @param ex      the ex
     */
    void afterRestCompletion(Object handler,
                             @Nullable Exception ex);
}
