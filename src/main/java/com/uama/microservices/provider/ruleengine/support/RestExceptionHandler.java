package com.uama.microservices.provider.ruleengine.support;

import com.uama.framework.common.exception.BizErrorMessage;
import com.uama.framework.common.exception.BizException;
import com.uama.framework.common.exception.BizSystemErrorMessage;
import com.uama.framework.common.http.BaseExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Set;

@RestControllerAdvice
public class RestExceptionHandler extends BaseExceptionHandler {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String ERROR_MESSAGE_SEPARATE = ",";
	private static final int ERROR_MESSAGE_DEFAULT_CODE = 4001;

	@ExceptionHandler
	public ResponseEntity<BizErrorMessage> exception(HttpServletRequest req, Exception e) {
		BizErrorMessage errorMessage = new BizErrorMessage();
		if (e instanceof BizException) {
			BizException biz = (BizException) e;
			errorMessage.setMessage(biz.getMsg());
			errorMessage.setCode(biz.getCode());
			return ResponseEntity.badRequest().body(errorMessage);
		} else if (e instanceof MethodArgumentNotValidException) {
			MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
			final StringBuilder errors = new StringBuilder();
			for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
				errors.append(error.getField() + error.getDefaultMessage() + ERROR_MESSAGE_SEPARATE);
			}
			for (final ObjectError error : ex.getBindingResult().getGlobalErrors()) {
				errors.append(error.getObjectName() + error.getDefaultMessage() + ERROR_MESSAGE_SEPARATE);
			}
			errorMessage.setCode(ERROR_MESSAGE_DEFAULT_CODE);
			errorMessage.setMessage(errors.toString());
			return ResponseEntity.badRequest().body(errorMessage);
		} else if (e instanceof ConstraintViolationException) {
			ConstraintViolationException ex = (ConstraintViolationException) e;
			Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
			final StringBuilder errors = new StringBuilder();
			for (ConstraintViolation<?> violation : violations) {
				errors.append(violation.getMessage() + ERROR_MESSAGE_SEPARATE);
			}
			errorMessage.setCode(ERROR_MESSAGE_DEFAULT_CODE);
			errorMessage.setMessage(errors.toString());
			return ResponseEntity.badRequest().body(errorMessage);
		} else if (e instanceof BindException) {
			BindException ex = (BindException) e;
			final StringBuilder errors = new StringBuilder();
			for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
				errors.append(error.getField() + error.getDefaultMessage() + ERROR_MESSAGE_SEPARATE);
			}
			for (final ObjectError error : ex.getBindingResult().getGlobalErrors()) {
				errors.append(error.getObjectName() + error.getDefaultMessage() + ERROR_MESSAGE_SEPARATE);
			}
			errorMessage.setCode(ERROR_MESSAGE_DEFAULT_CODE);
			errorMessage.setMessage(errors.toString());
			return ResponseEntity.badRequest().body(errorMessage);
		} else {
			logger.error(e.getMessage(), e);
			HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
			errorMessage.setErrorMessage(BizSystemErrorMessage.INTERNAL_SERVER_ERROR);
			return new ResponseEntity<>(errorMessage, status);
		}
	}
}