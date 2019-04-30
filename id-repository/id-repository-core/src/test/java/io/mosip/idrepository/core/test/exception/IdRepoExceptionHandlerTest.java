package io.mosip.idrepository.core.test.exception;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.Errors;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.ServletWebRequest;

import io.mosip.idrepository.core.constant.IdRepoErrorConstants;
import io.mosip.idrepository.core.dto.IdResponseDTO;
import io.mosip.idrepository.core.exception.IdRepoAppException;
import io.mosip.idrepository.core.exception.IdRepoAppUncheckedException;
import io.mosip.idrepository.core.exception.IdRepoExceptionHandler;
import io.mosip.kernel.core.exception.ServiceError;

/**
 * The Class IdRepoExceptionHandlerTest.
 *
 * @author Manoj SP
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
@WebMvcTest
@ActiveProfiles("test")
@ConfigurationProperties("mosip.idrepo")
public class IdRepoExceptionHandlerTest {

	private static final String UIN = "UIN";

	private Map<String, String> id;

	@Autowired
	Environment env;

	/** The errors. */
	@Mock
	private Errors errors;

	@Mock
	ServletWebRequest request;

	/** The handler. */
	@InjectMocks
	private IdRepoExceptionHandler handler;

	public Map<String, String> getId() {
		return id;
	}

	public void setId(Map<String, String> id) {
		this.id = id;
	}

	/**
	 * Before.
	 */
	@Before
	public void before() {
		ReflectionTestUtils.setField(handler, "env", env);
		ReflectionTestUtils.setField(handler, "id", id);
	}

	/**
	 * Test handle all exception.
	 */
	@Test
	public void testHandleAllException() {
		when(request.getHttpMethod()).thenReturn(HttpMethod.GET);
		ResponseEntity<Object> handleAllExceptions = ReflectionTestUtils.invokeMethod(handler, "handleAllExceptions",
				new RuntimeException("Runtime Exception"), request);
		IdResponseDTO response = (IdResponseDTO) handleAllExceptions.getBody();
		List<ServiceError> errorCode = response.getErrors();
		errorCode.forEach(e -> {
			assertEquals(IdRepoErrorConstants.UNKNOWN_ERROR.getErrorCode(), e.getErrorCode());
			assertEquals(IdRepoErrorConstants.UNKNOWN_ERROR.getErrorMessage(), e.getMessage());
		});
	}

	/**
	 * Test handle exception internal.
	 */
	@Test
	public void testHandleExceptionInternal() {
		when(request.getHttpMethod()).thenReturn(HttpMethod.POST);
		ResponseEntity<Object> handleExceptionInternal = ReflectionTestUtils.invokeMethod(handler,
				"handleExceptionInternal",
				new HttpMediaTypeNotSupportedException("Http Media Type Not Supported Exception"), null, null,
				HttpStatus.EXPECTATION_FAILED, request);
		IdResponseDTO response = (IdResponseDTO) handleExceptionInternal.getBody();
		List<ServiceError> errorCode = response.getErrors();
		errorCode.forEach(e -> {
			assertEquals(IdRepoErrorConstants.INVALID_REQUEST.getErrorCode(), e.getErrorCode());
			assertEquals(IdRepoErrorConstants.INVALID_REQUEST.getErrorMessage(), e.getMessage());
		});
	}

	/**
	 * Test handle id app exception.
	 */
	@Test
	public void testHandleIdAppException() {
		when(request.getHttpMethod()).thenReturn(HttpMethod.PATCH);
		ResponseEntity<Object> handleIdAppException = ReflectionTestUtils.invokeMethod(handler, "handleIdAppException",
				new IdRepoAppException(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorCode(),
						String.format(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorMessage(), UIN)), request);
		IdResponseDTO response = (IdResponseDTO) handleIdAppException.getBody();
		List<ServiceError> errorCode = response.getErrors();
		errorCode.forEach(e -> {
			assertEquals(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorCode(), e.getErrorCode());
			assertEquals(String.format(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorMessage(), UIN), e.getMessage());
		});
	}

	/**
	 * Test handle id app exception with cause.
	 */
	@Test
	public void testHandleIdAppExceptionWithCause() {
		when(request.getHttpMethod()).thenReturn(HttpMethod.GET);
		IdRepoAppException ex = new IdRepoAppException(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorCode(),
				String.format(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorMessage(), UIN));
		ResponseEntity<Object> handleIdAppException = ReflectionTestUtils.invokeMethod(handler, "handleIdAppException",
				ex, request);
		IdResponseDTO response = (IdResponseDTO) handleIdAppException.getBody();
		List<ServiceError> errorCode = response.getErrors();
		errorCode.forEach(e -> {
			assertEquals(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorCode(), e.getErrorCode());
			assertEquals(String.format(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorMessage(), UIN), e.getMessage());
		});
	}

	@Test
	public void testHandleIdAppExceptionWithUncheckedCause() {
		when(request.getHttpMethod()).thenReturn(HttpMethod.POST);
		IdRepoAppUncheckedException ex = new IdRepoAppUncheckedException(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorCode(),
				String.format(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorMessage(), UIN));
		ResponseEntity<Object> handleIdAppException = ReflectionTestUtils.invokeMethod(handler,
				"handleIdAppUncheckedException", ex, request);
		IdResponseDTO response = (IdResponseDTO) handleIdAppException.getBody();
		List<ServiceError> errorCode = response.getErrors();
		errorCode.forEach(e -> {
			assertEquals(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorCode(), e.getErrorCode());
			assertEquals(String.format(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorMessage(), UIN), e.getMessage());
		});
	}

	@Test
	public void testHandleIdAppUncheckedException() {
		when(request.getHttpMethod()).thenReturn(HttpMethod.PATCH);
		IdRepoAppUncheckedException ex = new IdRepoAppUncheckedException(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorCode(),
				String.format(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorMessage(), UIN));
		ResponseEntity<Object> handleIdAppUncheckedException = ReflectionTestUtils.invokeMethod(handler,
				"handleIdAppUncheckedException", ex, request);
		IdResponseDTO response = (IdResponseDTO) handleIdAppUncheckedException.getBody();
		List<ServiceError> errorCode = response.getErrors();
		errorCode.forEach(e -> {
			assertEquals(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorCode(), e.getErrorCode());
			assertEquals(String.format(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorMessage(), UIN), e.getMessage());
		});
	}

	/**
	 * Test handle exception internal with object.
	 */
	@Test
	public void testHandleExceptionInternalWithObject() {
		when(request.getHttpMethod()).thenReturn(HttpMethod.GET);
		ResponseEntity<Object> handleExceptionInternal = ReflectionTestUtils.invokeMethod(handler,
				"handleExceptionInternal",
				new HttpMediaTypeNotSupportedException("Http Media Type Not Supported Exception"), null, null, null,
				request);
		IdResponseDTO response = (IdResponseDTO) handleExceptionInternal.getBody();
		response.getErrors();
	}

	@Test
	public void testHandleExceptionInternalWithOtherException() {
		when(request.getHttpMethod()).thenReturn(HttpMethod.POST);
		ResponseEntity<Object> handleExceptionInternal = ReflectionTestUtils.invokeMethod(handler,
				"handleExceptionInternal", new IdRepoAppException(), null, null, null, request);
		IdResponseDTO response = (IdResponseDTO) handleExceptionInternal.getBody();
		response.getErrors();
	}
}
