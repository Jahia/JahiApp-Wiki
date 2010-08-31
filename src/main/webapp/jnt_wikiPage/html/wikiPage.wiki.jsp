<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="wiki.css"/>

        <div class='grid_3'><!--start grid_3-->
            <div class="boxwiki">
                <div class="boxwikigrey boxwikipadding16 boxwikimarginbottom16">

                    <div class="boxwiki-inner">
                        <div class="boxwiki-inner-border"><!--start boxwiki -->
                            <h3 class="boxwikititleh3"><fmt:message key="jnt_wiki.searchWiki"/></h3>

                            <div id="wikisearch">
                                <form method="get" action="#">
                                    <fieldset>
                                        <p class="field">
                                            <input name="search" type="text" class="search" tabindex="4"
                                                   value="Search..."/>

                                        </p>

                                        <div class="divButton">
                                            <a class="aButton" href="#"><span><fmt:message key="search"/></span></a>

                                            <div class="clear"></div>
                                        </div>
                                    </fieldset>
                                </form>
                                <ul class="listwiki">
                                    <li>
                                        <a href="#"><fmt:message key="jnt_wiki.advancedSearch"/></a>
                                    </li>

                                </ul>
                            </div>

                            <div class="clear"></div>
                        </div>
                    </div>
                </div>
            </div>
             <!--stop boxwiki -->
            <div class="boxwiki">
                <div class="boxwikigrey boxwikipadding16 boxwikimarginbottom16">
                    <div class="boxwiki-inner">
                        <div class="boxwiki-inner-border"><!--start boxwiki -->

                            <h3 class="boxwikititleh3"><fmt:message key="jnt_wiki.createPage"/></h3>
                            <div id="wikicreate">
                                <form id="wikiFormCreate" name="wikiFormCreate">
                                    <fieldset>
                                        <p class="field">
                                            <input class="create" id="link" name="link" onchange="this.form.action=this.form.elements.link.value+'.html'"/>

                                        </p>

                                        <div class="divButton">
                                            <a class="aButton" href="javascript:document.wikiFormCreate.submit();"><span><fmt:message key="create"/></span></a>

                                            <div class="clear"></div>
                                        </div>
                                    </fieldset>
                                </form>
                            </div>
                            <div class="clear"></div>
                        </div>
                    </div>
                </div>
            </div>
            <!--stop boxwiki -->
            <div class="boxwiki">
                <div class="boxwikigrey boxwikipadding16 boxwikimarginbottom16">
                    <div class="boxwiki-inner">
                        <div class="boxwiki-inner-border"><!--start boxwiki -->

                            <h3 class="boxwikititleh3"><fmt:message key="jnt_wiki.syntax.label"/> </h3>


                            <ul class="listwiki">
                                <li><fmt:message key="jnt_wiki.syntax.link"/></li>
                                <li><fmt:message key="jnt_wiki.syntax.bold"/></li>
                                <li><fmt:message key="jnt_wiki.syntax.underline"/></li>
                                <li><fmt:message key="jnt_wiki.syntax.italic"/></li>
                                <li><fmt:message key="jnt_wiki.syntax.strike"/></li>
                                <li><fmt:message key="jnt_wiki.syntax.monospace"/></li>
                                <li><fmt:message key="jnt_wiki.syntax.superscript"/></li>
                                <li><fmt:message key="jnt_wiki.syntax.subscript"/></li>
                                <li><fmt:message key="jnt_wiki.syntax.level1"/></li>
                                <li><fmt:message key="jnt_wiki.syntax.level2"/></li>
                                <li><fmt:message key="jnt_wiki.syntax.level3"/></li>
                                <li><fmt:message key="jnt_wiki.syntax.level4"/></li>
                                <li><fmt:message key="jnt_wiki.syntax.listitem1"/></li>
                                <li><fmt:message key="jnt_wiki.syntax.listitem2"/></li>
                                <li><fmt:message key="jnt_wiki.syntax.listitem3"/></li>
                            </ul>
							<a href="http://platform.xwiki.org/xwiki/bin/view/Main/XWikiSyntax" target="_new"><fmt:message key="jnt_wiki.syntax.link.label"></fmt:message></a>

                            <div class="clear"></div>
                        </div>
                    </div>
                </div>
            </div>
            <!--stop boxwiki -->
            <div class='clear'></div>
        </div>
        <!--stop grid_3-->


        <div class='grid_10'><!--start grid_10-->

            <h2>${currentNode.name}</h2>

            <div class="idTabsContainer"><!--start idTabsContainer-->
                <c:choose>
                    <c:when test="${currentResource.resolvedTemplate == 'default'}">
                        <ul class="idTabs">
                            <li><a class="on selected" href="${url.base}${currentNode.path}.html"><span><fmt:message key="jnt_wiki.article"/></span></a></li>
                            <li><a class="off" href="${url.base}${currentNode.path}.contribute.html"><span><fmt:message key="jnt_wiki.contribute"/></span></a></li>
                            <li class="spacing"><a class="off" href="${url.base}${currentNode.path}.history.html"><span><fmt:message key="jnt_wiki.history"/> </span></a></li>
                        </ul>
                    </c:when>
                    <c:when test="${currentResource.resolvedTemplate == 'contribute'}">
                        <ul class="idTabs">
                            <li><a class="off" href="${url.base}${currentNode.path}.html"><span><fmt:message key="jnt_wiki.article"/></span></a></li>
                            <li><a class="on selected" href="${url.base}${currentNode.path}.contribute.html"><span><fmt:message key="jnt_wiki.contribute"/></span></a></li>
                            <li class="spacing"><a class="off" href="${url.base}${currentNode.path}.history.html"><span><fmt:message key="jnt_wiki.history"/> </span></a></li>
                        </ul>
                    </c:when>
                    <c:when test="${currentResource.resolvedTemplate == 'history'}">
                        <ul class="idTabs">
                            <li><a class="off" href="${url.base}${currentNode.path}.html"><span><fmt:message key="jnt_wiki.article"/></span></a></li>
                            <li><a class="off" href="${url.base}${currentNode.path}.contribute.html"><span><fmt:message key="jnt_wiki.contribute"/></span></a></li>
                            <li class="spacing"><a class="on selected" href="${url.base}${currentNode.path}.history.html"><span><fmt:message key="jnt_wiki.history"/> </span></a></li>
                        </ul>
                    </c:when>
                    <c:otherwise>
                        <ul class="idTabs">
                            <li><a class="off" href="${url.base}${currentNode.path}.html"><span><fmt:message key="jnt_wiki.article"/></span></a></li>
                            <li><a class="off" href="${url.base}${currentNode.path}.contribute.html"><span><fmt:message key="jnt_wiki.contribute"/></span></a></li>
                            <li class="spacing"><a class="off" href="${url.base}${currentNode.path}.history.html"><span><fmt:message key="jnt_wiki.history"/> </span></a></li>
                        </ul>
                    </c:otherwise>
                </c:choose>
            </div>
            <div class="tabContainer"><!--start tabContainer-->
                ${wrappedContent}
            </div>
            <!--stop tabContainer-->
        </div>
        <!--stop grid_10-->
        <div class='grid_3'><!--start grid_3-->
            <img src="${url.currentModule}/images/jahia-apps-wiki.png" alt="jahia-apps-wiki"/>
            <h4 class="boxwiki-title"><fmt:message key="jnt_wiki.navigation"/></h4>

            <ul id="menuwiki">
                <li class="menuwikitop"><a href="${url.base}${currentNode.parent.path}.html"><fmt:message key="jnt_wiki.wikihome"/></a></li>
                <li class="menuwikitop"><a href="#"><fmt:message key="jnt_wiki.allwikis"/></a></li>
                <li class="menuwikitop"><a href="#"><fmt:message key="jnt_wiki.indexofpages"/></a></li>
            </ul>
            <div class="boxwiki">
                <div class="boxwikigrey boxwikipadding16 boxwikimarginbottom16">
                    <div class="boxwiki-inner">
                        <div class="boxwiki-inner-border"><!--start boxwiki -->
                            <h3 class="boxwikititleh3"><fmt:message key="jnt_wiki.languages"/></h3>

                            <ul class="listwiki">
                                <li><a href="#"><fmt:message key="jnt_wiki.Languages.french"/></a></li>
                                <li><a href="#"><fmt:message key="jnt_wiki.Languages.english"/></a></li>
                                <li><a href="#"><fmt:message key="jnt_wiki.Languages.chinese"/></a></li>
                                <li><a href="#"><fmt:message key="jnt_wiki.Languages.indian"/></a></li>
                            </ul>

                            <div class="clear"></div>
                        </div>
                    </div>
                </div>
            </div>
            <!--stop boxwiki -->
                <div class="boxwiki">
                <div class="boxwikigrey boxwikipadding16 boxwikimarginbottom16">
                    <div class="boxwiki-inner">
                        <div class="boxwiki-inner-border"><!--start boxwiki -->
                            <h3 class="boxwikititleh3"><fmt:message key="jnt_wiki.createPageSummary"/></h3>

									<p>{{box cssClass="summary"}}<br />

{{toc/}}<br />

{{/box}}</p>

                            <div class="clear"></div>
                        </div>
                    </div>
                </div>
            </div>
            <!--stop boxwiki -->
            <div class='clear'></div>
        </div>
        <!--stop grid_3-->