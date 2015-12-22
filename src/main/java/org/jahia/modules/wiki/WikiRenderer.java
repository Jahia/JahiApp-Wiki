/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.modules.wiki;

import java.io.StringReader;

import org.jahia.services.render.RenderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.manager.ComponentRepositoryException;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.renderer.xhtml.XHTMLLinkRenderer;
import org.xwiki.rendering.syntax.SyntaxFactory;
import org.xwiki.rendering.transformation.TransformationManager;

/**
 * @auther ktlili
 */
public class WikiRenderer {
    private static class Holder {
        static final EmbeddableComponentManager COMPONENT_MANAGER;
        
        static {
            COMPONENT_MANAGER = new EmbeddableComponentManager();
            COMPONENT_MANAGER.initialize(WikiRenderer.class.getClassLoader());
            // register use our linkRenderer as  default link renderer
            DefaultComponentDescriptor<XHTMLLinkRenderer> componentDescriptor = new DefaultComponentDescriptor<XHTMLLinkRenderer>();
            componentDescriptor.setRole(XHTMLLinkRenderer.class);
            componentDescriptor.setImplementation(CustomXHTMLLinkRenderer.class);
            componentDescriptor.setRoleHint("default");
            try {
                COMPONENT_MANAGER.registerComponent(componentDescriptor);
            } catch (ComponentRepositoryException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(WikiRenderer.class);

    
    /**
     * Get a singleton instance of the component manager.
     *
     * @return a singleton instance of the component manager
     */
    public static ComponentManager getComponentManager() {
        return Holder.COMPONENT_MANAGER;
    }

    /**
     * Get componentManager. If null, created a new one
     *
     * @param classLoader
     * @return
     */
    public static ComponentManager getComponentManager(ClassLoader classLoader) throws ComponentRepositoryException {
        return getComponentManager();

    }

    /**
     * Render wiki content as html
     *
     * @param html
     * @return
     * @throws Exception
     */
    public static String renderWikiSyntaxAsXHTML(RenderContext renderContext, String html, SyntaxFactory syntaxFactory, String inputSyntax, String outputSyntax) throws Exception {
        logger.debug("Wiki content before processing: " + html);
        // Initialize Rendering components and allow getting instances
        ComponentManager componentManager = getComponentManager();

        // update the renderContext
        CustomXHTMLLinkRenderer linkRenderer = (CustomXHTMLLinkRenderer) componentManager.lookup(XHTMLLinkRenderer.class);
        linkRenderer.setRenderContext(renderContext);

        // add .html if there is no extention
        Parser parser = componentManager.lookup(Parser.class, inputSyntax);
        XDOM xdom = parser.parse(new StringReader(html));


        // Execute transformations (for example this executes the Macros which are implemented as Transformations).
        TransformationManager txManager = componentManager.lookup(TransformationManager.class);
        txManager.performTransformations(xdom, parser.getSyntax());

        // Generate XWiki 2.0 Syntax as output for example
        WikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer renderer = componentManager.lookup(BlockRenderer.class, outputSyntax);
        renderer.render(xdom, printer);


        String result = printer.toString();
        logger.debug("Wiki content after processing:" + result);
        return printer.toString();


    }


}
