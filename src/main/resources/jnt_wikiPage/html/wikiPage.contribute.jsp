<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<template:addResources type="css" resources="wiki.css"/>
<template:addResources type="css" resources="wiki.css"/>
<template:addResources type="css" resources="markitupStyle.css"/>
<template:addResources type="css" resources="markitupWikiStyle.css"/>
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="javascript" resources="jquery.markitup.js"/>
<template:addResources type="javascript" resources="markitupWikiSet.js"/>

<h2><c:out value="${currentNode.properties['jcr:title'].string}" /></h2>
<c:if test="${jcr:hasPermission(currentNode,'removeWikiContent')}">
<template:tokenizedForm>
    <form action="<c:url value='${url.base}${currentNode.path}'/>" method="post"
          id="jahia-wiki-article-delete-${currentNode.UUID}">
        <input type="hidden" name="jcrRedirectTo" value="<c:url value='${url.base}${jcr:getParentOfType(renderContext.mainResource.node, "jnt:page").path}'/>"/>
            <%-- Define the output format for the newly created node by default html or by jcrRedirectTo--%>
        <input type="hidden" name="jcrNewNodeOutputFormat" value="html"/>
        <input type="hidden" name="jcrMethodToCall" value="delete"/>
    </form>
</template:tokenizedForm>
<c:set var="currentDisplayName">
	<c:out value="${currentNode.displayableName}" />
</c:set>
<a class="wikipagedelete"  href="#" onclick="confirm('<fmt:message key="label.wikipage.delete.warning"><fmt:param value="${currentDisplayName}"/></fmt:message>')?document.getElementById('jahia-wiki-article-delete-${currentNode.UUID}').submit():false;"><fmt:message key="label.wikipage.delete"/></a>
</c:if>

<template:tokenizedForm>
<form name="formWiki" class="formWiki" action="${currentNode.name}" method="post">
    <input type="hidden" name="jcrAutoCheckin" value="true">
    <input type="hidden" name="jcrNodeType" value="jnt:wikiPage">
    <label><fmt:message key="label.title"/></label><input type="text" name="jcr:title" value="<c:out value="${currentNode.properties['jcr:title'].string}" />"/>
    <script type="text/javascript">
            $(document).ready(function() {
                // Add markItUp! to your textarea in one line
                // $('textarea').markItUp( { Settings }, { OptionalExtraSettings } );
                $('#text-${currentNode.identifier}').markItUp(mySettings);

            });
    </script>
    <textarea class="textareawiki" name="wikiContent" id="text-${currentNode.identifier}" rows="30" cols="85">${currentNode.properties['wikiContent'].string}</textarea>

    <p>
        <label><fmt:message key="jnt_wiki.addComment"/>: </label> <input name="lastComment" value=""/>
    </p>
    <input class="button" type="submit" value="<fmt:message key='label.submit'/>"/>
</form>
</template:tokenizedForm>

