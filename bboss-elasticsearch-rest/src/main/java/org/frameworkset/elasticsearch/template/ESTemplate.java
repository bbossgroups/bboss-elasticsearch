package org.frameworkset.elasticsearch.template;
/**
 * Copyright 2024 bboss
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import bboss.org.apache.velocity.context.Context;
import bboss.org.apache.velocity.exception.MethodInvocationException;
import bboss.org.apache.velocity.exception.ParseErrorException;
import bboss.org.apache.velocity.exception.ResourceNotFoundException;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * <p>Description: </p>
 * <p></p>
 *
 * @author biaoping.yin
 * @Date 2024/12/23
 */
public interface ESTemplate {
    /**
     *  gets the named resource as a stream, parses and inits
     *
     * @return true if successful
     * @throws ResourceNotFoundException if template not found
     *          from any available source.
     * @throws ParseErrorException if template cannot be parsed due
     *          to syntax (or other) error.
     * @throws IOException problem reading input stream
     */
    public boolean process()
            throws ResourceNotFoundException, ParseErrorException;

    /**
     * The AST node structure is merged with the
     * context to produce the final output.
     *
     *  @param context Conext with data elements accessed by template
     *  @param writer output writer for rendered template
     *  @param macroLibraries a list of template files containing macros to be used when merging
     *  @throws ResourceNotFoundException if template not found
     *          from any available source.
     *  @throws ParseErrorException if template cannot be parsed due
     *          to syntax (or other) error.
     *  @throws MethodInvocationException When a method on a referenced object in the context could not invoked.
     *  @since 1.6
     */
    public void merge(Context context, Writer writer, List<String> macroLibraries)
            throws ResourceNotFoundException, ParseErrorException, MethodInvocationException;

    /**
     * The AST node structure is merged with the
     * context to produce the final output.
     *
     *  @param context Context with data elements accessed by template
     *  @param writer output writer for rendered template
     *  @throws ResourceNotFoundException if template not found
     *          from any available source.
     *  @throws ParseErrorException if template cannot be parsed due
     *          to syntax (or other) error.
     *  @throws MethodInvocationException When a method on a referenced object in the context could not invoked.
     */
    public void merge(Context context, Writer writer)
            throws ResourceNotFoundException, ParseErrorException, MethodInvocationException;
}
