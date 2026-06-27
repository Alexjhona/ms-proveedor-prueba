package com.example.ms_proveedor.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.ContentCachingRequestWrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CachedRequestBodyFilterTest {

    private final CachedRequestBodyFilter filter = new CachedRequestBodyFilter();

    @Test
    void wrapsPlainRequestBeforeContinuingChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(
                org.mockito.ArgumentMatchers.isA(ContentCachingRequestWrapper.class),
                same(response));
    }

    @Test
    void reusesRequestThatIsAlreadyWrapped() throws Exception {
        ContentCachingRequestWrapper wrapper =
                new ContentCachingRequestWrapper(new MockHttpServletRequest());
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(wrapper, response, chain);

        verify(chain).doFilter(same(wrapper), same(response));
        assertThat(wrapper.getContentAsByteArray()).isEmpty();
    }
}
