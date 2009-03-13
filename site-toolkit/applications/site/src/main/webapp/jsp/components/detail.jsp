<%--
  Copyright 2008 Hippo

  Licensed under the Apache License, Version 2.0 (the  "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS"
  BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License. --%>

<%@ page language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://www.hippoecm.org/jsp/hst/core" prefix='hst'%>

<div>

    <h1>My detail page</h1>
    <div style="border:1px black solid; width:400px;">
    <hst:link var="link" node="${parent}"/>
    <a href="${link}">
    PARENT : ${parent.name}      
    </a>
    
    </div>  
    <div style="border:1px black solid; width:400px;">
    
        ${document.title}
       
        <br/>
        ${document.summary}
        <br/>
        ${document.calendar.time}
        
        <div style="border:1px black solid;" >
        ${document.html.content}
        </div>
     
        
    </div>
