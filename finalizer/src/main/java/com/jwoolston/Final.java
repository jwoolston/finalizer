package com.jwoolston;

import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Mark a code element to default all child elements to final
 *
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
@Retention(CLASS)
public @interface Final {

}
