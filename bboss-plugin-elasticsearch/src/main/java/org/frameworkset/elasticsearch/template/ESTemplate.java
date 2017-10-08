/**
 *  Copyright 2008 biaoping.yin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.  
 */
package org.frameworkset.elasticsearch.template;

import bboss.org.apache.velocity.VelocityContext;
import bboss.org.apache.velocity.context.Context;
import bboss.org.apache.velocity.context.InternalContextAdapterImpl;
import bboss.org.apache.velocity.exception.*;
import bboss.org.apache.velocity.runtime.RuntimeConstants;
import bboss.org.apache.velocity.runtime.directive.Scope;
import bboss.org.apache.velocity.runtime.directive.StopCommand;
import bboss.org.apache.velocity.runtime.parser.ParseException;
import bboss.org.apache.velocity.runtime.parser.node.ASTText;
import bboss.org.apache.velocity.runtime.parser.node.Node;
import bboss.org.apache.velocity.runtime.parser.node.SimpleNode;
import bboss.org.apache.velocity.runtime.resource.Resource;
import bboss.org.apache.velocity.runtime.resource.ResourceManager;
import com.frameworkset.velocity.BBossVelocityUtil;
import org.frameworkset.soa.BBossStringReader;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * <p> SQLTemplate.java</p>
 * <p> Description: sql语句模板</p>
 * <p> bboss workgroup </p>
 * <p> Copyright (c) 2009 </p>
 * 
 * @Date 2012-12-4 下午7:31:55
 * @author biaoping.yin
 * @version 1.0
 */
public class ESTemplate extends Resource
{
    /*
     * The name of the variable to use when placing
     * the scope object into the context.
     */
    private String scopeName = "template";
    private ESInfo esInfo;
    
    private boolean provideScope = false;
   private String template;   
   private boolean processed;

    private VelocityException errorCondition = null;

    /** Default constructor */
    public ESTemplate(ESInfo esInfo)
    {
        super();
        this.esInfo = esInfo;   
         this.template = this.esInfo.getTemplate();
        this.setName(esInfo.getTemplateName());
        setType(ResourceManager.RESOURCE_ES);
    }
    /**
     * 根据解析出的语法结构确定sql语句是否是velocity模板
     * 如果不是则重置sqlinfo的istpl属性，相关的缓存就可以使用sqlname来作为key
     * 同时避免每次都对token进行拼接，提升系统性能
     */
    private void rechecksqlistpl()
    {
    	if(data != null)
    	{
    		Node[] childrens = ((SimpleNode)data).getChildren();
    		if(childrens != null && childrens.length > 0)
    		{
    			boolean switchcase = true;
    			for(Node node:childrens)
    			{
    				if(!(node instanceof ASTText))
    				{
    					switchcase = false;
    					break;
    				}
    			}
    			if(switchcase)
    			{
    				this.esInfo.setTpl(false);
    				 
    			}
    			else
    			{
    				this.esInfo.setTpl(true);
    			}
    		}
    		
    	}
    }
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
        throws ResourceNotFoundException, ParseErrorException
    {
    	if(processed)
    		return true;
    	synchronized(this)
    	{
    		if(processed)
        		return true;
    		
	        data = null;
	        
	        errorCondition = null;
	        Reader is = null;
	
	        /*
	         *  first, try to get the stream from the loader
	         */
	        try
	        {
	        	is = new BBossStringReader(template);
	        }
	        catch( ResourceNotFoundException rnfe )
	        {
	            /*
	             *  remember and re-throw
	             */
	
	            errorCondition = rnfe;
	            processed = true;
	            throw rnfe;
	        }
	
	        /*
	         *  if that worked, lets protect in case a loader impl
	         *  forgets to throw a proper exception
	         */
	
	        if (is != null)
	        {
	            /*
	             *  now parse the template
	             */
	
	            try
	            {
	                BufferedReader br = new BufferedReader( is);
	                data = rsvc.parseElasticTemplate(br, name);
	                initDocument();
	                processed = true;
	                try {
						rechecksqlistpl();
					} catch (Exception e) {
						
					}
	                return true;
	            }
	            
	            catch ( ParseException pex )
	            {
	                /*
	                 *  remember the error and convert
	                 */
	                errorCondition =  new ParseErrorException(pex, name);
	                processed = true;
	                throw errorCondition;
	            }
	            catch ( TemplateInitException pex )
	            {
	                errorCondition = new ParseErrorException( pex, name);
	                processed = true;
	                throw errorCondition;
	            }
	            /**
	             * pass through runtime exceptions
	             */
	            catch( RuntimeException e )
	            {
	                errorCondition = new VelocityException("Exception thrown processing Template "
	                    +getName(), e);
	                processed = true;
	                throw errorCondition;
	            }
	            finally
	            {
	            	processed = true;
	                /*
	                 *  Make sure to close the inputstream when we are done.
	                 */
	                try
	                {
	                    is.close();
	                }
	                catch(IOException e)
	                {
	                    // If we are already throwing an exception then we want the original
	                    // exception to be continued to be thrown, otherwise, throw a new Exception.
	                    if (errorCondition == null)
	                    {
	                         throw new VelocityException(e);
	                    }                    
	                }
	                
	            }
	        }
	        else
	        {
	            /*
	             *  is == null, therefore we have some kind of file issue
	             */
	            errorCondition = new ResourceNotFoundException("Unknown resource error for resource " + name );
	            processed = true;
	            throw errorCondition;
	        }
    	}
    }

