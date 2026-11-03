package com.example.claim_submission_workflow.claim.api;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

	@ExceptionHandler(ClaimNotFoundException.class)
	public ProblemDetail handleClaimNotFound(ClaimNotFoundException exception, HttpServletRequest request) {
		return problem(HttpStatus.NOT_FOUND, "Claim not found", exception.getMessage(), request);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleValidation(
			MethodArgumentNotValidException exception,
			HttpServletRequest request) {
		ProblemDetail problem = problem(
				HttpStatus.BAD_REQUEST,
				"Validation failed",
				"One or more request fields are invalid.",
				request);
		Map<String, String> errors = new LinkedHashMap<>();
		exception.getBindingResult().getFieldErrors().forEach(error ->
				errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
		problem.setProperty("errors", errors);
		return problem;
	}

	@ExceptionHandler({
			HttpMessageNotReadableException.class,
			MethodArgumentTypeMismatchException.class,
			MissingServletRequestParameterException.class
	})
	public ProblemDetail handleBadRequest(Exception exception, HttpServletRequest request) {
		return problem(
				HttpStatus.BAD_REQUEST,
				"Invalid request",
				"The request body or one of its parameters is invalid.",
				request);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ProblemDetail handleDataConflict(
			DataIntegrityViolationException exception,
			HttpServletRequest request) {
		log.warn("Claim request conflicted with persisted data at {}", request.getRequestURI());
		return problem(
				HttpStatus.CONFLICT,
				"Claim conflict",
				"The claim could not be saved because it conflicts with existing data.",
				request);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ProblemDetail handleMethodNotAllowed(
			HttpRequestMethodNotSupportedException exception,
			HttpServletRequest request) {
		return problem(
				HttpStatus.METHOD_NOT_ALLOWED,
				"Method not allowed",
				"The HTTP method is not supported for this endpoint.",
				request);
	}

	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ProblemDetail handleUnsupportedMediaType(
			HttpMediaTypeNotSupportedException exception,
			HttpServletRequest request) {
		return problem(
				HttpStatus.UNSUPPORTED_MEDIA_TYPE,
				"Unsupported media type",
				"The request content type is not supported.",
				request);
	}

	@ExceptionHandler(Exception.class)
	public ProblemDetail handleUnexpected(Exception exception, HttpServletRequest request) {
		log.error("Unexpected error while handling {}", request.getRequestURI(), exception);
		return problem(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"Internal server error",
				"The request could not be completed due to an unexpected error.",
				request);
	}

	private static ProblemDetail problem(
			HttpStatus status,
			String title,
			String detail,
			HttpServletRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setTitle(title);
		problem.setInstance(java.net.URI.create(request.getRequestURI()));
		problem.setProperty("timestamp", Instant.now());
		return problem;
	}
}
