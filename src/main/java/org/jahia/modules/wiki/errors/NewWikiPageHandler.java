/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
package org.jahia.modules.wiki.errors;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.errors.ErrorHandler;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.query.QueryResultWrapper;
import org.jahia.services.query.QueryWrapper;
import org.jahia.services.render.URLResolver;
import org.jahia.services.render.URLResolverFactory;
import org.slf4j.Logger;

import javax.jcr.PathNotFoundException;
import javax.jcr.query.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * Wiki page creation handler.
 * User: toto
 * Date: Dec 2, 2009
 * Time: 4:11:46 PM
 */
public class NewWikiPageHandler implements ErrorHandler {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(NewWikiPageHandler.class);

    private URLResolverFactory urlResolverFactory;

    public void setUrlResolverFactory(URLResolverFactory urlResolverFactory) {
        this.urlResolverFactory = urlResolverFactory;
    }

    public boolean handle(Throwable e, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        try {
            if (!(e instanceof PathNotFoundException)) {
                return false;
            }
            URLResolver urlResolver = urlResolverFactory.createURLResolver(request.getPathInfo(), request.getServerName(), request);
            JCRNodeWrapper pageNode;
            String parentPath = StringUtils.substringBeforeLast(urlResolver.getPath(), "/");
            String newName = StringUtils.substringAfterLast(urlResolver.getPath(), "/");
            newName = StringUtils.substringBefore(newName, ".html");
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(
                    urlResolver.getWorkspace(), urlResolver.getLocale());
            try {
                JCRNodeWrapper parent = session.getNode(parentPath);
                if (parent.isNodeType("jnt:page")) {
                    pageNode = parent;
                } else {
                    pageNode = JCRContentUtils.getParentOfType(parent, "jnt:page");
                }
                // test if pageNode is wiki
                boolean isWiki = false;
                if (pageNode != null && pageNode.hasProperty("j:templateName")) {
                    String query = "select * from [jnt:pageTemplate] where [j:nodename]='" + pageNode.getPropertyAsString("j:templateName") + "'";
                    QueryWrapper q = session.getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2);
                    QueryResultWrapper result = q.execute();
                    if(result.getNodes().hasNext() && JCRContentUtils.getDescendantNodes((JCRNodeWrapper) result.getNodes().next(), "jnt:wikiPageFormCreation").hasNext()) {
                        isWiki = true;
                    }

                }
                if (pageNode == null || !isWiki) {
                    return false;
                }
                try {
                    JCRNodeWrapper node = pageNode.getNode(newName);
                    String link = request.getContextPath() + request.getServletPath() + "/" + StringUtils.substringBefore(
                            request.getPathInfo().substring(1),
                            "/") + "/" + urlResolver.getWorkspace() + "/" + urlResolver.getLocale() + node.getPath();

                    link += ".html";
                    response.sendRedirect(link);
                } catch (PathNotFoundException e1) {
                    if (null != pageNode) {
                        String link = request.getContextPath() + request.getServletPath() + "/" + StringUtils.substringBefore(
                                request.getPathInfo().substring(1),
                                "/") + "/" + urlResolver.getWorkspace() + "/" + urlResolver.getLocale() + pageNode.getPath();
                        String wikiTitle = request.getParameter("wikiTitle") != null ? request.getParameter("wikiTitle") : URLEncoder.encode(newName, "UTF-8");
                        link += ".html?displayTab=create-new-page&newPageName=" + URLEncoder.encode(newName, "UTF-8")+ "&wikiTitle=" + URLEncoder.encode(wikiTitle,"UTF-8");
                        response.sendRedirect(link);
                        return true;
                    }
                    logger.debug("Wiki page not found ask for creation",e1);
                }
            } catch (PathNotFoundException e1) {
                return false;
            }
        } catch (Exception e1) {
            logger.error(e1.getMessage(), e1);
        }
        return false;
    }
}