    /**
     *  initializes the document.  init() is not longer
     *  dependant upon context, but we need to let the
     *  init() carry the template name down throught for VM
     *  namespace features
     * @throws TemplateInitException When a problem occurs during the document initialization.
     */
    public void initDocument()
    throws TemplateInitException
    {
        /*
         *  send an empty InternalContextAdapter down into the AST to initialize it
         */

        InternalContextAdapterImpl ica = new InternalContextAdapterImpl(  new VelocityContext() );

        try
        {
            /*
             *  put the current template name on the stack
             */

            ica.pushCurrentTemplateName( name );
            ica.setCurrentResource( this );

            /*
             *  init the AST
             */

            ((SimpleNode)data).init( ica, rsvc);

            String property = scopeName+'.'+RuntimeConstants.PROVIDE_SCOPE_CONTROL;
            provideScope = rsvc.getBoolean(property, provideScope);
        }
        finally
        {
            /*
             *  in case something blows up...
             *  pull it off for completeness
             */

            ica.popCurrentTemplateName();
            ica.setCurrentResource( null );
        }

    }

    /**
     * The AST node structure is merged with the
     * context to produce the final output.
     *
     *  @param context Conext with data elements accessed by template
     *  @param writer output writer for rendered template
     *  @throws ResourceNotFoundException if template not found
     *          from any available source.
     *  @throws ParseErrorException if template cannot be parsed due
     *          to syntax (or other) error.
     *  @throws MethodInvocationException When a method on a referenced object in the context could not invoked.
     */
    public void merge( Context context, Writer writer)
        throws ResourceNotFoundException, ParseErrorException, MethodInvocationException
    {
        merge(context, writer, null);
    }
    
    public String evaluate( Map variablevalues)
            throws ResourceNotFoundException, ParseErrorException, MethodInvocationException
    {
    	StringWriter writer = new StringWriter();    	
        merge(BBossVelocityUtil.buildVelocityContext(variablevalues), writer, null);
        return writer.toString();
    }
    
    

    
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
    public void merge( Context context, Writer writer, List macroLibraries)
        throws ResourceNotFoundException, ParseErrorException, MethodInvocationException
    {
    	process();
        /*
         *  we shouldn't have to do this, as if there is an error condition,
         *  the application code should never get a reference to the
         *  Template
         */

        if (errorCondition != null)
        {
            throw errorCondition;
        }

        if( data != null)
        {
            /*
             *  create an InternalContextAdapter to carry the user Context down
             *  into the rendering engine.  Set the template name and render()
             */

            InternalContextAdapterImpl ica = new InternalContextAdapterImpl( context );

            /**
             * Set the macro libraries
             */
            ica.setMacroLibraries(macroLibraries);

            if (macroLibraries != null)
            {
                for (int i = 0; i < macroLibraries.size(); i++)
                {
                    /**
                     * Build the macro library
                     */
                    try
                    {
                        rsvc.getTemplate((String) macroLibraries.get(i));
                    }
                    catch (ResourceNotFoundException re)
                    {
                        /*
                        * the macro lib wasn't found.  Note it and throw
                        */
                        rsvc.getLog().error("template.merge(): " +
                                "cannot find template " +
                                (String) macroLibraries.get(i));
                        throw re;
                    }
                    catch (ParseErrorException pe)
                    {
                        /*
                        * the macro lib was found, but didn't parse - syntax error
                        *  note it and throw
                        */
                        rsvc.getLog().error("template.merge(): " +
                                "syntax error in template " +
                                (String) macroLibraries.get(i) + ".");
                        throw pe;
                    }
                    
                    catch (Exception e)
                    {
                        throw new RuntimeException("Template.merge(): parse failed in template  " +
                                (String) macroLibraries.get(i) + ".", e);
                    }
                }
            }

            if (provideScope)
            {
                ica.put(scopeName, new Scope(this, ica.get(scopeName)));
            }
            try
            {
                ica.pushCurrentTemplateName( name );
                ica.setCurrentResource( this );

                ( (SimpleNode) data ).render( ica, writer);
            }
            catch (StopCommand stop)
            {
                if (!stop.isFor(this))
                {
                    throw stop;
                }
                else if (rsvc.getLog().isDebugEnabled())
                {
                    rsvc.getLog().debug(stop.getMessage());
                }
            }
            catch (IOException e)
            {
                throw new VelocityException("IO Error rendering template '"+ name + "'", e);
            }
            finally
            {
                /*
                 *  lets make sure that we always clean up the context
                 */
                ica.popCurrentTemplateName();
                ica.setCurrentResource( null );

                if (provideScope)
                {
                    Object obj = ica.get(scopeName);
                    if (obj instanceof Scope)
                    {
                        Scope scope = (Scope)obj;
                        if (scope.getParent() != null)
                        {
                            ica.put(scopeName, scope.getParent());
                        }
                        else if (scope.getReplaced() != null)
                        {
                            ica.put(scopeName, scope.getReplaced());
                        }
                        else
                        {
                            ica.remove(scopeName);
                        }
                    }
                }
            }
        }
        else
        {
            /*
             * this shouldn't happen either, but just in case.
             */

            String msg = "Template.merge() failure. The document is null, " +
                "most likely due to parsing error.";

            throw new RuntimeException(msg);

        }
    }

	
}
