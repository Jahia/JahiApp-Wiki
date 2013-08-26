package org.jahia.modules.wiki;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Jahia;
import org.jahia.services.content.*;
import org.jahia.services.content.interceptor.PropertyInterceptor;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.utils.WebUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.manager.ComponentRepositoryException;
import org.xwiki.rendering.block.ImageBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.listener.URLImage;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.syntax.SyntaxFactory;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jahia.api.Constants.JAHIAMIX_REFERENCES_IN_FIELD;
import static org.jahia.api.Constants.JAHIA_REFERENCE_IN_FIELD_PREFIX;

public class WikiURLInterceptor implements PropertyInterceptor , InitializingBean {
    private static final String DOC_CONTEXT_PLACEHOLDER = "##doc-context##/";

    private SyntaxFactory syntaxFactory;
    private String inputSyntax;

    private String dmsContext;

    private Pattern refPattern = Pattern.compile("/##ref:link([0-9]+)##(.*)");




    private static Logger logger = org.slf4j.LoggerFactory.getLogger(WikiURLInterceptor.class);

    public boolean canApplyOnProperty(JCRNodeWrapper jcrNodeWrapper, ExtendedPropertyDefinition extendedPropertyDefinition) throws RepositoryException {
        return extendedPropertyDefinition != null && extendedPropertyDefinition.getName().equals("wikiContent") && extendedPropertyDefinition.getDeclaringNodeType().getName().equals("jnt:wikiPage");
    }

