/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.tags.homepage;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

public class DomainsBarFooterTag extends TagSupport {

  protected String m_iconAngleBas;

  public void setIconAngleBas(String iconAngleBas) {
    this.m_iconAngleBas = iconAngleBas;
  }

  public int doStartTag() throws JspException {
    try {
      pageContext.getOut().println("<tr>");
      pageContext
          .getOut()
          .println(
              "	<td width='100%' class='intfdcolor51'><img src='icons/1px.gif' width='1' height='6'></td>");
      pageContext
          .getOut()
          .println(
              "	<td rowspan='3' colspan='2' class='intfdcolor51'><img src='icons/angleBasDomainsBar.gif' width='8' height='8'></td>");
      pageContext.getOut().println("</tr>");
      pageContext.getOut().println("<tr>");
      pageContext
          .getOut()
          .println(
              "	<td width='100%' class='intfdcolor4'><img src='icons/1px.gif' width='1' height='1'></td>");
      pageContext.getOut().println("</tr>");
      pageContext.getOut().println("<tr class='intfdcolor13'>");
      pageContext.getOut().println(
          "	<td width='100%'><img src='icons/1px.gif' width='1' height='1'></td>");
      pageContext.getOut().println("</tr>");
    } catch (IOException ioe) {
      // User probably disconnected ...
      throw new JspTagException("IO_ERROR");
    }

    return SKIP_BODY;
  }

}