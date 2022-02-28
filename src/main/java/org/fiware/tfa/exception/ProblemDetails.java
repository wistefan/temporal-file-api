package org.fiware.tfa.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Problem details as defined by <a href="https://tools.ietf.org/html/rfc7807">RFC-7807</a>" and mandated by the
 * <a href="https://docbox.etsi.org/isg/cim/open/Latest%20release%20NGSI-LD%20API%20for%20public%20comment.pdf">NGSI-LD Spec</a> 5.5.2 and 5.5.3
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProblemDetails {

	/**
	 * Type of the error
	 */
	private String type;
	/**
	 * Title of the error
	 */
	private String title;
	/**
	 * (HTTP) Status code associated with the error
	 */
	private int status;
	/**
	 * (preferably human readable) error details
	 */
	private String detail;
	/**
	 * Id of an instance associated with the problem, null if there is no such instance
	 */
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String instance;

}
