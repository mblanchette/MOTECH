<%--

    MOTECH PLATFORM OPENSOURCE LICENSE AGREEMENT

    Copyright (c) 2010 The Trustees of Columbia University in the City of
    New York and Grameen Foundation USA.  All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:

    1. Redistributions of source code must retain the above copyright notice,
    this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

    3. Neither the name of Grameen Foundation USA, Columbia University, or
    their respective contributors may be used to endorse or promote products
    derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY GRAMEEN FOUNDATION USA, COLUMBIA UNIVERSITY
    AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
    BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
    FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL GRAMEEN FOUNDATION
    USA, COLUMBIA UNIVERSITY OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
    LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
    OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
    NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
    EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

--%>

<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/module/motechmodule/care.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>

<meta name="heading" content="Care Schedules" />
<%@ include file="demoLocalHeader.jsp" %>

<h2>Care Schedules</h2>
<c:forEach items="${patients}" var="patient">
<div>
<h3>Patient ${patient.id}: ${patient.name}</h3>
<table>
	<thead>
		<tr>
			<th>Name</th>
			<th>Due Date</th>
			<th>Late Date</th>
		</tr>
	</thead>
	<tbody>
		<c:forEach items="${patient.cares}" var="care">
			<tr>
				<td>${care.name}</td>
				<td><openmrs:formatDate date="${care.dueDate}" format="yyyy-MM-dd" /></td>
				<td><openmrs:formatDate date="${care.lateDate}" format="yyyy-MM-dd" /></td>
			</tr>
		</c:forEach>
	</tbody>
</table>
</div>
</c:forEach>

<%@ include file="/WEB-INF/template/footer.jsp"%>