package org.tikito.auth;

import org.tikito.exception.RequestNotAllowedException;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

@Slf4j
public class AuthUserResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(final MethodParameter methodParameter) {
        if (Optional.class.equals(methodParameter.getParameterType())) {
            return isValidClassToResolve(getClassFromMethodParameter(methodParameter));
        }
        return isValidClassToResolve(methodParameter.getParameterType());
    }

    @Override
    public Object resolveArgument(final MethodParameter methodParameter,
                                  final ModelAndViewContainer modelAndViewContainer,
                                  @Nullable final NativeWebRequest nativeWebRequest,
                                  final WebDataBinderFactory webDataBinderFactory) throws RequestNotAllowedException {
        final boolean optional = Optional.class.equals(methodParameter.getParameterType());
        final Class<AuthUser> requiredClass;
        final Optional<AuthUser> authentication = AuthUtil.getAuthentication();

        if (!optional && authentication.isEmpty()) {
            log.warn("Required authentication is not present in parameter for method {}", methodParameter.getMethod() != null ? methodParameter.getMethod().getName() : "unknown");
            throw new RequestNotAllowedException();
        } else if (optional && authentication.isEmpty()) {
            return Optional.empty();
        } else if (optional) {
            requiredClass = (Class<AuthUser>) getClassFromMethodParameter(methodParameter);
        } else {
            requiredClass = (Class<AuthUser>) methodParameter.getParameterType();
        }

        if (!requiredClass.isAssignableFrom(authentication.get().getClass())) {
            log.warn("Required level of user is not assignable: required {}, but authentication is of type {} for method {}", requiredClass.getName(), authentication.get().getClass().getName(), methodParameter.getMethod() != null ? methodParameter.getMethod().getName() : "unknown");
            throw new RequestNotAllowedException();
        }
        if (optional) {
            return authentication;
        }

        return authentication.get();
    }

    Class<?> getClassFromMethodParameter(final MethodParameter methodParameter) {
        final Type type = (((ParameterizedType) methodParameter.getGenericParameterType()).getActualTypeArguments()[0]);
        return (Class<?>) type;
    }

    static boolean isValidClassToResolve(final Class<?> c) {
        return AuthUser.class.isAssignableFrom(c);
    }
}