    public void beforeRemove(JCRNodeWrapper node, String name, ExtendedPropertyDefinition definition) throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (node.isNodeType(JAHIAMIX_REFERENCES_IN_FIELD)) {
            NodeIterator ni = node.getNodes(JAHIA_REFERENCE_IN_FIELD_PREFIX);
            while (ni.hasNext()) {
                JCRNodeWrapper ref = (JCRNodeWrapper) ni.next();
                if (name.equals(ref.getProperty("j:fieldName").getString())) {
                    ref.remove();
                }
            }
        }
    }

    public Value beforeSetValue(JCRNodeWrapper node, String name, ExtendedPropertyDefinition definition, Value originalValue) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        String content = originalValue.getString();

        final Map<String, Long> refs = new HashMap<String, Long>();

        if (logger.isDebugEnabled()) {
            logger.debug("Intercept setValue for "+node.getPath()+"/"+name);
        }

        if (node.isNodeType(JAHIAMIX_REFERENCES_IN_FIELD)) {
            NodeIterator ni = node.getNodes(JAHIA_REFERENCE_IN_FIELD_PREFIX);
            while (ni.hasNext()) {
                JCRNodeWrapper ref = (JCRNodeWrapper) ni.next();
                if (name.equals(ref.getProperty("j:fieldName").getString()) && ref.hasProperty("j:reference")) {
                    refs.put(ref.getProperty("j:reference").getString(), Long.valueOf(StringUtils.substringAfterLast(ref.getName(), "_")));
                }
            }
        }

        Map<String, Long> newRefs = new HashMap<String, Long>();

        String result = content;

        try {
            ComponentManager componentManager = WikiRenderer.getComponentManager(syntaxFactory.getClass().getClassLoader());
            Parser parser = componentManager.lookup(Parser.class, inputSyntax);
            XDOM xdom = parser.parse(new StringReader(content));
            List<ImageBlock> l = xdom.getChildrenByType(ImageBlock.class, true);
            for (ImageBlock imageBlock : l) {
                final String url = imageBlock.getImage().getName();
                if (url.startsWith(Jahia.getContextPath() + "/files/")) {
                    String newUrl = replaceRefsByPlaceholders(url, newRefs, refs, node.getSession().getWorkspace().getName());
                    imageBlock.getParent().replaceChild(new ImageBlock(new URLImage(newUrl), imageBlock.isFreeStandingURI(), imageBlock.getParameters()), imageBlock);
                }
            }
//             new BlockRenderer();
            BlockRenderer br = (BlockRenderer) componentManager.lookup(BlockRenderer.class, inputSyntax);
            DefaultWikiPrinter p = new DefaultWikiPrinter();
            br.render(xdom.getRoot(), p);
            result = p.toString();
        } catch (ComponentRepositoryException e) {
            logger.error("Cannot parse wiki content",e);
        } catch (ComponentLookupException e) {
            logger.error("Cannot parse wiki content",e);
        } catch (ParseException e) {
            logger.error("Cannot parse wiki content",e);
        }

        if (!newRefs.equals(refs)) {
            if (!newRefs.isEmpty() && !node.isNodeType(JAHIAMIX_REFERENCES_IN_FIELD)) {
                node.addMixin(JAHIAMIX_REFERENCES_IN_FIELD);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("New references : "+newRefs);
            }
            NodeIterator ni = node.getNodes(JAHIA_REFERENCE_IN_FIELD_PREFIX);
            while (ni.hasNext()) {
                JCRNodeWrapper ref = (JCRNodeWrapper) ni.next();
                if (name.equals(ref.getProperty("j:fieldName").getString()) && !newRefs.containsKey(ref.getProperty("j:reference").getString())) {
                    ref.remove();
                }
            }

            for (Map.Entry<String,Long> entry : newRefs.entrySet()) {
                if (!refs.containsKey(entry.getKey())) {
                    JCRNodeWrapper ref = node.addNode("j:referenceInField_" + name + "_" + entry.getValue(), "jnt:referenceInField");
                    ref.setProperty("j:fieldName",name);
                    ref.setProperty("j:reference", entry.getKey());
                }
            }
        }

        if (!result.equals(content)) {
            return node.getSession().getValueFactory().createValue(result);
        }
        return originalValue;

    }

    public Value[] beforeSetValues(JCRNodeWrapper jcrNodeWrapper, String s, ExtendedPropertyDefinition extendedPropertyDefinition, Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return values;
    }

    public Value afterGetValue(JCRPropertyWrapper property, Value storedValue) throws ValueFormatException, RepositoryException {
        String content = storedValue.getString();
        if (content == null || !content.contains(DOC_CONTEXT_PLACEHOLDER)) {
            return storedValue;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Intercept getValue for "+property.getPath());
        }

        final Map<Long, String> refs = new HashMap<Long, String>();

        final ExtendedPropertyDefinition definition = (ExtendedPropertyDefinition) property.getDefinition();
        String name = definition.getName();
        JCRNodeWrapper parent = property.getParent();

        if (parent.isNodeType(JAHIAMIX_REFERENCES_IN_FIELD)) {
            NodeIterator ni = parent.getNodes(JAHIA_REFERENCE_IN_FIELD_PREFIX);
            while (ni.hasNext()) {
                JCRNodeWrapper ref = (JCRNodeWrapper) ni.next();
                if (name.equals(ref.getProperty("j:fieldName").getString()) && ref.hasProperty("j:reference")) {
                    refs.put(Long.valueOf(StringUtils.substringAfterLast(ref.getName(), "_")), ref.getProperty("j:reference").getString());
                }
            }
        }

        String result = content;

        try {
            ComponentManager componentManager = WikiRenderer.getComponentManager(syntaxFactory.getClass().getClassLoader());
            Parser parser = componentManager.lookup(Parser.class, inputSyntax);
            XDOM xdom = parser.parse(new StringReader(content));
            List<ImageBlock> l = xdom.getChildrenByType(ImageBlock.class, true);
            for (ImageBlock imageBlock : l) {
                final String url = imageBlock.getImage().getName();
                if (url.startsWith(DOC_CONTEXT_PLACEHOLDER)) {
                    try {
                        String newUrl = replacePlaceholdersByRefs(url, refs, property.getSession().getWorkspace().getName(), property.getSession().getLocale());
                        imageBlock.getParent().replaceChild(new ImageBlock(new URLImage(newUrl), imageBlock.isFreeStandingURI(), imageBlock.getParameters()), imageBlock);
                    } catch (RepositoryException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            BlockRenderer br = (BlockRenderer) componentManager.lookup(BlockRenderer.class, inputSyntax);
            DefaultWikiPrinter p = new DefaultWikiPrinter();
            br.render(xdom.getRoot(), p);
            result = p.toString();
        } catch (ComponentRepositoryException e) {
            logger.error("Cannot parse wiki content",e);
        } catch (ComponentLookupException e) {
            logger.error("Cannot parse wiki content",e);
        } catch (ParseException e) {
            logger.error("Cannot parse wiki content",e);
        }



        if (!result.equals(content)) {
            return property.getSession().getValueFactory().createValue(result);
        }
        return storedValue;
    }

    public Value[] afterGetValues(JCRPropertyWrapper jcrPropertyWrapper, Value[] values) throws ValueFormatException, RepositoryException {
        return values;
    }

    String replaceRefsByPlaceholders(final String originalValue, final Map<String, Long> newRefs, final Map<String, Long> oldRefs, String workspace) throws RepositoryException {

        if (logger.isDebugEnabled()) {
            logger.debug("Before replaceRefsByPlaceholders : "+originalValue);
        }

        String pathPart = originalValue;
        if (pathPart.startsWith(dmsContext)) {
            // Remove DOC context part
            pathPart = StringUtils.substringAfter(StringUtils.substringAfter(pathPart, dmsContext), "/");
        } else {
            return originalValue;
        }

        final String path = "/" + WebUtils.urlDecode(pathPart);

        return JCRTemplate.getInstance().doExecuteWithSystemSession(null, workspace, null, new JCRCallback<String>() {
            public String doInJCR(JCRSessionWrapper session) throws RepositoryException {
                String value = originalValue;
                String ext = null;
                String tpl = null;
                JCRNodeWrapper reference;
                try {
                    String currentPath = path;
                    // retrieve path
                    while (true) {
                        if (StringUtils.contains(currentPath,'/')) {
                            currentPath = StringUtils.substringAfter(currentPath,"/");
                        } else {
                            throw new PathNotFoundException("not found in "+path);
                        }
                        try {
                            reference = session.getNode(JCRContentUtils.escapeNodePath("/"+currentPath));
                            break;
                        } catch (PathNotFoundException e) {
                            // continue
                        }
                    }
                    value = DOC_CONTEXT_PLACEHOLDER + StringUtils.substringAfter(value, dmsContext);
                } catch (PathNotFoundException e) {
                    throw new ConstraintViolationException("Invalid link : " + path, e);
                }
                String id = reference.getIdentifier();
                if (!newRefs.containsKey(id)) {
                    if (oldRefs.containsKey(id)) {
                        newRefs.put(id, oldRefs.get(id));
                    } else {
                        Long max = Math.max(oldRefs.isEmpty() ? 0 : Collections.max(oldRefs.values()), newRefs.isEmpty() ? 0 : Collections.max(newRefs.values()));
                        newRefs.put(id, max + 1);
                    }
                }
                Long index = newRefs.get(id);
                String link = "/##ref:link" + index + "##";
                if (tpl != null) {
                    link += "." + tpl;
                }
                if (ext != null) {
                    link += "." + ext;
                }
                value = WebUtils.urlDecode(value).replace(path, link);
                if (logger.isDebugEnabled()) {
                    logger.debug("After replaceRefsByPlaceholders : "+value);
                }
                return value;
            }
        });
    }

    private String replacePlaceholdersByRefs(final String originalValue, final Map<Long, String> refs, final String workspaceName, Locale locale) throws RepositoryException {

        String pathPart = originalValue;
        if (logger.isDebugEnabled()) {
            logger.debug("Before replacePlaceholdersByRefs : "+originalValue);
        }

        if (pathPart.startsWith(DOC_CONTEXT_PLACEHOLDER)) {
            // Remove DOC context part
            pathPart = StringUtils.substringAfter(StringUtils.substringAfter(pathPart, DOC_CONTEXT_PLACEHOLDER), "/");
        } else {
            return originalValue;
        }

        final String path = "/" + pathPart;

        return JCRTemplate.getInstance().doExecuteWithSystemSession(null, workspaceName, locale, new JCRCallback<String>() {
            public String doInJCR(JCRSessionWrapper session) throws RepositoryException {
                String value = originalValue;
                try {
                    Matcher matcher = refPattern.matcher(path);
                    if (!matcher.matches()) {
                        logger.error("Cannot match value, should contain ##ref : " + path);
                        return originalValue;
                    }
                    String id = matcher.group(1);
                    String ext = matcher.group(2);
                    String uuid = refs.get(new Long(id));
                    String nodePath = null;
                    try {
                        nodePath = session.getNodeByUUID(uuid).getPath();
                    } catch (ItemNotFoundException infe) {
                        logger.warn("Cannot find referenced item : "+uuid);
                        return "#";
                    }
                    value = originalValue.replace(path, nodePath + ext);
                    StringBuilder builder = new StringBuilder(dmsContext);
                    builder.append(workspaceName).append(nodePath).append(ext);
                    value = builder.toString();
                    if (logger.isDebugEnabled()) {
                        logger.debug("After replacePlaceholdersByRefs : "+value);
                    }
                } catch (Exception e) {
                    logger.error("Exception when transforming placeholder for" + path,e);
                }
                return value;
            }
        });
    }




    public void setSyntaxFactory(SyntaxFactory syntaxFactory) {
        this.syntaxFactory = syntaxFactory;
    }

    public void setInputSyntax(String inputSyntax) {
        this.inputSyntax = inputSyntax;
    }

    public void afterPropertiesSet() throws Exception {
        dmsContext = Jahia.getContextPath() + "/files/";
    }


}
